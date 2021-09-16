package gov.alaska.gmchandheld;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class RemoteAPIDownload implements Runnable {
    private String url;
    private RemoteAPIDownloadCallback downloadingCallback;

    public void setAPICallback(RemoteAPIDownloadCallback downloadingCallback){
        this.downloadingCallback = downloadingCallback;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void run() {
        InputStream inputStream;
        HttpURLConnection connection;
        try {
            URL myURL = new URL(url);
            connection = (HttpURLConnection) myURL.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Token " + BaseActivity.apiKeyBase);
            connection.setReadTimeout(10 * 1000);
            connection.setConnectTimeout(5 * 1000);
            connection.connect();
            try {
                inputStream = connection.getInputStream();
            } catch (Exception e) {
                inputStream = connection.getErrorStream();
            }
            try {
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
                    System.out.println("RCL : " + connection.getResponseCode());
                    System.out.println("ML " + connection.getResponseMessage());
                }
                downloadingCallback.displayData(sb.toString(), connection.getResponseCode(), connection.getResponseMessage());

            } catch (Exception e) {
                e.printStackTrace();
            }
            inputStream.close();
            connection.disconnect();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}