package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

interface AsyncResponse {
	void processFinish(String output);
}

public class DownloadData extends AsyncTask<String, Void, String> {

	public DownloadData() {}

	public AsyncResponse delegate = null;

	@Override
	protected String doInBackground(String... strings) {

		String websiteURL;

		int APILevel = android.os.Build.VERSION.SDK_INT;
		if (APILevel < 18) {
			websiteURL = "http://maps.dggs.alaska.gov/gmc/inventory.json?barcode=" + strings[0];
		} else {
			websiteURL = "https://maps.dggs.alaska.gov/gmc/inventory.json?barcode=" + strings[0];
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
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

			int i;
			try {
				i = inputStream.read();
				while (i != -1) {
					byteArrayOutputStream.write(i);
					i = inputStream.read();
				}
				inputStream.close();
				connection.disconnect();
				delegate.processFinish(byteArrayOutputStream.toString());
				return byteArrayOutputStream.toString();
			} catch (IOException e) {
				delegate.processFinish(e.toString());
				return e.toString();
			}
		} catch (ProtocolException e) {
			delegate.processFinish(e.toString());

			return null;
		} catch (MalformedURLException e) {
			delegate.processFinish(e.toString());
			return null;
		} catch (IOException e) {
			delegate.processFinish(e.toString());
			return null;
		}
	}
}

