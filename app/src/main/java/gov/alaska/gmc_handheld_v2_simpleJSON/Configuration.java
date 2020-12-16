package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.text.DateFormat;
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

		TextView buildDateTV = findViewById(R.id.buildDateTV);
		Date buildDate = new Date(BuildConfig.TIMESTAMP);
		buildDateTV.setText(DateFormat.getDateTimeInstance().format(buildDate));

		urlInput = findViewById(R.id.url_editText);
		apiInput = findViewById(R.id.api_editText);

		final Button updateButton = findViewById(R.id.updateButton);

		updateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				downloadInstallApk();
			}
		});

		final Button saveButton = findViewById(R.id.saveButton);
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
		}else {
			getUrl();
			if("".equals(getApiKey())) {
				Toast.makeText(this, "You did not enter an API key.", Toast.LENGTH_LONG).show();
			}else{
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

	public void downloadInstallApk(){

		String file = "app-release-1.apk";
		DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
		final Uri downloadUri = Uri.parse("http://maps.dggs.alaska.gov/gmcdev/app/app-release.apk");

		final File outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "app-release-1.apk");

		DownloadManager.Request request = new DownloadManager.Request(downloadUri);

		request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
				.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS + File.separator, file)
				.setTitle(file).setDescription("apk")
				.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

		dm.enqueue(request);


		//set BroadcastReceiver to install app when .apk is downloaded
		BroadcastReceiver onComplete = new BroadcastReceiver() {
			public void onReceive(Context ctxt, Intent intent) {
				Uri uriFile = Uri.fromFile(outputFile);
				// Intent to open apk
				intent = new Intent(Intent.ACTION_INSTALL_PACKAGE, downloadUri);
				intent.setDataAndType(uriFile, "application/vnd.android.package-archive");
				intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				startActivity(intent);

				unregisterReceiver(this);
				finish();
			}
		};
		//register receiver for when .apk download is compete
		registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));


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
}