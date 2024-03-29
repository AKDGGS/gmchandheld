package gov.alaska.gmchandheld;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.net.HttpURLConnection;
import java.util.Date;
import java.util.HashMap;

public class Recode extends BaseActivity implements HTTPRequestCallback {
    private EditText oldBarcodeET, newBarcodeET;
    private ProgressDialog downloadingAlert;

    @Override
    public int getLayoutResource() {
        return R.layout.recode;
    }

    @Override
    public void onRestart() {
        super.onRestart();
        this.recreate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        oldBarcodeET = findViewById(R.id.oldBarcodeET);
        newBarcodeET = findViewById(R.id.newBarcodeET);
        Button oldBarcodeCameraBtn = findViewById(R.id.oldBarcodeCameraBtn);
        Button newBarcodeCameraBtn = findViewById(R.id.newBarcodeCameraBtn);
        if (!sp.getBoolean("cameraOn", false)) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.weight = 8.25f;
            oldBarcodeET.setLayoutParams(params);
            newBarcodeET.setLayoutParams(params);
            newBarcodeCameraBtn.setVisibility(View.GONE);
            oldBarcodeCameraBtn.setVisibility(View.GONE);
        } else {
            qrScan = new IntentIntegrator(this);
            qrScan.setBeepEnabled(true);
        }
        oldBarcodeCameraBtn.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT <= 24) {
                intent = qrScan.createScanIntent();
            } else {
                intent = new Intent(Recode.this, CameraToScanner.class);
            }
            startActivityForResult(intent, 1);
        });
        newBarcodeCameraBtn.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT <= 24) {
                intent = qrScan.createScanIntent();
            } else {
                intent = new Intent(Recode.this, CameraToScanner.class);
            }
            startActivityForResult(intent, 2);
        });
        // onClickListener listens if the submit button is clicked
        findViewById(R.id.submitBtn).setOnClickListener(v -> {
            if ((!oldBarcodeET.getText().toString().isEmpty()) &&
                    (!newBarcodeET.getText().toString().isEmpty())) {
                HashMap<String, Object> params = new HashMap<>();
                params.put("old", oldBarcodeET.getText().toString());
                params.put("new", newBarcodeET.getText().toString());
                try {
                    downloadingAlert = new ProgressDialog(this);
                    downloadingAlert.setMessage("Recoding..." );
                    downloadingAlert.setCancelable(false);
                    downloadingAlert.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            thread.interrupt();
                            downloadingAlert.dismiss();//dismiss dialog
                        }
                    });
                    downloadingAlert.show();
                    getHTTPRequest().setFetchDataObj(baseURL + "recode.json?",
                            this,
                            0,
                            params,
                            null);
                } catch (Exception e) {
                    System.out.println("Recode Exception: " + e.getMessage());
                    Toast.makeText(Recode.this,
                            "The there is a problem. " + e.getMessage(), Toast.LENGTH_LONG).show();
                    thread.interrupt();
                    if (downloadingAlert != null) {
                        downloadingAlert.dismiss();
                    }
                }

            }
        });
        // KeyListener listens if enter is pressed
        oldBarcodeET.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                newBarcodeET.requestFocus();
                return true;
            }
            return false;
        });
        if (BaseActivity.getUpdatable()) {  //Set in UpdateBroadcastReceiver and Configuration
            downloadingAlert();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (Build.VERSION.SDK_INT <= 24) {
            switch (requestCode) {
                case 1: {
                    IntentResult result = IntentIntegrator.parseActivityResult(
                            IntentIntegrator.REQUEST_CODE, resultCode, data);
                    oldBarcodeET.setText(result.getContents());
                }
                break;
                case 2: {
                    IntentResult result = IntentIntegrator.parseActivityResult(
                            IntentIntegrator.REQUEST_CODE, resultCode, data);
                    newBarcodeET.setText(result.getContents());
                }
                break;
            }
        } else {
            if (data != null) {
                Barcode barcode = data.getParcelableExtra("barcode");
                switch (requestCode) {
                    case 1: {
                        if (resultCode == CommonStatusCodes.SUCCESS) {
                            if (barcode != null) {
                                oldBarcodeET.setText(barcode.displayValue);
                            }
                        }
                        break;
                    }
                    case 2: {
                        if (resultCode == CommonStatusCodes.SUCCESS) {
                            if (barcode != null) {
                                newBarcodeET.setText(barcode.displayValue);
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

    @Override
    public void displayData(byte[] byteData, Date date, int responseCode, String responseMessage, int requestType) {
        if (downloadingAlert != null) {
            downloadingAlert.dismiss();
        }
        String data = new String(byteData);
        runOnUiThread(() -> {
            if (!(responseCode < HttpURLConnection.HTTP_BAD_REQUEST) || data == null) {
                switch (responseCode){
                    case 403:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(Recode.this,
                                        "The token is not correct.", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(Recode.this, GetToken.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                Recode.this.startActivity(intent);
                            }
                        });
                    case 404:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(Recode.this,
                                        "The URL is not correct.", Toast.LENGTH_LONG).show();
                                BaseActivity.editor.putString("urlText", "").apply();
                                Intent intent = new Intent(Recode.this, GetToken.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                Recode.this.startActivity(intent);
                            }
                        });
                    default:
                        Toast.makeText(Recode.this, "There was a problem.  " +
                                "Nothing was changed.", Toast.LENGTH_SHORT).show();
                        oldBarcodeET.requestFocus();
                }
            } else if (data.contains("success")) {
                Toast.makeText(Recode.this, "The recode was successful.",
                        Toast.LENGTH_SHORT).show();
                newBarcodeET.setText("");
                oldBarcodeET.setText("");
                oldBarcodeET.requestFocus();
            }
        });
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
                    System.out.println("Recode Exception: " + e.getMessage());
                }
            });
        }
    }
}