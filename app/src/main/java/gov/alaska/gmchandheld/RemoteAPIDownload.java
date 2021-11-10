package gov.alaska.gmchandheld;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RemoteAPIDownload implements Runnable {

    public static final int GET = 0;
    public static final int POST = 1;
    public static final int HEAD = 2;
    public static final int APK = 3;
    private final Object lockObj;
    private RemoteAPIDownloadCallback remoteAPIDownloadCallback;
    private String url;
    private RequestBody body;
    private int requestType;
    private HashMap<String, Object> params;

    public RemoteAPIDownload() {
        lockObj = new Object();
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setFetchDataObj(String url,
                                RemoteAPIDownloadCallback remoteAPIDownloadCallback,
                                int requestType,
                                HashMap<String, Object> params) throws Exception {

        if (url == null) {
            throw new Exception("URL is null");
        }
        new URL(url);

        if (remoteAPIDownloadCallback == null) {
            throw new Exception("The callback can't be null");
        }

        synchronized (lockObj) {
            if (this.url != null) {
                throw new Exception("Uploading is busy");
            }
            StringBuilder sb = new StringBuilder();
            this.params = params;
            if (requestType != POST) {
                sb.append(url);
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    if (entry.getValue() instanceof String) {
                        sb.append("&");
                        sb.append(URLEncoder.encode(entry.getKey(), "utf-8"));
                        sb.append("=");
                        sb.append(URLEncoder.encode((String) entry.getValue(), "utf-8"));
                    } else if (entry.getValue() instanceof ArrayList) {
                        String delim = "&" + entry.getKey() + "=";
                        if (entry.getValue() != null && ((ArrayList<?>) entry.getValue()).size() > 0) {
                            sb.append(delim);
                            int i = 0;
                            while (i < ((ArrayList<?>) entry.getValue()).size() - 1) {
                                try {
                                    sb.append(URLEncoder.encode(
                                            ((ArrayList<String>) entry.getValue()).get(i), "utf-8"));
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                                sb.append(delim);
                                i++;
                            }
                            sb.append(((ArrayList<String>) entry.getValue()).get(i));
                        }
                    } else if (entry.getValue() instanceof Integer) {
                        System.out.println("Integer");
                    } else if (entry.getValue() instanceof java.io.InputStream) {
                        System.out.println("Input Stream");
                    } else {
                        System.out.println("Input not recognized.  Add it. " +
                                entry.getValue().getClass());
                    }
                }
            } else {
                sb.append(url);
                MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    if (entry.getValue() instanceof String) {
                        builder.addFormDataPart(entry.getKey(), (String) entry.getValue());
                    } else if (entry.getValue() instanceof File) {
                        builder.addFormDataPart("content", ((File) entry.getValue()).getName(),
                                RequestBody.create(MediaType.parse("Image/jpeg"),
                                        (File) entry.getValue()));
                    } else {
                        System.out.println("Input not recognized.  Add it. " +
                                entry.getValue().getClass());
                    }
                }
                this.body = builder.build();
            }
            this.url = sb.toString();
            this.remoteAPIDownloadCallback = remoteAPIDownloadCallback;
            this.requestType = requestType;
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
                case APK:
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
                    .callTimeout(15, TimeUnit.SECONDS)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                int count;
                InputStream input;
                input = new BufferedInputStream(response.body().byteStream(), 8192);
                switch (requestType) {
                    case GET:
                    case POST: {
                        StringBuilder textBuilder = new StringBuilder();
                        try (Reader reader = new BufferedReader(new InputStreamReader
                                (input))) {
                            int c = 0;
                            while ((c = reader.read()) != -1) {
                                textBuilder.append((char) c);
                            }
                        }
                        input.close();
                        remoteAPIDownloadCallback.displayData(textBuilder.toString(), response.code(),
                                response.message(), requestType);
                        break;
                    }
                    case HEAD: {
                        remoteAPIDownloadCallback.displayData(response.headers().get("Last-Modified"),
                                response.code(), response.message(), requestType);
                        break;
                    }
                    case APK: {
                        OutputStream output = new FileOutputStream(
                                BaseActivity.sp.getString("apkSavePath", ""));
                        byte[] data = new byte[1024];
                        long total = 0;
                        while ((count = input.read(data)) != -1) {
                            total += count;
                            output.write(data, 0, count);
                        }
                        output.flush();
                        output.close();
                        input.close();
                        remoteAPIDownloadCallback.displayData(null, response.code(),
                                response.message(), requestType);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                remoteAPIDownloadCallback.displayException(e);
            }
        }
    }
}