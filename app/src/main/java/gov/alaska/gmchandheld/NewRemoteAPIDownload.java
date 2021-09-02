package gov.alaska.gmchandheld;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;


public class NewRemoteAPIDownload {
    private Exception exception = null;
    private String rawJson;

    public String getDataFromURL(String url) {
        InputStream inputStream;
        HttpURLConnection connection;
        int responseCode;
        System.out.println("Get Data from url: " + url);
        try {
            URL myURL = new URL(url);
            connection = (HttpURLConnection) myURL.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Token " + BaseActivity.apiKeyBase);
            connection.setReadTimeout(10 * 1000);
            connection.setConnectTimeout(5 * 1000);
            connection.connect();
            responseCode = connection.getResponseCode();
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
                    exception = new Exception(String.valueOf(sb));
                } else {
                    rawJson = sb.toString();
                    return rawJson;
                }
                inputStream.close();
                connection.disconnect();
            } catch (Exception e) {
                exception = e;
                inputStream.close();
                connection.disconnect();
            }
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
