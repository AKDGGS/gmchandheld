package gov.alaska.gmchandheld;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class RemoteAPIDownload implements Runnable {

    private final Object lockObj = new Object();
    private RemoteAPIDownloadCallback remoteAPIDownloadCallback;
    private String url, token;
    private RequestBody body;
    private boolean running;

    public RemoteAPIDownload() {
        running = true;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setFetchDataObj(String url,
                                String token,
                                RequestBody body,
                                RemoteAPIDownloadCallback remoteAPIDownloadCallback) throws Exception {

        running = true;

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
            System.out.print(" lock 1 ");
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
        while (running) {
            if (Thread.currentThread().isInterrupted()) {
                running = false;
            }
            synchronized (lockObj) {
                System.out.print("************** lock 2 ");
                try {
                    lockObj.wait();
                } catch (Exception e) {
                    System.out.println("Exception thrown in lock 2: " + e.getMessage());
                    continue;
                }
            }

            try {
                if (body == null) {
                    InputStream inputStream;
                    HttpURLConnection connection;
                    URL myURL;
                    synchronized (lockObj) {
                        System.out.print(" lock 3 ");
                        myURL = new URL(url);
                    }
                    connection = (HttpURLConnection) myURL.openConnection();
                    connection.setRequestMethod("GET");
                    if (token != null) {
                        connection.setRequestProperty("Authorization", "Token " + token);
                    }
                    connection.setReadTimeout(5 * 1000);
                    connection.setConnectTimeout(5 * 1000);
                    connection.connect();

                    System.out.print("Response code: " + url.toString() + " " + connection.getResponseCode());

                    StringBuilder sb = new StringBuilder();
                    inputStream = connection.getInputStream();
                    byte[] buffer = new byte[4096];
                    int buffer_read = 0;
                    while (buffer_read != -1) {
                        buffer_read = inputStream.read(buffer);
                        if (buffer_read > 0) {
                            sb.append(new String(buffer, 0, buffer_read));
                        }
                    }
                    if (connection.getErrorStream() != null) {
                        int length;
                        while ((length = connection.getErrorStream().read(buffer)) != -1) {
                            sb.append(new String(buffer, 0, length));
                        }
                        throw new Exception(sb.toString());
                    }
                    remoteAPIDownloadCallback.displayData(sb.toString(), connection.getResponseCode(),
                            connection.getResponseMessage());
                    inputStream.close();
                    connection.disconnect();
                } else if (body.contentType().type().equals("multipart")) {
                    ImageFileRequestBody imageFileRequestBody;
                    Request request;

                    OkHttpClient client = new OkHttpClient().newBuilder()
                            .followRedirects(false)
                            .followSslRedirects(false)
                            .build();
                    okhttp3.Response response;
                    imageFileRequestBody = new ImageFileRequestBody(body);
                    synchronized (this) {
                        System.out.print(" lock 4 ");
                        request = new Request.Builder()
                                .header("Authorization", "Token " + token)
                                .url(url)
                                .post(imageFileRequestBody)
                                .build();
                    }
                    response = client.newCall(request).execute();
                    try {
                        remoteAPIDownloadCallback.displayData(response.toString(), response.code(), response.message());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                remoteAPIDownloadCallback.displayException(e);
            }
            synchronized (lockObj) {
                System.out.print(" lock 5 ");
                this.remoteAPIDownloadCallback = null;
                this.body = null;
                this.token = null;
                this.url = null;
            }
        }
    }
}