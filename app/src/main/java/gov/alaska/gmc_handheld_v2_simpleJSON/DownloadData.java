package gov.alaska.gmc_handheld_v2_simpleJSON;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class DownloadData {
	private Exception exception = null;
	private String rawJson;


	public DownloadData() {
	}

	public boolean isErrored(){
		if (exception != null){
			return true;
		}
		return false;
	}

	public Exception getException() {
		return exception;
	}

	public String getRawJson() {
		return rawJson;
	}

	public DownloadData getDataFromURL(String barcode){
		String websiteURL;

		int APILevel = android.os.Build.VERSION.SDK_INT;
		if (APILevel < 18) {
			websiteURL = "http://maps.dggs.alaska.gov/gmc/inventory.json?barcode=" + barcode;
		} else {
			websiteURL = "https://maps.dggs.alaska.gov/gmc/inventory.json?barcode=" + barcode;
		}

		InputStream inputStream;

		// Retry code: https://stackoverflow.com/a/37443321
		HttpURLConnection connection;

		try {
			URL myURL = new URL(websiteURL);
			connection = (HttpURLConnection) myURL.openConnection();
			connection.setReadTimeout(10000);
			connection.setConnectTimeout(200000);
			connection.setRequestMethod("GET");
			connection.connect();

			inputStream = connection.getInputStream();

			int i;
			try {
				StringBuilder sb = new StringBuilder();
				byte[] buffer = new byte[4096];
				int buffer_read = 0;

				while (buffer_read != -1) {
					buffer_read = inputStream.read(buffer);
					if(buffer_read > 0){
						sb.append(new String(buffer, 0, buffer_read));
					}
				}
				rawJson = sb.toString();
				inputStream.close();
				connection.disconnect();
				return null;
			} catch (IOException e) {
				exception = e;
				return null;
			}
		} catch (ProtocolException e) {
			exception = e;
			return null;
		} catch (MalformedURLException e) {
			exception = e;
			return null;
		} catch (IOException e) {
			exception = e;
			return null;
		}
	}
}
