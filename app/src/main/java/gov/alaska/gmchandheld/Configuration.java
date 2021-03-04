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
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Configuration extends BaseActivity {

    private SharedPreferences sp;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static SharedPreferences.Editor editor;

    private ToggleButton autoUpdatebtn;
    private boolean alarmUp;
    private ToggleButton cameraToScannerbtn;

    private IntentIntegrator urlQrScan;
    private IntentIntegrator apiQrScan;

    private EditText hourInput;
    private EditText minuteInput;
    private String hour = "24";
    private String minute = "0";

    private EditText urlET;
    private EditText apiET;
    private String url;
    private String apiKey;

    @Override
    public void onRestart() {
        super.onRestart();
        this.recreate();
    }

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

        urlET = findViewById(R.id.urlET);
        urlET.requestFocus();
        apiET = findViewById(R.id.apiET);
        autoUpdatebtn = findViewById(R.id.autoUpdateBtn);
        hourInput = findViewById(R.id.hourET);
        minuteInput = findViewById(R.id.minuteET);

        cameraToScannerbtn = findViewById(R.id.cameraToScannerBtn);

        Boolean cameraOn = (sp.getBoolean("cameraOn", false));

        Button urlCameraBtn = findViewById(R.id.urlCameraBtn);
        Button apiCameraBtn = findViewById(R.id.apiCameraBtn);

        if (!cameraOn) {
            urlCameraBtn.setVisibility(View.GONE);
            apiCameraBtn.setVisibility(View.GONE);
        } else {
            urlQrScan = new IntentIntegrator(this);
            urlQrScan.setBeepEnabled(true);
            apiQrScan = new IntentIntegrator(this);
            apiQrScan.setBeepEnabled(true);
        }

        urlCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT <= 24) {
                    Intent intent = urlQrScan.createScanIntent();
                    startActivityForResult(intent, 1);
                } else {
                    Intent intent = new Intent(Configuration.this, CameraToScanner.class);
                    startActivityForResult(intent, 1);
                }
            }
        });

        apiCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT <= 24) {
                    Intent intent = apiQrScan.createScanIntent();
                    startActivityForResult(intent, 2);
                } else {
                    Intent intent = new Intent(Configuration.this, CameraToScanner.class);
                    startActivityForResult(intent, 2);
                }
            }
        });

        final Button updateButton = findViewById(R.id.updateBtn);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Update Button Pressed.");
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

                                SharedPreferences sharedPreferences = Configuration.this.getSharedPreferences("sharedPrefs", MODE_PRIVATE);
                                String hour = sharedPreferences.getString("updateHour", "24");
                                String minute = sharedPreferences.getString("updateMinute", "0");

                                Calendar alarmOffTime = Calendar.getInstance();
                                if (!hour.isEmpty()) {
                                    alarmOffTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
                                } else {
                                    alarmOffTime.set(Calendar.HOUR_OF_DAY, 24);
                                    hourInput = findViewById(R.id.hourET);
                                    hourInput.setText("24");
                                }

                                if (!minute.isEmpty()) {
                                    alarmOffTime.set(Calendar.MINUTE, Integer.parseInt(minute));
                                } else {
                                    alarmOffTime.set(Calendar.MINUTE, 0);
                                    minuteInput = findViewById(R.id.minuteET);
                                    minuteInput.setText("0");
                                }
                                alarmOffTime.set(Calendar.SECOND, 0);

                                if (alarmOffTime.before(Calendar.getInstance())) {
                                    alarmOffTime.add(Calendar.DATE, 1);
                                }

                                am.setRepeating(AlarmManager.RTC_WAKEUP, alarmOffTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, sender);
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

        apiET.addTextChangedListener(new TextWatcher() {
            SharedPreferences.Editor editor = sp.edit();

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editor.putString("apiText", getApiKey()).commit();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {
                saveData();
            }
        });
    }

    private void urlInputChangeWatcher() {

        urlET.addTextChangedListener(new TextWatcher() {
            SharedPreferences.Editor editor = sp.edit();

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                System.out.println(getUrl());
                editor.putString("urlText", getUrl()).commit();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {
                saveData();
            }
        });
    }

    public String getUrl() {
        urlET = findViewById(R.id.urlET);
        url = urlET.getText().toString();
        if (url.length() > 1 && url.charAt(url.length() - 1) != ('/')) {
            url = url + '/';
        }
        return url;
    }

    public String getApiKey() {
        apiET = findViewById(R.id.apiET);
        return apiET.getText().toString();
    }

    public void saveData() {
        SharedPreferences.Editor editor = sp.edit();


        editor.putString("urlText", getUrl());
        editor.putString("apiText", getApiKey());
        editor.putString("updateHour", hourInput.getText().toString());
        editor.putString("updateMinute", minuteInput.getText().toString());
        editor.putBoolean("cameraOn", cameraToScannerbtn.isChecked());
        editor.apply();
    }

    public void loadData() {
        url = sp.getString("urlText", "");
        apiKey = sp.getString("apiText", "");


        hour = sp.getString("updateHour", "24");
        minute = sp.getString("updateMinute", "0");

        autoUpdatebtn.setChecked(alarmUp);
        cameraToScannerbtn.setChecked(sp.getBoolean("cameraOn", false));
    }

    public void updateViews() {
        urlET.setText(url);
        apiET.setText(apiKey);
        hourInput.setText(hour);
        minuteInput.setText(minute);
        autoUpdatebtn.setChecked(alarmUp);
        cameraToScannerbtn.setChecked(sp.getBoolean("cameraOn", false));
    }

    private void hourInputChangeWatcher() {

        hourInput.addTextChangedListener(new TextWatcher() {
            SharedPreferences.Editor editor = sp.edit();

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editor.putString("updateHour", hourInput.getText().toString()).commit();
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
                editor.putString("updateMinute", minuteInput.getText().toString()).commit();
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
        if (Build.VERSION.SDK_INT <= 24) {
            switch (requestCode) {
                case 1: {
                    urlET = findViewById(R.id.urlET);
                    IntentResult result = IntentIntegrator.parseActivityResult(IntentIntegrator.REQUEST_CODE, resultCode, data);
                    urlET.setText(result.getContents());
                }
                break;
                case 2: {
                    apiET = findViewById(R.id.apiET);
                    IntentResult result = IntentIntegrator.parseActivityResult(IntentIntegrator.REQUEST_CODE, resultCode, data);
                    apiET.setText(result.getContents());
                }
                break;
            }
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