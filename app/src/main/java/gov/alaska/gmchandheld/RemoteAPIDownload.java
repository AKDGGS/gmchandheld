package gov.alaska.gmchandheld;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class RemoteAPIDownload implements Runnable {

    private final Object lockObj;
    private RemoteAPIDownloadCallback remoteAPIDownloadCallback;
    private String url, token;
    private RequestBody body;

    public RemoteAPIDownload() {
        lockObj = new Object();
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setFetchDataObj(String url,
                                String token,
                                RequestBody body,
                                RemoteAPIDownloadCallback remoteAPIDownloadCallback) throws Exception {


        if (url == null) {
            throw new Exception("URL is null");
        }
        new URL(url);

        if (token == null) {
            throw new Exception("The token can't be null");
        }

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
            lockObj.notify();
        }
    }

    public void run() {
        //Infinitely produce items
        while (true) {
            synchronized (lockObj) {
                try {
                    url = null;
                    lockObj.wait();
                } catch (Exception e) {
                    continue;
                }
            }
            Request request;
            okhttp3.Response response;
            OkHttpClient client = new OkHttpClient.Builder()
                    .followRedirects(false)
                    .followSslRedirects(false)
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .writeTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(5, TimeUnit.SECONDS)
                    .build();
            try {
                if (body == null) {
                    try {

                        URL myURL;
                        synchronized (lockObj) {
                            myURL = new URL(url);
                        }
                        request = new Request.Builder()
                                .header("Authorization", "Token " + token)
                                .url(myURL)
                                .build();
                        response = client.newCall(request).execute();
                        remoteAPIDownloadCallback.displayData(response.body().string(), response.code(), response.message());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (body.contentType().type().equals("multipart")) {
                    ImageFileRequestBody imageFileRequestBody;
                    imageFileRequestBody = new ImageFileRequestBody(body);
                    synchronized (this) {
                        request = new Request.Builder()
                                .header("Authorization", "Token " + token)
                                .url(url)
                                .post(imageFileRequestBody)
                                .build();
                    }
                    response = client.newCall(request).execute();
                    remoteAPIDownloadCallback.displayData(response.toString(), response.code(), response.message());
                }
            } catch (Exception e) {
                remoteAPIDownloadCallback.displayException(e);
            }
        }
    }
}