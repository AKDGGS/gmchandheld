package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class DownloadData {
	private Exception exception = null;
	private String rawJson;
	private JSONArray jsonArrayResult;
	private String url;
	private Context context;

	public DownloadData(String url, Context context) {

		this.url = url;
		this.context = context;
	}

	public boolean isErrored() {
		return exception != null;
	}

	public Exception getException() {
		return exception;
	}

	public String getRawJson() {
		return rawJson;
	}

	public JSONArray getJsonArrayResult() {
		return jsonArrayResult;
	}

	public DownloadData getDataFromURL() {
		SharedPreferences sp;
		InputStream inputStream;
		HttpURLConnection connection;

		try {
			URL myURL = new URL(url);
			connection = (HttpURLConnection) myURL.openConnection();
			connection.setReadTimeout(60000);
			connection.setConnectTimeout(200000);
			connection.setRequestMethod("GET");
			connection.connect();

			inputStream = connection.getInputStream();

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
				rawJson = sb.toString();
				sp = context.getSharedPreferences("userPreferences", Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = sp.edit();
				editor.putString("downloadedDataString", rawJson);
				editor.commit();
				System.out.println(rawJson);


				inputStream.close();
				connection.disconnect();
				return this;
			} catch (IOException e) {
				exception = e;
				return this;
			}
		} catch (ProtocolException e) {
			exception = e;
			return this;
		} catch (MalformedURLException e) {
			exception = e;
			return this;
		} catch (IOException e) {
			exception = e;
			return this;
		}
	}

//	public DownloadData fetchJSON() {
//
//		InputStream inputStream;
//		HttpURLConnection connection;
//
//		try {
//			URL myURL = new URL(url);
//
//			connection = (HttpURLConnection) myURL.openConnection();
//			connection.setReadTimeout(10000);
//			connection.setConnectTimeout(200000);
//			connection.setRequestMethod("GET");
//			connection.connect();
//
//			inputStream = connection.getInputStream();
//
//			try {
//				StringBuilder sb = new StringBuilder();
//				byte[] buffer = new byte[4096];
//				int buffer_read = 0;
//
//				while (buffer_read != -1) {
//					buffer_read = inputStream.read(buffer);
//					if (buffer_read > 0) {
//						sb.append(new String(buffer, 0, buffer_read));
//					}
//				}
//				rawJson = sb.toString();
//
//				Object json = new JSONTokener(rawJson).nextValue();
//				if (json instanceof JSONArray) {
//					jsonArrayResult = (JSONArray) json;
//				}
//
//				inputStream.close();
//				connection.disconnect();
//				return this;
//			} catch (IOException | JSONException e) {
//				exception = e;
//				return this;
//			}
//		} catch (ProtocolException e) {
//			exception = e;
//			return this;
//		} catch (MalformedURLException e) {
//			exception = e;
//			return this;
//		} catch (IOException e) {
//			exception = e;
//			return this;
//		}
//	}
}
