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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import android.widget.ToggleButton;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Configuration extends BaseActivity {

	private SharedPreferences sp;
	public static final String SHARED_PREFS = "sharedPrefs";
	public static final String URL_TEXT = "urlText";
	public static final String API_TEXT = "apiText";
	public static final String UPDATE_HOUR = "updateHour";
	public static final String UPDATE_MINUTE = "updateMinute";
	public static final String HOUR_TEXT = "updateHour";
	public static final String MINUTE_TEXT = "updateMinute";

	private ToggleButton autoUpdatebtn;
	private boolean alarmUp;
	private ToggleButton cameraToScannerbtn;
	public static final String CAMERA_ON = "cameraOn";

	private EditText hourInput;
	private EditText minuteInput;
	private String hour = "24";
	private String minute = "0";

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

		sp = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);

		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setBackgroundColor(Color.parseColor("#ff567b95"));
		setSupportActionBar(toolbar);

		Date buildDate = new Date(BuildConfig.TIMESTAMP);
		TextView buildDateTV = findViewById(R.id.buildDateTV);
		buildDateTV.setText(DateFormat.getDateTimeInstance().format(buildDate));

		urlInput = findViewById(R.id.urlET);
		urlInput.requestFocus();
		apiInput = findViewById(R.id.apiET);
		autoUpdatebtn = findViewById(R.id.autoUpdateBtn);
		hourInput = findViewById(R.id.hour_editText);
		minuteInput = findViewById(R.id.minute_editText);

		cameraToScannerbtn = findViewById(R.id.cameraToScannerBtn);

		SharedPreferences sp = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		Boolean cameraOn = (sp.getBoolean("cameraOn", false));

		Button urlCameraBtn = findViewById(R.id.urlCameraBtn);
		Button apiCameraBtn = findViewById(R.id.apiCameraBtn);

		if(!cameraOn){
			urlCameraBtn.setVisibility(View.GONE);
			apiCameraBtn.setVisibility(View.GONE);
		}

		urlCameraBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(Configuration.this, CameraToScanner.class);
				startActivityForResult(intent, 1);
			}
		});

		apiCameraBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(Configuration.this, CameraToScanner.class);
				startActivityForResult(intent, 2);
			}
		});

		final Button updateButton = findViewById(R.id.updateBtn);
		updateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateAPK();
			}
		});

		hourInputChangeWatcher();
		minuteInputChangeWatcher();
		urlInputChangeWatcher();
		apiInputChangeWatcher();
		autoUpdateChangeWatcher();

		loadData();

		cameraToScannerChangeWatcher();
		loadData();
		updateViews();


	}

	private void cameraToScannerChangeWatcher() {

		cameraToScannerbtn.setOnCheckedChangeListener(
				new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton compoundButton,
												 boolean isChecked) {
						SharedPreferences.Editor editor = sp.edit();
						if (isChecked) {
							editor.putBoolean("cameraOn", true).commit();
						} else {
							editor.putBoolean("cameraOn", false).commit();
						}
						saveData();
					}
				});
	}

	private void autoUpdateChangeWatcher() {
		final Intent intent = new Intent(Configuration.this, UpdateBroadcastReceiver.class);
		alarmUp = (PendingIntent.getBroadcast(Configuration.this, 2, intent, 0) != null);
		autoUpdatebtn.setChecked(alarmUp);
		autoUpdatebtn.setOnCheckedChangeListener(
				new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton compoundButton,
												 boolean isChecked) {
						if (isChecked) {
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
								if (!hour.isEmpty()) {
									alarmOffTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
								} else {
									alarmOffTime.set(Calendar.HOUR_OF_DAY, 24);
									hourInput = findViewById(R.id.hour_editText);
									hourInput.setText("24");
								}

								if (!minute.isEmpty()) {
									alarmOffTime.set(Calendar.MINUTE, Integer.parseInt(minute));
								} else {
									alarmOffTime.set(Calendar.MINUTE, 0);
									minuteInput = findViewById(R.id.minute_editText);
									minuteInput.setText("0");
								}
								alarmOffTime.set(Calendar.SECOND, 0);

								if (alarmOffTime.before(Calendar.getInstance())) {
									alarmOffTime.add(Calendar.DATE, 1);
								}

								am.setRepeating(AlarmManager.RTC_WAKEUP, alarmOffTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, sender);
//								long interval = 60 * 1000;
//								am.setRepeating(AlarmManager.RTC_WAKEUP, alarmOffTime.getTimeInMillis(), interval, sender);
								saveData();
							}
						} else {
							Intent intent = new Intent(Configuration.this, UpdateBroadcastReceiver.class);
							PendingIntent sender = PendingIntent.getBroadcast(Configuration.this, 2, intent, 0);
							AlarmManager am = (AlarmManager) Configuration.this.getSystemService(Context.ALARM_SERVICE);
							if (am != null) {
								am.cancel(sender);
							}
							saveData();
						}
					}
				});
	}

	private void apiInputChangeWatcher() {

		apiInput.addTextChangedListener(new TextWatcher() {
			SharedPreferences.Editor editor = sp.edit();

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				editor.putString(API_TEXT, getApiKey()).commit();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				saveData();
			}
		});
	}

	private void urlInputChangeWatcher() {

		urlInput.addTextChangedListener(new TextWatcher() {
			SharedPreferences.Editor editor = sp.edit();

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				editor.putString(URL_TEXT, getUrl()).commit();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				saveData();
			}
		});
	}

	public String getUrl() {
		urlInput = findViewById(R.id.urlET);
		url = urlInput.getText().toString();
		if (url.length() > 1 && url.charAt(url.length() - 1) != ('/')) {
			url = url + '/';
		}
		return url;
	}

	public String getApiKey() {
		apiInput = findViewById(R.id.apiET);
		return apiInput.getText().toString();
	}

	public void saveData() {
		SharedPreferences.Editor editor = sp.edit();

		editor.putString(URL_TEXT, getUrl());
		editor.putString(API_TEXT, getApiKey());
		editor.putString(UPDATE_HOUR, hourInput.getText().toString());
		editor.putString(UPDATE_MINUTE, minuteInput.getText().toString());
		editor.putBoolean(CAMERA_ON, cameraToScannerbtn.isChecked());
		editor.apply();
	}

	public void loadData() {
		url = sp.getString(URL_TEXT, "");
		apiKey = sp.getString(API_TEXT, "");

		hour = sp.getString(UPDATE_HOUR, "24");
		minute = sp.getString(UPDATE_MINUTE, "0");

		autoUpdatebtn.setChecked(alarmUp);
		cameraToScannerbtn.setChecked(sp.getBoolean(CAMERA_ON, false));
	}

	public void updateViews() {
		urlInput.setText(url);
		apiInput.setText(apiKey);
		hourInput.setText(hour);
		minuteInput.setText(minute);
		autoUpdatebtn.setChecked(alarmUp);
		cameraToScannerbtn.setChecked(sp.getBoolean(CAMERA_ON, false));
	}

	private void hourInputChangeWatcher() {

		hourInput.addTextChangedListener(new TextWatcher() {
			SharedPreferences.Editor editor = sp.edit();

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				editor.putString(UPDATE_HOUR, hourInput.getText().toString()).commit();
				autoUpdatebtn.setChecked(false);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				autoUpdatebtn.setChecked(true);
			}
		});
	}

	private void minuteInputChangeWatcher() {
		minuteInput.addTextChangedListener(new TextWatcher() {
			SharedPreferences.Editor editor = sp.edit();

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				editor.putString(UPDATE_MINUTE, minuteInput.getText().toString()).commit();
				autoUpdatebtn.setChecked(false);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				autoUpdatebtn.setChecked(true);
			}
		});
	}


	public void updateAPK() {
		new UpdateCheckLastModifiedDate(this).execute();
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		if (Build.VERSION.SDK_INT < 24) {
//			IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
//			editText.setText(result.getContents());
		} else {
			switch (requestCode) {
				case 1: {
					if (resultCode == CommonStatusCodes.SUCCESS) {
						Barcode barcode = data.getParcelableExtra("barcode");
						EditText edit_text = findViewById(R.id.urlET);
						if (barcode != null) {
							edit_text.setText(barcode.displayValue);
						}
					}
					break;
				}
				case 2: {
					if (resultCode == CommonStatusCodes.SUCCESS) {
						Barcode barcode = data.getParcelableExtra("barcode");
						EditText edit_text = findViewById(R.id.apiET);
						if (barcode != null) {
							edit_text.setText(barcode.displayValue);
						}
					}
					break;
				}
				default:
					super.onActivityResult(requestCode, resultCode, data);
			}
		}
	}
}