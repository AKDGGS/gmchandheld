package gov.alaska.gmchandheld;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HTTPRequest implements Runnable {

    public static final int GET = 0;
    public static final int POST = 1;
    public static final int HEAD = 2;
    private final Object lockObj;
    private HTTPRequestCallback HTTPRequestCallback;
    private String url;
    private RequestBody body;
    private int requestType;
    private HashMap<String, Object> params;
    private OutputStream outputStream;

    public HTTPRequest() {
        lockObj = new Object();
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setFetchDataObj(String url,
                                HTTPRequestCallback HTTPRequestCallback,
                                int requestType,
                                HashMap<String, Object> params,
                                OutputStream outputStream) throws Exception {

        if (url == null) {
            throw new Exception("URL is null");
        }
        new URL(url);

        if (HTTPRequestCallback == null) {
            throw new Exception("The callback can't be null");
        }

        synchronized (lockObj) {
            if (this.url != null) {
                throw new Exception("Processing. Give me a moment and then try again.");
            }
            HttpUrl.Builder httpBuilder = HttpUrl.parse(url).newBuilder();
            this.params = params;
            if (requestType != POST) {
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    if (entry.getValue() instanceof String) {
                        httpBuilder.addQueryParameter(entry.getKey(), (String) entry.getValue());
                    } else if (entry.getValue() instanceof ArrayList) {
                        ArrayList arrList = (ArrayList<String>) entry.getValue();
                        if (entry.getValue() != null && arrList.size() > 0) {
                            int i = 0;
                            while (i < arrList.size()) {
                                httpBuilder.addQueryParameter(entry.getKey(), (String) arrList.get(i));
                                i++;
                            }
                        }
                    } else if (entry.getValue() instanceof Integer) {
                        httpBuilder.addQueryParameter(entry.getKey(), String.valueOf(entry.getValue()));
                    } else if (entry.getValue() instanceof java.io.InputStream) {
                        StringBuilder textBuilder = new StringBuilder();
                        try (Reader reader = new BufferedReader(new InputStreamReader
                                ((InputStream) entry.getValue()))) {
                            int c = 0;
                            while ((c = reader.read()) != -1) {
                                textBuilder.append((char) c);
                            }
                        }
                        httpBuilder.addQueryParameter(entry.getKey(), textBuilder.toString());
                    } else {
                        System.out.println("Input not recognized.  Add it. " +
                                entry.getValue().getClass());
                    }
                }
            } else {
                MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    if (entry.getValue() instanceof String) {
                        builder.addFormDataPart(entry.getKey(), (String) entry.getValue());
                    } else if (entry.getValue() instanceof File) {
                        builder.addFormDataPart("content", ((File) entry.getValue()).getName(),
                                RequestBody.create(MediaType.parse("Image/jpeg"),
                                        (File) entry.getValue()));
                    } else if (entry.getValue() instanceof ArrayList) {
                        ArrayList arrList = (ArrayList<String>) entry.getValue();
                        if (entry.getValue() != null && arrList.size() > 0) {
                            int i = 0;
                            while (i < arrList.size()) {
                                builder.addFormDataPart(entry.getKey(), (String) arrList.get(i));
                                i++;
                            }
                        }
                    } else {
                        System.out.println("Input not recognized.  Add it. " +
                                entry.getValue().getClass());
                    }
                }
                this.body = builder.build();
            }
            this.url = httpBuilder.toString();
            this.HTTPRequestCallback = HTTPRequestCallback;
            this.requestType = requestType;
            this.outputStream = outputStream;
            lockObj.notify();
        }
    }

    public void run() {
        URL myURL;
        Request request;
        //Infinitely produce items
        while (true) {
            synchronized (lockObj) {
                try {
                    url = null;
                    lockObj.wait();
                    myURL = new URL(url);
                } catch (Exception e) {
                    continue;
                }
            }
            switch (requestType) {
                case GET:
                    request = new Request.Builder()
                            .header("Authorization", "Token " + BaseActivity.getToken())
                            .url(myURL)
                            .build();
                    break;
                case POST:
                    request = new Request.Builder()
                            .header("Authorization", "Token " + BaseActivity.getToken())
                            .url(myURL)
                            .post(body)
                            .build();
                    break;
                case HEAD:
                    request = new Request.Builder()
                            .header("Authorization", "Token " + BaseActivity.getToken())
                            .url(myURL)
                            .head()
                            .build();
                    break;
                default:
                    request = null;
                    continue;
            }

            OkHttpClient client = new OkHttpClient.Builder()
                    .followRedirects(false)
                    .followSslRedirects(false)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .callTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(false)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                int count;
                switch (requestType) {
                    case GET:
                    case POST: {
                        if (outputStream == null) {
                            HTTPRequestCallback.displayData(response.body().bytes(), response.headers().getDate("Last-Modified"), response.code(),
                                    response.message(), requestType);
                        } else {
                            InputStream input = response.body().byteStream();
                            byte[] data = new byte[1024];
                            long total = 0;
                            while ((count = input.read(data)) != -1) {
                                total += count;
                                outputStream.write(data, 0, count);
                            }
                            outputStream.flush();
                            outputStream.close();
                            input.close();
                            HTTPRequestCallback.displayData(null, response.headers().getDate("Last-Modified"), response.code(),
                                    response.message(), requestType);
                        }
                        break;
                    }
                    case HEAD: {
                        HTTPRequestCallback.displayData(null, response.headers().getDate("Last-Modified"),
                                response.code(), response.message(), requestType);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                HTTPRequestCallback.displayException(e);
            }
        }
    }
}