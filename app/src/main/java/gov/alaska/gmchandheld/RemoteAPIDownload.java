package gov.alaska.gmchandheld;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.util.concurrent.TimeUnit;

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
    private String url, token;
    private RequestBody body;
    private int requestType;

    public RemoteAPIDownload() {
        lockObj = new Object();
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setFetchDataObj(String url,
                                String token,
                                RequestBody body,
                                RemoteAPIDownloadCallback remoteAPIDownloadCallback,
                                int requestType) throws Exception {

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
            this.url = url;
            this.token = token;
            this.body = body;
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
                        remoteAPIDownloadCallback.displayData(textBuilder.toString(), response.code(), response.message(), requestType);
                        break;
                    }
                    case HEAD: {
                        remoteAPIDownloadCallback.displayData(response.headers().get("Last-Modified"), response.code(), response.message(), requestType);
                        break;
                    }
                    case APK: {
                        OutputStream output = new FileOutputStream(BaseActivity.sp.getString("apkSavePath", ""));
                        byte[] data = new byte[1024];
                        long total = 0;
                        while ((count = input.read(data)) != -1) {
                            total += count;
                            output.write(data, 0, count);
                        }
                        output.flush();
                        output.close();
                        input.close();
                        remoteAPIDownloadCallback.displayData(null, response.code(), response.message(), requestType);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                remoteAPIDownloadCallback.displayException(e);
            }
        }
    }
}