package gov.alaska.gmc_handheld_v2_simpleJSON;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class RemoteApiDownload {
	private Exception exception = null;
	private StringBuilder sb;
	private int responseCode;
	private String responseMsg;
	private String rawJson;
	private String queryOrDestination;
	private String addedContainerName;
	private String addedContainerRemark;
	private ArrayList<String> containerList;
	private Context context;
	SimpleDateFormat sdf;

	public static final String SHARED_PREFS = "sharedPrefs";

	@SuppressLint("SimpleDateFormat")
	public RemoteApiDownload(Context context){
		this.context = context;
		sdf = new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss zzz"
		);
	}

	public void setQueryOrDestination(String queryOrDestination){this.queryOrDestination = queryOrDestination;}
	public void setContainerList(ArrayList<String> containerList){this.containerList = containerList;}

	public void setAddedContainerName(String addedContainerName) {this.addedContainerName = addedContainerName;}

	public void setAddedContainerRemark(String addedContainerRemark) {this.addedContainerRemark = addedContainerRemark;}

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
		SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
		String url = sharedPreferences.getString("urlText", "");

		try {
			Date date = new Date();
			String HDATE = getDateFormat().format(date);

			String QUERYPARAM = null;

			switch (context.getClass().getSimpleName()){
				case "Lookup":
				case "LookupDisplay":{
					String query = null;
					try {
						query = URLEncoder.encode(queryOrDestination, "utf-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					QUERYPARAM = "barcode=" + query;
					url = url + "inventory.json?barcode=" + queryOrDestination;
					break;
				}
				case "Summary":
				case "SummaryDisplay":{
					String query = null;
					try {
						query = URLEncoder.encode(queryOrDestination, "utf-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					QUERYPARAM = "barcode=" + query;
					url = url + "summary.json?barcode=" + queryOrDestination;
					break;

				}
				case "MoveDisplay":{
					String query = null;
					try {
						query = URLEncoder.encode(queryOrDestination, "utf-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					query = "d=" + query + containersToMoveStr(containerList);

					QUERYPARAM = query;
					url = url +"move.json?" +  query;
					break;
				}
				case "AddContainer":{
					String query = null;
					String name = null;
					String remark = null;
					try {
						query = URLEncoder.encode(queryOrDestination, "utf-8");
						name = URLEncoder.encode(addedContainerName, "utf-8");
						remark = URLEncoder.encode(addedContainerRemark, "utf-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}

					query = "barcode=" + query + "&name=" + name + "&remark=" + remark ;

					QUERYPARAM = query;
					url = url +"addcontainer.json?" +  query;

					break;
				}
			}

			String message = HDATE + "\n" + QUERYPARAM;

			String APIKEY = sharedPreferences.getString("apiText", "");

			String AUTH_DGST = getDGST(APIKEY, message);

			URL myURL = new URL(url);
			connection = (HttpURLConnection) myURL.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Authorization", "BASE64-HMAC-SHA256 " + AUTH_DGST);
			connection.setRequestProperty("Date", HDATE);

			connection.setReadTimeout(10 * 1000);
			connection.setConnectTimeout(5 * 1000);

			connection.connect();

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
				}

				if (sb.length() <= 2) {
					exception = new Exception("No results found.\n\nIs the barcode correct? " + queryOrDestination);
				} else {
					rawJson = sb.toString();
				}

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
			exception = new Exception(String.valueOf(sb));
		} catch (IOException e) {
			exception = e;
		}
	}

	public SimpleDateFormat getDateFormat() {
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

	public String containersToMoveStr(ArrayList<String> list) {
		String delim = "&c=";

		StringBuilder sb = new StringBuilder();

		sb.append(delim);
		int i = 0;
		while (i < list.size() - 1) {

			try {
				sb.append(URLEncoder.encode(list.get(i), "utf-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			sb.append(delim);
			i++;
		}
		sb.append(list.get(i));
		return sb.toString();
	}
}
