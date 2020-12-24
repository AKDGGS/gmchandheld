package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
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

		final Button saveButton = findViewById(R.id.saveButton);
		// onClickListener listens if the save button is clicked
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveData();
			}
		});

		final Button updateButton = findViewById(R.id.updateBtn);
		updateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateAPK();
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

		Button updateBtn = findViewById(R.id.updateBtn);

		updateBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				new DownloadFileFromURL().execute(fileUrl);
			}
		});
	}


	class DownloadFileFromURL extends AsyncTask<String, String, String> {
		StringBuilder sb;
		DateFormat simple = new SimpleDateFormat("yyyyMMddHHmm");
		String currentBuildTime = simple.format(new Date(BuildConfig.TIMESTAMP));

		@Override
		protected String doInBackground(String... f_url) {
			int count;
			try {
				URL url = new URL(f_url[0]);
				URLConnection connection = url.openConnection();

//				connection.setConnectTimeout(10000);
//				connection.setReadTimeout(10000);
				connection.connect();
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
				
				if (Double.parseDouble(currentBuildTime) < Double.parseDouble(sb.toString())) {
					String fileUrl = "http://maps.dggs.alaska.gov/gmcdev/app/app-release-1.apk";
					try {
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

					} catch (Exception e) {
						Log.e("Error: ", e.getMessage());
					}
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
			if (Double.parseDouble(currentBuildTime) < Double.parseDouble(sb.toString())) {
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