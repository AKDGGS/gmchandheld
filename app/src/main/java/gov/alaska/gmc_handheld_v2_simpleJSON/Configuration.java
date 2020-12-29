package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Configuration extends BaseActivity {

	public static final String SHARED_PREFS = "sharedPrefs";
	public static final String URL_TEXT = "urlText";
	public static final String API_TEXT = "apiText";

	private EditText urlInput;
	private EditText apiInput;
	private String url;
	private String apiKey;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.configuration);

		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setBackgroundColor(Color.parseColor("#ff567b95"));
		setSupportActionBar(toolbar);

		Date buildDate = new Date(BuildConfig.TIMESTAMP);
		TextView buildDateTV = findViewById(R.id.buildDateTV);
		buildDateTV.setText(DateFormat.getDateTimeInstance().format(buildDate));

		urlInput = findViewById(R.id.url_editText);
		apiInput = findViewById(R.id.api_editText);

		final Button updateButton = findViewById(R.id.updateBtn);
		updateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateAPK();
			}
		});

		final Button saveButton = findViewById(R.id.saveBtn);
		// onClickListener listens if the save button is clicked
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveData();
			}
		});

		loadData();
		updateViews();
	}

	public String getUrl() {
		urlInput = findViewById(R.id.url_editText);

		url = urlInput.getText().toString();
		if (url.length() > 1 && url.charAt(url.length() - 1) != ('/')) {
			url = url + '/';
		}
		return url;
	}

	public String getApiKey() {
		apiInput = findViewById(R.id.api_editText);
		return apiInput.getText().toString();
	}

	public void saveData() {
		SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();

		if ("".equals(getUrl())) {
			Toast.makeText(this, "You did not enter an URL.", Toast.LENGTH_LONG).show();
		} else {
			getUrl();
			if ("".equals(getApiKey())) {
				Toast.makeText(this, "You did not enter an API key.", Toast.LENGTH_LONG).show();
			} else {
				getApiKey();
				editor.putString(URL_TEXT, getUrl());
				editor.putString(API_TEXT, getApiKey());

				editor.apply();
				Toast.makeText(this, "Changes to configuration saved.", Toast.LENGTH_LONG).show();

				Intent intent = new Intent(this, Lookup.class);
				startActivity(intent);
			}
		}
	}

	public void loadData() {
		SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		url = sharedPreferences.getString(URL_TEXT, "");
		apiKey = sharedPreferences.getString(API_TEXT, "");
	}

	public void updateViews() {
		urlInput.setText(url);
		apiInput.setText(apiKey);
	}

	public void updateAPK() {
		final String fileUrl = "http://maps.dggs.alaska.gov/gmcdev/app/currentVersion.txt";

		final Context mContext = this;
		final AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

		ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (!(cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected())) {
			alertDialog.setMessage("Is the device connected to the internet/network?  " +
					"Check if the connection has been lost.");
			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			View layout = inflater.inflate(R.layout.lookup_error_display, (ViewGroup) ((Activity) mContext).findViewById(R.id.lookup_error_root));
			alertDialog.setView(layout);
			alertDialog.setPositiveButton("Dimiss",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});

			AlertDialog alert = alertDialog.create();
			alert.setCanceledOnTouchOutside(false);
			alert.show();

		} else {
			new DownloadFileFromURL().execute(fileUrl);
		}
	}

	class DownloadFileFromURL extends AsyncTask<String, String, String> {
		StringBuilder sb;
		DateFormat simple = new SimpleDateFormat("yyyyMMddHHmm");
		String currentBuildTime = simple.format(new Date(BuildConfig.TIMESTAMP));
		int responseCode = 200;
		int responseCode2 = 200;

		@Override
		protected String doInBackground(String... f_url) {
			int count;
			try {
				URL url = new URL(f_url[0]);
				URLConnection connection = url.openConnection();

//				connection.setConnectTimeout(10000);
//				connection.setReadTimeout(10000);
				try {
					connection.connect();
					HttpURLConnection.setFollowRedirects(false);
					HttpURLConnection con = (HttpURLConnection) new URL(f_url[0]).openConnection();
					con.setRequestMethod("HEAD");
					System.out.println(con.getResponseCode());
					if (con.getResponseCode() == 200) {

						System.out.println("response: 200");
						String version = "";
						sb = new StringBuilder();
						InputStream input = new BufferedInputStream(url.openStream(), 8192);
						BufferedReader reader = new BufferedReader(new InputStreamReader(input));
						while (true) {
							try {
								if ((version = reader.readLine()) == null) break;
							} catch (IOException e) {
								e.printStackTrace();
							}
							sb.append(version);
						}
						input.close();


						if ((responseCode == 200) && (Double.parseDouble(currentBuildTime) < Double.parseDouble(sb.toString()))) {
							String fileUrl = "http://maps.dggs.alaska.gov/gmcdev/app/app-release-1.apk";
							try {
								con = (HttpURLConnection) new URL(fileUrl).openConnection();
								con.setRequestMethod("HEAD");
								System.out.println("app-release-1: " + con.getResponseCode());
								if (con.getResponseCode() == 200) {
									System.out.println("app-release-1: " + con.getResponseCode());
									url = new URL(fileUrl);
									connection = url.openConnection();
//				connection.setConnectTimeout(10000);
//				connection.setReadTimeout(10000);
									connection.connect();
									input = new BufferedInputStream(url.openStream(), 8192);
									OutputStream output = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/app-release-1.apk");
									byte data[] = new byte[1024];
									long total = 0;

									while ((count = input.read(data)) != -1) {
										total += count;
										// writing data to file
										output.write(data, 0, count);
									}

									output.flush();
									output.close();
									input.close();
								} else {
									System.out.println("Inner file doesn't exist");
									responseCode2 = con.getResponseCode();
								}
							} catch (IOException e) {
								Log.e("Error: ", e.getMessage());
							}
						}
					}

				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String fileUrl) {
			Intent intent = new Intent();
			File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/app-release-1.apk");
			System.out.println((responseCode == 200) && (responseCode2 == 200));
			if ((responseCode == 200) && (responseCode2 == 200) && (Double.parseDouble(currentBuildTime) < Double.parseDouble(sb.toString()))) {
				Uri uriFile = Uri.fromFile(file);
				Toast.makeText(getBaseContext(), "Update available....Installing.", Toast.LENGTH_SHORT).show();
//				 Intent to open apk
				intent = new Intent(Intent.ACTION_INSTALL_PACKAGE, uriFile);
				intent.setDataAndType(uriFile, "application/vnd.android.package-archive");
				intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			} else {
				Toast.makeText(getBaseContext(), "No update available.", Toast.LENGTH_LONG).show();
			}
		}
	}
}