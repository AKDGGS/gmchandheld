package gov.alaska.gmchandheld;

import java.io.File;
import java.net.URL;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class UploadPhoto implements Runnable {
    private RemoteAPIDownloadCallback remoteAPIDownloadCallback;
    private String url, token, barcode, description;
    private File file;

    public void setUploadPhotoObj(String url,
                                  String token,
                                  String file,
                                  String barcode,
                                  String description,
                                  RemoteAPIDownloadCallback remoteAPIDownloadCallback) throws Exception {
        if (url == null) {
            throw new Exception("URL is null");
        }
        new URL(url);

        if (token == null) {
            throw new Exception("The token can't be null");
        }
        if (!new File(file).exists()) {
            throw new Exception("The file can't be found.");
        }

        if (barcode == null) {
            throw new Exception("The barcode can't be null");
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
            this.file = new File(file);
            this.barcode = barcode;
            this.description = description;
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
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .followRedirects(false)
                        .followSslRedirects(false)
                        .build();
                okhttp3.Response response = null;
                MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                builder.addFormDataPart("barcode", barcode);
                builder.addFormDataPart("content", file.getName(),
                        RequestBody.create(MediaType.parse("Image/jpeg"), file));
                if (description != null) {
                    builder.addFormDataPart("description", description);
                }

                MultipartBody body = builder.build();
                ImageFileRequestBody imageFileRequestBody = new ImageFileRequestBody(body);
                Request request;
                synchronized (this) {
                    request = new Request.Builder()
                            .header("Authorization", "Token " + token)
                            .url(url)
                            .post(imageFileRequestBody)
                            .build();
                }
                try {
                    response = client.newCall(request).execute();
                    remoteAPIDownloadCallback.displayData(response.toString(), response.code(), response.message());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                remoteAPIDownloadCallback.displayException(e);
            }
            synchronized (this) {
                this.remoteAPIDownloadCallback = null;
                this.description = null;
                this.barcode = null;
                this.file = null;
                this.token = null;
                this.url = null;
            }
        }
    }
}
