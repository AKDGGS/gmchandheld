package gov.alaska.gmchandheld;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RemoteAPIDownload implements Runnable {


    private RemoteAPIDownloadCallback remoteAPIDownloadCallback;
    private String url, token;

    public void setFetchDataObj(String url, String token, RemoteAPIDownloadCallback remoteAPIDownloadCallback) throws Exception {
        if (url == null) {
            throw new Exception("URL is null");
        }
        if (remoteAPIDownloadCallback == null) {
            throw new Exception("The callback can't be null");
        }

        new URL(url);

        synchronized (this) {
            if (this.url != null) {
                throw new Exception("Fetch is busy");
            }
            this.url = url;
            this.token = token;
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
//                    dataCallback.displayException(e);
                    continue;
                }
            }

            try {
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
                synchronized (this) {
                    remoteAPIDownloadCallback = null;
                    token = null;
                    url = null;
                }
            } catch (Exception e) {
                remoteAPIDownloadCallback.displayException(e);
            }
        }
    }
}