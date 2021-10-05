package gov.alaska.gmchandheld;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RemoteAPIDownload implements Runnable {
    private static Lock lock = new ReentrantLock();
    private RemoteAPIDownloadCallback remoteAPIDownloadCallback;
    private String url, token;
    private Condition cond = lock.newCondition();

    public void setFetchDataObj(String url, String token, RemoteAPIDownloadCallback remoteAPIDownloadCallback) throws Exception {
        if (url == null) {
            throw new Exception("URL is null");
        }
        if (remoteAPIDownloadCallback == null) {
            throw new Exception("The callback can't be null");
        }

        new URL(url);
        try {
            lock.lock();
            if (this.url != null) {
                throw new Exception("Fetch is busy");
            }
            this.url = url;
            this.token = token;
            this.remoteAPIDownloadCallback = remoteAPIDownloadCallback;
            cond.signal();
        } finally {
            lock.unlock();
        }
    }

    public void run() {
        //Infinitely produce items
        while (true) {
            try {
                lock.lock();
                cond.await();
            } catch (Exception e) {
                continue;
            } finally {
                lock.unlock();
            }

            try {
                InputStream inputStream;
                HttpURLConnection connection;
                URL myURL;
                try {
                    lock.lock();
                    myURL = new URL(url);
                } finally {
                    lock.lock();
                }
                connection = (HttpURLConnection) myURL.openConnection();
                connection.setRequestMethod("GET");
                if (token != null) {
                    connection.setRequestProperty("Authorization", "Token " + token);
                }
                connection.setReadTimeout(10 * 1000);
                connection.setConnectTimeout(5 * 1000);
                connection.connect();
                inputStream = connection.getInputStream();
                StringBuilder sb = new StringBuilder();
                byte[] buffer = new byte[4096];
                int buffer_read = 0;
                while (buffer_read != -1) {
                    buffer_read = inputStream.read(buffer);
                    if (buffer_read > 0) {
                        sb.append(new String(buffer, 0, buffer_read));
                    }
                }
                remoteAPIDownloadCallback.displayData(sb.toString(), connection.getResponseCode(),
                        connection.getResponseMessage());
                inputStream.close();
                connection.disconnect();
            } catch (Exception e) {
                remoteAPIDownloadCallback.displayException(e);
            }
            try {
                lock.lock();
                remoteAPIDownloadCallback = null;
                token = null;
                url = null;
            } finally {
                lock.unlock();
            }
        }
    }
}