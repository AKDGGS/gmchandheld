package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.content.Context;

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

	public DownloadData(String url, Context context) {
		this.url = url;
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


			SimpleDateFormat sdf = new SimpleDateFormat(
					"EEE, dd MMM yyyy HH:mm:ss zzz"
			);
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

			Date date = new Date();
			String HDATE= "Date" + sdf.format(date) + "\n";

			HDATE = "Thu, 01 Oct 2020 22:46:47 GMT";
			String url_query = "barcode=GMC-000096345";
			String message = HDATE + "\n" + url_query;

			String APIKEY = "thXAgLfS68TRpmixfvr2nksFQYrzZf5F";

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

			String AUTH_DGST= android.util.Base64.encodeToString(hmac256, android.util.Base64.DEFAULT);
			System.out.println("HMAC-SHA256 Base64: " + AUTH_DGST);


			connection.setRequestMethod("GET");
			connection.setRequestProperty ("Authorization", "BASE64-HMAC-SHA256 " + AUTH_DGST);
			connection.setRequestProperty("Date", HDATE);
			System.out.println(HDATE);

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
}
