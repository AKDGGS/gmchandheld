package gov.alaska.gmchandheld;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
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

	public static final String UPDATE_SWITCH_TEXT = "updateSwitchText";
	private SwitchCompat updateSwitch;
	private boolean updateSwitchOnOff;

	private EditText urlInput;
	private EditText apiInput;
	private String url;
	private String apiKey;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
		StrictMode.setVmPolicy(builder.build());
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			builder.detectFileUriExposure();
		}

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
		updateSwitch = findViewById(R.id.autoUpdateSwitch);

		final Button updateButton = findViewById(R.id.updateBtn);
		updateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateAPK();
			}
		});

		final Button saveButton = findViewById(R.id.saveBtn);
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
		getApiKey();
		editor.putString(URL_TEXT, getUrl());
		editor.putString(API_TEXT, getApiKey());
		editor.putBoolean(UPDATE_SWITCH_TEXT, updateSwitch.isChecked());

		editor.apply();
		Toast.makeText(this, "Changes saved.", Toast.LENGTH_LONG).show();

		Intent intent = new Intent(this, Lookup.class);
		startActivity(intent);
	}

	public void loadData() {
		SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		url = sharedPreferences.getString(URL_TEXT, "");
		apiKey = sharedPreferences.getString(API_TEXT, "");
		updateSwitchOnOff = sharedPreferences.getBoolean(UPDATE_SWITCH_TEXT, false);
	}

	public void updateViews() {
		urlInput.setText(url);
		apiInput.setText(apiKey);
		updateSwitch.setChecked(updateSwitchOnOff);
	}


	public void updateAPK() {
		new UpdateCheckLastModifiedDate(this).execute();
	}
}