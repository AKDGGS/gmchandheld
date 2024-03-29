package gov.alaska.gmchandheld;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.security.ProviderInstaller;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import javax.net.ssl.SSLContext;

public class Summary extends BaseActivity implements HTTPRequestCallback {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.REQUEST_INSTALL_PACKAGES
    };
    private static LinkedList<String> summaryHistory;
    private static String lastAdded;
    private ListView listView;
    private EditText barcodeET;
    private String barcode;
    private ProgressDialog downloadingAlert;
    private StringBuilder sb = new StringBuilder();

    public Summary() {
        summaryHistory = SummaryDisplayObjInstance.getInstance().getSummaryHistory();
    }

    public static LinkedList<String> getSummaryHistory() {
        return summaryHistory;
    }

    public static void setSummaryHistory(LinkedList<String> summaryHistory) {
        Summary.summaryHistory = summaryHistory;
    }

    public static String getLastAdded() {
        return Summary.lastAdded;
    }

    public static void setLastAdded(String lastAdded) {
        Summary.lastAdded = lastAdded;
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // If we don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private void deleteApkFile() {
        File dir = getExternalCacheDir();
        File file = new File(dir, "current.apk");
        file.delete();
    }

    @Override
    public int getLayoutResource() {
        return R.layout.summary_get_barcode;
    }

    @Override
    public void onRestart() {
        super.onRestart();
        checkUrlUsesHttps(this);
        EditText barcodeET = findViewById(R.id.barcodeET);
        barcodeET.selectAll();
        this.recreate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Summary");
        SummaryDisplayObjInstance.getInstance().summaryLogicForDisplayObj = null;
        verifyStoragePermissions(Summary.this);
        deleteApkFile();
        Intent myIntent = new Intent(Summary.this, UpdateBroadcastReceiver.class);
        boolean isWorking = (PendingIntent.getBroadcast(Summary.this, 101, myIntent, PendingIntent.FLAG_NO_CREATE) != null);
        if (!isWorking) {
            setAlarm();
        }
        PackageManager pm = this.getPackageManager();
        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            BaseActivity.editor.putBoolean("cameraOn", false).apply();
        }
        // populates the history list
        listView = findViewById(R.id.listViewSummaryHistory);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1);
        adapter.addAll(summaryHistory);
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
        Button cameraBtn = findViewById(R.id.cameraBtn);
        if (!sp.getBoolean("cameraOn", false)) {
            cameraBtn.setVisibility(View.GONE);
        } else {
            qrScan = new IntentIntegrator(this);
            qrScan.setBeepEnabled(true);
        }
        cameraBtn.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT <= 24) {
                qrScan.initiateScan();
            } else {
                intent = new Intent(Summary.this, CameraToScanner.class);
                startActivityForResult(intent, 0);
            }
        });
        barcodeET = findViewById(R.id.barcodeET);
        barcodeET.requestFocus();
        Button submitBtn = findViewById(R.id.submitBtn);
        submitBtn.setOnClickListener(v -> {
            submitBtn.setEnabled(false);
            if (!barcodeET.getText().toString().isEmpty()) {
                barcode = barcodeET.getText().toString();
                downloadingAlert = new ProgressDialog(this);
                downloadingAlert.setMessage("Loading...\n" + barcode);
                downloadingAlert.setCancelable(false);
                downloadingAlert.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        thread.interrupt();
                        downloadingAlert.dismiss();//dismiss dialog
                    }
                });
                downloadingAlert.show();
                if (!barcode.isEmpty()) {
                    HashMap<String, Object> params = new HashMap<>();
                    params.put("barcode", barcode);
                    try {
                        getHTTPRequest().setFetchDataObj(
                                baseURL + "summary.json?",
                                this,
                                0,
                                params,
                                null);
                    } catch (Exception e) {
                        System.out.println("Summary Exception: " + e.getMessage());
                        e.printStackTrace();
                        Toast.makeText(Summary.this,
                                "There is a problem. " + e.getMessage(), Toast.LENGTH_LONG).show();
                        thread.interrupt();
                        if (downloadingAlert != null) {
                            downloadingAlert.dismiss();
                        }
                    }
                }
            }
            barcodeET.setText("");
            submitBtn.setEnabled(true);
        });
        // KeyListener listens if enter is pressed
        barcodeET.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                submitBtn.performClick();
                return true;
            }
            return false;
        });
        // Clicking barcode in history list.
        listView.setOnItemClickListener((parent, view, position, id) -> {
            barcodeET.setText(listView.getItemAtPosition(position).toString());
            submitBtn.performClick();
        });
        if (BaseActivity.getUpdatable()) { //Set in UpdateBroadcastReceiver and Configuration
            downloadingAlert();
        }
    }

    @Override
    public void onBackPressed() {
        SummaryDisplayObjInstance.getInstance().summaryLogicForDisplayObj = null;
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (Build.VERSION.SDK_INT <= 24) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            barcodeET.setText(result.getContents());
        } else {
            if (requestCode == 0) {
                if (resultCode == CommonStatusCodes.SUCCESS && data != null) {
                    Barcode barcode = data.getParcelableExtra("barcode");
                    if (barcode != null) {
                        barcodeET.setText(barcode.displayValue);
                    }
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        AudioManager manager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        manager.adjustVolume(AudioManager.ADJUST_RAISE, 0);
        manager.adjustVolume(AudioManager.ADJUST_LOWER, 0);
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_VOLUME_UP: {
                if (action == KeyEvent.ACTION_DOWN && event.isLongPress()) {
                    listView.smoothScrollToPosition(0, 0);
                }
                if (KeyEvent.ACTION_UP == action) {
                    listView.smoothScrollByOffset(-3);
                }
                return true;
            }
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_VOLUME_DOWN: {
                if (action == KeyEvent.ACTION_DOWN && event.isLongPress()) {
                    listView.smoothScrollToPosition(listView.getCount());
                }
                if (KeyEvent.ACTION_UP == action) {
                    listView.smoothScrollByOffset(3);
                }
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void displayData(byte[] byteData, Date date, int responseCode, String responseMessage, int requestType) {
        if (downloadingAlert != null) {
            downloadingAlert.dismiss();
        }
        String data = new String(byteData);
        if (!(responseCode < HttpURLConnection.HTTP_BAD_REQUEST) || data == null) {
            switch (responseCode){
                case 403:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Summary.this,
                                    "The token is not correct.", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(Summary.this, GetToken.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            Summary.this.startActivity(intent);
                        }
                    });
                case 404:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Summary.this,
                                    "The URL is not correct.", Toast.LENGTH_LONG).show();
                            BaseActivity.editor.putString("urlText", "").apply();
                            Intent intent = new Intent(Summary.this, GetToken.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            Summary.this.startActivity(intent);
                        }
                    });
                default:
                    runOnUiThread(() -> Toast.makeText(Summary.this,
                            "There was an error looking up " + barcode + ".\n" +
                                    "Is the barcode a container?", Toast.LENGTH_LONG).show());
                    sb = new StringBuilder();
            }
        } else {
            SummaryLogicForDisplay summaryLogicForDisplayObj;
            summaryLogicForDisplayObj = new SummaryLogicForDisplay();
            SummaryDisplayObjInstance.getInstance().summaryLogicForDisplayObj
                    = summaryLogicForDisplayObj;
            summaryLogicForDisplayObj.setBarcodeQuery(barcode);
            try {
                summaryLogicForDisplayObj.processRawJSON(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
            intent = new Intent(Summary.this, SummaryDisplay.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("barcode", barcode);
            Summary.this.startActivity(intent);

            if (!summaryHistory.isEmpty()) {
                lastAdded = summaryHistory.get(0);
            }
            if (!barcode.equals(lastAdded)) {
                summaryHistory.add(0, barcode);
            }
        }
    }

    @Override
    public void displayException(Exception e) {
        if (downloadingAlert != null) {
            downloadingAlert.dismiss();
        }
        if (e.getMessage() != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    thread.interrupt();
                    barcodeET.setText("");
                    sb.setLength(0);
                }
            });
        }
    }

    public void enableTSL(Context mContext) {
        try {
            // enables TSL-1.2 if Google Play is updated on old devices.
            // doesn't work with emulators
            // https://stackoverflow.com/a/29946540
            ProviderInstaller.installIfNeeded(mContext);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, null);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }
}
