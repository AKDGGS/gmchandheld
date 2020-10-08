package gov.alaska.gmc_handheld_v2_simpleJSON;


import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class DownloadData {
	private Exception exception = null;
	private String url;
	private String rawJson;
	private String barcodeQuery;
	private Context context;
	public static final String SHARED_PREFS = "sharedPrefs";

	public DownloadData(String url, String barcodeQuery, Context context) {
		this.url = url;
		this.barcodeQuery = barcodeQuery;
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


	public void getDataFromURL() {
		InputStream inputStream;
		HttpURLConnection connection;

		try {
			URL myURL = new URL(url);
			connection = (HttpURLConnection) myURL.openConnection();

			Date date = new Date();
			String HDATE = getDateFormat().format(date);

			String QUERYPARAM = "barcode=" + barcodeQuery;
			String message = HDATE + "\n" + QUERYPARAM;

			SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
			String APIKEY = sharedPreferences.getString("apiText", "");

			String AUTH_DGST = getDGST(APIKEY, message);

			connection.setRequestMethod("GET");
			connection.setRequestProperty("Authorization", "BASE64-HMAC-SHA256 " + AUTH_DGST);
			connection.setRequestProperty("Date", HDATE);

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
				inputStream.close();
				connection.disconnect();
			} catch (IOException e) {
				exception = e;
				inputStream.close();
				connection.disconnect();
			}
		} catch (ProtocolException e) {
			exception = e;
		} catch (MalformedURLException e) {
			exception = e;
		} catch (IOException e) {
			exception = e;
		}
	}


	public SimpleDateFormat getDateFormat() {
		SimpleDateFormat sdf = new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss zzz"
		);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		return sdf;
	}


	private String getDGST(String APIKEY, String message) {
		Mac mac = null;
		byte[] hmac256 = null;
		try {
			mac = Mac.getInstance("HmacSHA256");
			SecretKeySpec sks = new SecretKeySpec(APIKEY.getBytes(), "HmacSHA256");
			mac.init(sks);
			hmac256 = mac.doFinal(message.getBytes());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		return android.util.Base64.encodeToString(hmac256, android.util.Base64.DEFAULT);
	}
}
