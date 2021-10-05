package gov.alaska.gmchandheld;

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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Recode extends BaseActivity implements RemoteAPIDownloadCallback {
    private EditText oldBarcodeET, newBarcodeET;
    private String data;

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
                String barcode = null;
                String newBarcode = null;
                try {
                    barcode = URLEncoder.encode(oldBarcodeET.getText().toString(),
                            "utf-8");
                    newBarcode = URLEncoder.encode(newBarcodeET.getText().toString(),
                            "utf-8");
                } catch (UnsupportedEncodingException e) {
                    //						exception = new Exception(e.getMessage());
                }

                StringBuilder sb = new StringBuilder();
                if (barcode != null) {
                    sb.append("old=").append(barcode);
                }
                if (newBarcode != null) {
                    sb.append("&new=").append(newBarcode);
                }

                try {
                    remoteAPIDownload.setFetchDataObj(baseURL + "recode.json?" + sb.toString(),
                            BaseActivity.apiKeyBase, null, this);
                } catch (Exception e) {
                    System.out.println("Exception: " + e.getMessage());
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
    public void displayData(String data, int responseCode, String responseMessage) {

        runOnUiThread(() -> {
            if (null == data) {
                Toast.makeText(Recode.this, "There was a problem.  " +
                        "Nothing was changed.", Toast.LENGTH_SHORT).show();
                oldBarcodeET.requestFocus();
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
        if (e.getMessage() != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    System.out.println(e.getMessage());
                }
            });
        }
    }
}