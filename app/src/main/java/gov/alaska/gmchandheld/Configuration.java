package gov.alaska.gmchandheld;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Observer;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class Configuration extends BaseActivity implements RemoteAPIDownloadCallback {
    private ToggleButton autoUpdateBtn, cameraToScannerBtn;
    private EditText updateIntervalET, urlET;

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
    protected void onPause() {
        super.onPause();
        saveData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        urlET = findViewById(R.id.urlET);
        urlET.requestFocus();

        // KeyListener listens if enter is pressed
        urlET.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                if (!urlET.getText().toString().startsWith("https")) {
                    Toast.makeText(Configuration.this, "The URL must use https.",
                            Toast.LENGTH_SHORT).show();
                    urlET.requestFocus();
                    urlET.selectAll();
                }
                return true;
            }
            return false;
        });
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            builder.detectFileUriExposure();
        }
        Date buildDate = new Date(BuildConfig.TIMESTAMP);
        TextView buildDateTV = findViewById(R.id.buildDateTV);
        buildDateTV.setText(DateFormat.getDateTimeInstance().format(buildDate));


        updateAvailable.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean == true) {
                    System.out.println("Update available");
                    downloadingAlert();
                } else {
                    System.out.println("No update is available");
                }
            }
        });

        autoUpdateBtn = findViewById(R.id.autoUpdateBtn);
        updateIntervalET = findViewById(R.id.updateIntervalET);
        cameraToScannerBtn = findViewById(R.id.cameraToScannerBtn);
        Button urlCameraBtn = findViewById(R.id.urlCameraBtn);
        if (!sp.getBoolean("cameraOn", false)) {
            urlCameraBtn.setVisibility(View.GONE);
        } else {
            qrScan = new IntentIntegrator(this);
            qrScan.setBeepEnabled(true);
        }

        urlCameraBtn.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT <= 24) {
                BaseActivity.intent = qrScan.createScanIntent();
            } else {
                BaseActivity.intent = new Intent(Configuration.this, CameraToScanner.class);
            }
            startActivityForResult(BaseActivity.intent, 1);
        });

        final Button updateBtn = findViewById(R.id.updateBtn);
        updateBtn.setOnClickListener(v -> updateAPK());
        updateViews();
        urlChangeFocusWatcher();
        updateIntervalChangeFocusWatcher();
        cameraToScannerChangeWatcher();
        loadData();
    }

    public void cameraToScannerChangeWatcher() {
        cameraToScannerBtn.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                BaseActivity.editor.putBoolean("cameraOn", true).apply();
            } else {
                BaseActivity.editor.putBoolean("cameraOn", false).apply();
            }
        });
    }

    public void urlChangeFocusWatcher() {
        updateIntervalET = findViewById(R.id.updateIntervalET);
        updateIntervalET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    BaseActivity.editor.putString("urlText", getUrl()).apply();
                    checkIssuesList();
                }
            }
        });
    }

    public String getUrl() {
        urlET = findViewById(R.id.urlET);
        String url = urlET.getText().toString();
        if (url.length() > 1 && url.charAt(url.length() - 1) != ('/')) {
            url = url + '/';
        }
        return url;
    }

    public void saveData() {
        BaseActivity.editor.putString("urlText", getUrl());
        BaseActivity.editor.putString("interval", updateIntervalET.getText().toString());
        BaseActivity.editor.putBoolean("cameraOn", cameraToScannerBtn.isChecked());
        BaseActivity.editor.putBoolean("alarmOn", autoUpdateBtn.isChecked());
        BaseActivity.editor.apply();
        if (sp.getString("urlText", "").isEmpty()) {
            Intent intent = new Intent(this, GetToken.class);
            startActivityForResult(intent, 0);
        }
    }

    public void loadData() {
        urlET.setText(sp.getString("urlText", ""));
        updateIntervalET.setText(sp.getString("interval", "60"));
        autoUpdateBtn.setChecked(sp.getBoolean("alarmOn", true));
        cameraToScannerBtn.setChecked(sp.getBoolean("cameraOn", false));
    }

    public void updateViews() {
        urlET.setText(BaseActivity.sp.getString("urlText", ""));
        updateIntervalET.setText(sp.getString("interval", "60"));
        autoUpdateBtn.setChecked(sp.getBoolean("alarmOn", true));
        cameraToScannerBtn.setChecked(sp.getBoolean("cameraOn", false));
    }

    public void updateIntervalChangeFocusWatcher() {
        updateIntervalET = findViewById(R.id.updateIntervalET);
        updateIntervalET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    cancelAlarm();
                    BaseActivity.editor.putString("interval", updateIntervalET.getText().toString()).apply();
                    setAlarm();
                 }
            }
        });
    }

    public void updateAPK() {
        Intent intent = new Intent(Configuration.this, UpdateBroadcastReceiver.class);
        sendBroadcast(intent);
        HashMap<String, Object> params = new HashMap<>();
        if (BaseActivity.getUpdatable()) {
            try {
                OutputStream outputStream = new FileOutputStream(
                        BaseActivity.sp.getString("apkSavePath", ""));
                getRemoteAPIDownload().setFetchDataObj(BaseActivity.sp.getString("urlText", "") + "app/current.apk",
                        this,
                        RemoteAPIDownload.GET,
                        params,
                        outputStream);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "No update available.", Toast.LENGTH_LONG).show();
        }
    }

    public void checkIssuesList() {
        HashMap<String, Object> params = new HashMap<>();
        try {
            getRemoteAPIDownload().setFetchDataObj(BaseActivity.sp.getString("urlText", "") + "qualitylist.json",
                    this,
                    0,
                    params,
                    null);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (Build.VERSION.SDK_INT <= 24) {
            urlET = findViewById(R.id.urlET);
            IntentResult result = IntentIntegrator
                    .parseActivityResult(IntentIntegrator.REQUEST_CODE, resultCode, data);
            urlET.setText(result.getContents());
        } else if (resultCode == CommonStatusCodes.SUCCESS && data != null) {
            Barcode barcode = data.getParcelableExtra("barcode");
            EditText edit_text = findViewById(R.id.urlET);
            if (barcode != null) {
                edit_text.setText(barcode.displayValue);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void displayData(String data, int responseCode, String responseMessage, int requestType) {
        if (responseCode < HttpURLConnection.HTTP_BAD_REQUEST) {
            switch (requestType) {
                case RemoteAPIDownload.GET: {
                    if (data != null) {
                        editor = sp.edit();
                        editor.putString("issuesString", data).commit();
                    } else {
                        Intent intent;
                        File apkFile = new File(sp.getString("apkSavePath", ""));
                        Uri uriFile = Uri.fromFile(apkFile);
                        if (this != null) {
                            if (Build.VERSION.SDK_INT >= 24) {
                                uriFile = FileProvider.getUriForFile(this,
                                        this.getPackageName() + ".provider", apkFile);
                            }
                        }
                        intent = new Intent(Intent.ACTION_INSTALL_PACKAGE, uriFile);
                        intent.setDataAndType(uriFile, "application/vnd.android.package-archive");
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        if (this != null) {
                            this.startActivity(intent);
                        }
                    }
                    break;

                }
                case RemoteAPIDownload.HEAD: {
                    Date updateBuildDate = new Date(data);
                    Date buildDate = new Date(BuildConfig.TIMESTAMP);

                    //gets the last refused modified date from shared preferences.
                    // (The last refused modified date comes from UpdateDownloadAPKHandler
                    long lastRefusedUpdate = BaseActivity.sp.getLong("ignoreUpdateDateSP", 0);

                    BaseActivity.updatable = !(updateBuildDate.compareTo(
                            new Date(lastRefusedUpdate)) == 0) &
                            (buildDate.compareTo(updateBuildDate) < 0);
                    checkIssuesList();
                    break;
                }
                default:
                    System.out.println("Configure Exception: the request type isn't GET, POST, HEAD, or APK ");
            }
        } else if (responseCode == 403) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(Configuration.this,
                            "The token is not correct.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Configuration.this, GetToken.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    Configuration.this.startActivity(intent);
                }
            });
        } else if (responseCode == 404) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(Configuration.this,
                            "The URL is not correct.", Toast.LENGTH_LONG).show();
                    BaseActivity.editor.putString("urlText", "").apply();
                    Intent intent = new Intent(Configuration.this, GetToken.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    Configuration.this.startActivity(intent);
                }
            });
        } else {
            Toast.makeText(Configuration.this,
                    "Something went wrong.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void displayException(Exception e) {
        if (e.getMessage() != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            });
        }
    }

    private void downloadingAlert() {
        final androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Update Available")
                .setMessage("Tap Update to install the app.")
                .setCancelable(false)
                .setNeutralButton("Ignore Update", (dialogInterface, i) -> {
                    Toast.makeText(this, "Ignore This Update.", Toast.LENGTH_LONG).show();
                    //If a user refuses an update, the last modified date for that update
                    // is saved in shared preferences,
                    Configuration.editor.putLong("ignoreUpdateDateSP", BaseActivity.updateAvailableBuildDate.getTime())
                            .apply();
                    BaseActivity.updatable = false;
                })
                .setPositiveButton("Update", (dialogInterface, i) -> {
                    Configuration.editor.putLong("ignoreUpdateDateSP", BaseActivity.updateAvailableBuildDate.getTime())
                            .apply();
                    downloadAPKFile();
                })
                .create();
        dialog.show();
    }

    public void downloadAPKFile() {
        if (BaseActivity.getUpdatable()) {
            HashMap<String, Object> params = new HashMap<>();
            try {
                OutputStream outputStream = new FileOutputStream(
                        BaseActivity.sp.getString("apkSavePath", ""));
                BaseActivity.getRemoteAPIDownload().setFetchDataObj(BaseActivity.sp.getString("urlText", "") + "app/current.apk",
                        this,
                        RemoteAPIDownload.GET,
                        params,
                        outputStream);
            } catch (Exception e) {
                System.out.println("Error with the update.");
            }
        }
    }
}