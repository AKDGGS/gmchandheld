package gov.alaska.gmc_handheld_v2_simpleJSON;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class DownloadData {
	private Exception exception = null;
	private ByteArrayOutputStream byteArrayOutputStream;


	public DownloadData() {
		this.byteArrayOutputStream =  new ByteArrayOutputStream();
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

	public ByteArrayOutputStream getByteArrayOutputStream() {
		return byteArrayOutputStream;
	}

	public DownloadData donwloadData(DownloadData obj, String barcode){
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
//			byteArrayOutputStream = new ByteArrayOutputStream();

			int i;
			try {
				i = inputStream.read();
				while (i != -1) {
					obj.byteArrayOutputStream.write(i);
					i = inputStream.read();
				}
				inputStream.close();
				connection.disconnect();
				return obj;
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
