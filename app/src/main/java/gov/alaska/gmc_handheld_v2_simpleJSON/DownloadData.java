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
	private StringBuilder sb;
	private int responseCode;
	private String responseMsg;
	private final String url;
	private String rawJson;
	private final String barcodeQuery;
	private final Context context;
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

	public int getResponseCode() {
		return responseCode;
	}

	public String getResponseMsg() {
		return responseMsg;
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

			connection.setReadTimeout( 10 * 1000);
			connection.setConnectTimeout(5 * 1000);

			connection.setRequestMethod("GET");

			connection.connect();
			System.out.println(connection.getReadTimeout());

			responseCode = connection.getResponseCode();
			responseMsg = connection.getResponseMessage();

			try {
				inputStream = connection.getInputStream();
			} catch (IOException e) {
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
					System.out.println(sb.toString());
				}

				if (sb.length() <= 2) {
					exception = new Exception("No results found.\n\nIs the barcode correct? " + barcodeQuery);
				} else {
					rawJson = sb.toString();
				}

				inputStream.close();
				connection.disconnect();
			}catch (IOException e) {
				exception = e;
				inputStream.close();
				connection.disconnect();
			}
		} catch (ProtocolException e) {
			exception = e;
		} catch (MalformedURLException e) {
			exception = e;
			exception = new Exception(String.valueOf(sb));
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
		Mac mac;
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
