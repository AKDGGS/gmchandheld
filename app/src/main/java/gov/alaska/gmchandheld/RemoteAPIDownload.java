package gov.alaska.gmchandheld;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class RemoteAPIDownload implements Runnable {

    public static final int GET = 0;
    public static final int POST = 1;
    public static final int HEAD = 2;
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

//        if (token == null) {
//            throw new Exception("The token can't be null");
//        }

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
            Request request;
            switch (requestType) {
                case GET:
                    request = new Request.Builder()
                            .header("Authorization", "Token " + BaseActivity.apiKeyBase)
                            .url(myURL)
                            .build();
                    break;
                case POST:
                    ImageFileRequestBody imageFileRequestBody;
                    imageFileRequestBody = new ImageFileRequestBody(body);
                    request = new Request.Builder()
                            .header("Authorization", "Token " + BaseActivity.apiKeyBase)
                            .url(myURL)
                            .post(imageFileRequestBody)
                            .build();
                    break;
                case HEAD:
                    request = new Request.Builder()
                            .header("Authorization", "Token " + BaseActivity.apiKeyBase)
                            .url(myURL)
                            .head()
                            .build();
                    break;
                default:
                    System.out.println("Default reached");
                    request = null;
            }

            okhttp3.Response response;
            OkHttpClient client = new OkHttpClient.Builder()
                    .followRedirects(false)
                    .followSslRedirects(false)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build();
            try {
                long startTime = System.currentTimeMillis();
                response = client.newCall(request).execute();
                int timeout = 15 * 1000;
                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed > timeout) {
                    throw new InterruptedException("Timeout");
                }

                try {
                    switch (requestType) {
                        case GET: {
                            remoteAPIDownloadCallback.displayData(response.body().string(), response.code(), response.message(), requestType);
                            break;
                        }
                        case POST: {
                            remoteAPIDownloadCallback.displayData(response.toString(), response.code(), response.message(), requestType);
                            break;
                        }
                        case HEAD: {
                            remoteAPIDownloadCallback.displayData(response.headers().get("Last-Modified"), response.code(), response.message(), requestType);
                            break;
                        }
                    }
                } catch (Exception e) {
                    remoteAPIDownloadCallback.displayException(e);
                    e.printStackTrace();
                }
            } catch (InterruptedException interruptedException) {
                remoteAPIDownloadCallback.displayException(interruptedException);
            } catch (Exception e) {
                remoteAPIDownloadCallback.displayException(e);
            }
        }
    }
}