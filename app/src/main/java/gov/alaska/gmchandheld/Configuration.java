package gov.alaska.gmchandheld;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class Configuration extends BaseActivity {
    private ToggleButton autoUpdateBtn;
    private ToggleButton cameraToScannerBtn;
    private IntentIntegrator urlQrScan;
    private IntentIntegrator apiQrScan;
    private EditText hourInput;
    private EditText minuteInput;
    private String hour;
    private String minute;
    private EditText urlET;
    private EditText apiET;
    private String url;

    @Override
    public int getLayoutResource() {
        return R.layout.configuration;
    }

    @Override
    public void onRestart() {
        super.onRestart();
        updateViews();
        loadData();
        saveData();
        this.recreate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        urlET = findViewById(R.id.urlET);
        urlET.requestFocus();
        if (urlET.getText().toString().isEmpty()) {
            enableTSL(this);
        }
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            builder.detectFileUriExposure();
        }
        Date buildDate = new Date(BuildConfig.TIMESTAMP);
        TextView buildDateTV = findViewById(R.id.buildDateTV);
        buildDateTV.setText(DateFormat.getDateTimeInstance().format(buildDate));
        apiET = findViewById(R.id.apiET);
        autoUpdateBtn = findViewById(R.id.autoUpdateBtn);
        hourInput = findViewById(R.id.hourET);
        minuteInput = findViewById(R.id.minuteET);
        cameraToScannerBtn = findViewById(R.id.cameraToScannerBtn);
        boolean cameraOn = (BaseActivity.sp.getBoolean("cameraOn", false));
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
                    updateAPK();
            }
        });
        updateViews();
        hourInputChangeWatcher();
        minuteInputChangeWatcher();
        urlInputChangeWatcher();
        apiInputChangeWatcher();
        autoUpdateChangeWatcher();
        cameraToScannerChangeWatcher();
        updateViews();
        loadData();
        saveData();
    }

    private void cameraToScannerChangeWatcher() {
        cameraToScannerBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton,boolean isChecked) {
                if (isChecked) {
                    BaseActivity.editor.putBoolean("cameraOn", true).apply();
                } else {
                    BaseActivity.editor.putBoolean("cameraOn", false).commit();
                }
            }
        });
    }

    private void autoUpdateChangeWatcher() {
        final Intent intent = new Intent(Configuration.this,
                UpdateBroadcastReceiver.class);
        boolean alarmUp = (PendingIntent.getBroadcast(Configuration.this, 2, intent,
                0) != null);
        autoUpdateBtn.setChecked(alarmUp);
        autoUpdateBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    PendingIntent sender = PendingIntent.getBroadcast(
                            Configuration.this, 2, intent, 0);
                    AlarmManager am = (AlarmManager) Configuration.this
                            .getSystemService(Context.ALARM_SERVICE);
                    if (am != null) {
                        hour = BaseActivity.sp.getString("updateHour", "24");
                        minute = BaseActivity.sp.getString("updateMinute", "0");
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
                        BaseActivity.editor.putBoolean("alarmOn", true);
                        am.setRepeating(AlarmManager.RTC_WAKEUP,
                                alarmOffTime.getTimeInMillis(),
                                AlarmManager.INTERVAL_DAY, sender);
                    }
                } else {
                    Intent intent = new Intent(Configuration.this,
                            UpdateBroadcastReceiver.class);
                    PendingIntent sender = PendingIntent.getBroadcast(
                            Configuration.this, 2, intent, 0);
                    AlarmManager am = (AlarmManager) Configuration.this
                            .getSystemService(Context.ALARM_SERVICE);
                    if (am != null) {
                        am.cancel(sender);
                    }
                    BaseActivity.editor.putBoolean("alarmOn", true);
                }
            }
        });
    }

    private void urlInputChangeWatcher() {
        urlET.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                BaseActivity.editor.putString("urlText", getUrl()).apply();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    public String getApiKey() {
        apiET = findViewById(R.id.apiET);
        return apiET.getText().toString();
    }

    private void apiInputChangeWatcher() {
        apiET.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                BaseActivity.apiKeyBase = getApiKey();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void afterTextChanged(Editable s) { }
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

    public void saveData() {
        BaseActivity.editor.putString("urlText", getUrl());
        BaseActivity.editor.putString("updateHour", hourInput.getText().toString());
        BaseActivity.editor.putString("updateMinute", minuteInput.getText().toString());
        BaseActivity.editor.putBoolean("cameraOn", cameraToScannerBtn.isChecked());
        BaseActivity.editor.putBoolean("alarmOn", autoUpdateBtn.isChecked());
        BaseActivity.editor.apply();
    }

    public void loadData() {
        url = sp.getString("urlText", "");
        hour = sp.getString("updateHour", "24");
        minute = sp.getString("updateMinute", "0");
        autoUpdateBtn.setChecked(sp.getBoolean("alarmOn", true));
        cameraToScannerBtn.setChecked(sp.getBoolean("cameraOn", false));
    }

    public void updateViews() {
        urlET.setText(BaseActivity.sp.getString("urlText", ""));
        apiET.setText(BaseActivity.apiKeyBase);
        hourInput.setText(sp.getString("updateHour", "24"));
        minuteInput.setText(sp.getString("updateMinute", "0"));
        autoUpdateBtn.setChecked(sp.getBoolean("alarmOn", true));
        cameraToScannerBtn.setChecked(sp.getBoolean("cameraOn", false));
    }

    private void hourInputChangeWatcher() {
        hourInput.addTextChangedListener(new TextWatcher() {
            String strBefore;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                BaseActivity.editor.putString("updateHour", hourInput.getText().toString()).apply();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                strBefore = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().equals(strBefore)){
                    autoUpdateBtn.setChecked(false);
                    autoUpdateBtn.setChecked(true);
                }
            }
        });
    }

    private void minuteInputChangeWatcher() {
        minuteInput.addTextChangedListener(new TextWatcher() {
            String strBefore;
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                BaseActivity.editor.putString("updateMinute", minuteInput.getText().toString()).apply();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                strBefore = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().equals(strBefore)){
                    autoUpdateBtn.setChecked(false);
                    autoUpdateBtn.setChecked(true);
                }
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