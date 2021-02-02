package gov.alaska.gmchandheld;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import java.text.DateFormat;
import java.util.Date;

public class Configuration extends BaseActivity {

	public static final String SHARED_PREFS = "sharedPrefs";
	public static final String URL_TEXT = "urlText";
	public static final String API_TEXT = "apiText";
	public static final String UPDATE_HOUR = "updateHour";
	public static final String UPDATE_MINUTE = "updateMinute";

	public static final String UPDATE_SWITCH_TEXT = "updateSwitchText";
	private SwitchCompat updateSwitch;
	private boolean updateSwitchSavedState;

	private EditText hourInput;
	private EditText minuteInput;
	private String hour;
	private String minute;

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


		System.out.println("Switch State: ****************** " + updateSwitchSavedState);


		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setBackgroundColor(Color.parseColor("#ff567b95"));
		setSupportActionBar(toolbar);

		Date buildDate = new Date(BuildConfig.TIMESTAMP);
		TextView buildDateTV = findViewById(R.id.buildDateTV);
		buildDateTV.setText(DateFormat.getDateTimeInstance().format(buildDate));

		urlInput = findViewById(R.id.url_editText);
		apiInput = findViewById(R.id.api_editText);
		updateSwitch = findViewById(R.id.autoUpdateSwitch);
		hourInput = findViewById(R.id.hour_editText);
		minuteInput = findViewById(R.id.minute_editText);

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

		updateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean updateSwitchIsChecked) {
				UpdateAlarmHandler updateAlarmHandler = new UpdateAlarmHandler(Configuration.this);
				System.out.println("Switch State: " + updateSwitchSavedState);
				if (updateSwitchIsChecked != updateSwitchSavedState) {
					if (updateSwitchIsChecked) {
						updateAlarmHandler.cancelAlarmManager();
						updateAlarmHandler.setAlarmManager();
					} else {
						updateAlarmHandler.cancelAlarmManager();
					}
				}
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
		System.out.println("Switch State: " + updateSwitchSavedState);

		SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		getApiKey();
		editor.putString(URL_TEXT, getUrl());
		editor.putString(API_TEXT, getApiKey());
		editor.putBoolean(UPDATE_SWITCH_TEXT, updateSwitch.isChecked());

		editor.putString(UPDATE_HOUR, hourInput.getText().toString());
		editor.putString(UPDATE_MINUTE, minuteInput.getText().toString());

		editor.apply();
		Toast.makeText(this, "Changes saved.", Toast.LENGTH_LONG).show();

		Intent intent = new Intent(this, Lookup.class);
		startActivity(intent);
	}

	public void loadData() {
		SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		url = sharedPreferences.getString(URL_TEXT, "");
		apiKey = sharedPreferences.getString(API_TEXT, "");
		updateSwitchSavedState = sharedPreferences.getBoolean(UPDATE_SWITCH_TEXT, false);
		hour = sharedPreferences.getString(UPDATE_HOUR, "");
		minute = sharedPreferences.getString(UPDATE_MINUTE, "");
	}

	public void updateViews() {
		urlInput.setText(url);
		apiInput.setText(apiKey);
		updateSwitch.setChecked(updateSwitchSavedState);
		hourInput.setText(hour);
		minuteInput.setText(minute);
	}


	public void updateAPK() {
		new UpdateCheckLastModifiedDate(this).execute();
	}
}