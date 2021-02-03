package gov.alaska.gmchandheld;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
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
import android.widget.ToggleButton;

import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Configuration extends BaseActivity {

	public static final String SHARED_PREFS = "sharedPrefs";
	public static final String URL_TEXT = "urlText";
	public static final String API_TEXT = "apiText";
	public static final String UPDATE_HOUR = "updateHour";
	public static final String UPDATE_MINUTE = "updateMinute";

	private ToggleButton autoUpdatebtn;

//	public static final String UPDATE_SWITCH_TEXT = "updateSwitchText";
//	private SwitchCompat updateSwitch;
//	private boolean updateSwitchSavedState;

	private EditText hourInput;
	private EditText minuteInput;
	private String hour;
	private String minute;

	private EditText urlInput;
	private EditText apiInput;
	private String url;
	private String apiKey;


	public static final String HOUR_TEXT = "updateHour";
	public static final String MINUTE_TEXT = "updateMinute";

//	private boolean switchStateOn = false;

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
		autoUpdatebtn = findViewById(R.id.autoUpdateBtn);
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

		final Intent intent = new Intent(Configuration.this, UpdateBroadcastReceiver.class);
		boolean alarmUp = (PendingIntent.getBroadcast(Configuration.this, 2, intent, 0) != null);
		autoUpdatebtn.setChecked(alarmUp);

		autoUpdatebtn.setOnCheckedChangeListener(
				new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton compoundButton,
												 boolean isChecked) {

						if(isChecked){
							PendingIntent sender = PendingIntent.getBroadcast(Configuration.this, 2, intent, 0);
							AlarmManager am = (AlarmManager) Configuration.this.getSystemService(Context.ALARM_SERVICE);

							if (am != null) {
								String strTime = "2021-01-20 14:07:00";
								DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
								Date d = null;
								try {
									d = dateFormat.parse(strTime);
								} catch (ParseException e) {
									e.printStackTrace();
								}

								SharedPreferences sharedPreferences = Configuration.this.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
								String hour = sharedPreferences.getString(HOUR_TEXT, "24");
								String minute = sharedPreferences.getString(MINUTE_TEXT, "0");

								Calendar alarmOffTime = Calendar.getInstance();
								alarmOffTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
								alarmOffTime.set(Calendar.MINUTE, Integer.parseInt(minute));
								alarmOffTime.set(Calendar.SECOND, 0);

								if (alarmOffTime.before(Calendar.getInstance())) {
									alarmOffTime.add(Calendar.DATE, 1);
								}

								am.setRepeating(AlarmManager.RTC_WAKEUP, alarmOffTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, sender);
							}
						} else {
							Intent intent = new Intent(Configuration.this, UpdateBroadcastReceiver.class);
							PendingIntent sender = PendingIntent.getBroadcast(Configuration.this, 2, intent, 0);
							AlarmManager am = (AlarmManager) Configuration.this.getSystemService(Context.ALARM_SERVICE);
							if (am != null) {
								am.cancel(sender);
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

		SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		getApiKey();
		editor.putString(URL_TEXT, getUrl());
		editor.putString(API_TEXT, getApiKey());
//		editor.putBoolean(UPDATE_SWITCH_TEXT, updateSwitch.isChecked());

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
//		updateSwitchSavedState = sharedPreferences.getBoolean(UPDATE_SWITCH_TEXT, false);
		hour = sharedPreferences.getString(UPDATE_HOUR, "");
		minute = sharedPreferences.getString(UPDATE_MINUTE, "");
	}

	public void updateViews() {
		urlInput.setText(url);
		apiInput.setText(apiKey);
//		updateSwitch.setChecked(updateSwitchSavedState);
		hourInput.setText(hour);
		minuteInput.setText(minute);
	}


	public void updateAPK() {
		new UpdateCheckLastModifiedDate(this).execute();
	}
}