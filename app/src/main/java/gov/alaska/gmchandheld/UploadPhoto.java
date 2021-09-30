package gov.alaska.gmchandheld;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class UploadPhoto implements Runnable {
    private RemoteAPIDownloadCallback remoteAPIDownloadCallback;
    private String url, token, barcode, description;
    private RequestBody body;

    public void setUploadPhotoObj(String url,
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

        synchronized (this) {
            if (this.url != null) {
                throw new Exception("Uploading is busy");
            }

            this.url = url;
            this.token = token;
            this.body = body;
            this.remoteAPIDownloadCallback = remoteAPIDownloadCallback;
            notify();
        }
    }

    public void run() {
        //Infinitely produce items
        while (true) {
            synchronized (this) {
                try {
                    wait();
                } catch (Exception e) {
//                    fetchDataCallback.displayException(e);
                    continue;
                }
            }

            try {
                if (body == null){
                    InputStream inputStream;
                    HttpURLConnection connection;
                    URL myURL;
                    synchronized (this) {
                        myURL = new URL(url);
                    }
                    connection = (HttpURLConnection) myURL.openConnection();
                    connection.setRequestMethod("GET");
                    if (token != null) {
                        connection.setRequestProperty("Authorization", "Token " + token);
                    }
                    connection.setReadTimeout(10 * 1000);
                    connection.setConnectTimeout(5 * 1000);
                    connection.connect();

                    try {
                        inputStream = connection.getInputStream();
                    } catch (Exception e) {
                        inputStream = connection.getErrorStream();
                    }
                    StringBuilder sb = new StringBuilder();
                    byte[] buffer = new byte[4096];
                    int buffer_read = 0;
                    while (buffer_read != -1) {
                        buffer_read = inputStream.read(buffer);
                        if (buffer_read > 0) {
                            sb.append(new String(buffer, 0, buffer_read));
                        }
                    }
                    if (connection.getErrorStream() != null) {
                        sb.append(connection.getResponseMessage());
                    }
                    remoteAPIDownloadCallback.displayData(sb.toString(), connection.getResponseCode(),
                            connection.getResponseMessage());
                    inputStream.close();
                    connection.disconnect();
                }
                else if (body.contentType().type().equals("multipart")){
                    ImageFileRequestBody imageFileRequestBody;
                    Request request;

                    OkHttpClient client = new OkHttpClient().newBuilder()
                            .followRedirects(false)
                            .followSslRedirects(false)
                            .build();
                    okhttp3.Response response = null;

                    switch (body.contentType().type()) {
                        case "multipart":
                            imageFileRequestBody = new ImageFileRequestBody(body);
                            synchronized (this) {
                                request = new Request.Builder()
                                        .header("Authorization", "Token " + token)
                                        .url(url)
                                        .post(imageFileRequestBody)
                                        .build();
                            }
                            response = client.newCall(request).execute();
                            break;
                    }
                    try {
                        remoteAPIDownloadCallback.displayData(response.toString(), response.code(), response.message());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                remoteAPIDownloadCallback.displayException(e);
            }
            synchronized (this) {
                this.remoteAPIDownloadCallback = null;
                this.body = null;
                this.token = null;
                this.url = null;
            }
        }
    }
}
