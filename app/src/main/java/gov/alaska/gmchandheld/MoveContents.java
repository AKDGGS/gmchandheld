package gov.alaska.gmchandheld;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
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
import java.net.HttpURLConnection;
import java.net.URLEncoder;

public class MoveContents extends BaseActivity implements RemoteAPIDownloadCallback {
    private EditText moveContentsFromET, moveContentsToET;

    @Override
    public int getLayoutResource() {
        return R.layout.move_contents;
    }

    @Override
    public void onRestart() {
        super.onRestart();
        this.recreate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        moveContentsFromET = findViewById(R.id.fromET);
        moveContentsToET = findViewById(R.id.toET);
        // onClickListener listens if the submit button is clicked

        findViewById(R.id.submitBtn).setOnClickListener(v -> {
            if (!(TextUtils.isEmpty(moveContentsFromET.getText())) &
                    !(TextUtils.isEmpty(moveContentsToET.getText()))) {
                String source = null;
                String destination = null;
                try {
                    source = URLEncoder.encode(moveContentsFromET.getText().toString(),
                            "utf-8");
                    destination = URLEncoder.encode(moveContentsToET.getText().toString(),
                            "utf-8");
                } catch (UnsupportedEncodingException e) {
//						exception = new Exception(e.getMessage());
                }

                StringBuilder sb = new StringBuilder();
                if (source != null) {
                    sb.append("src=").append(source);
                }
                if (destination != null) {
                    sb.append("&dest=").append(destination);
                }

                try {
                    getRemoteAPIDownload().setFetchDataObj(baseURL + "movecontents.json?" + sb.toString(),
                            BaseActivity.getToken(),
                            null,
                            this,
                            0);
                } catch (Exception e) {
                    System.out.println("Exception: " + e.getMessage());
                }
            }
        });

        // KeyListener listens if enter is pressed
        moveContentsFromET.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                moveContentsToET.requestFocus();
                return true;
            }
            return false;
        });
        Button fromCameraBtn = findViewById(R.id.fromCameraBtn);
        Button toCameraBtn = findViewById(R.id.toCameraBtn);
        if (!sp.getBoolean("cameraOn", false)) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.weight = 8.25f;
            moveContentsFromET.setLayoutParams(params);
            moveContentsToET.setLayoutParams(params);
            fromCameraBtn.setVisibility(View.GONE);
            toCameraBtn.setVisibility(View.GONE);
        } else {
            qrScan = new IntentIntegrator(this);
            qrScan.setBeepEnabled(true);
        }
        fromCameraBtn.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT <= 24) {
                intent = qrScan.createScanIntent();
            } else {
                intent = new Intent(MoveContents.this, CameraToScanner.class);
            }
            startActivityForResult(intent, 1);
        });
        toCameraBtn.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT <= 24) {
                intent = qrScan.createScanIntent();
            } else {
                intent = new Intent(MoveContents.this, CameraToScanner.class);
            }
            startActivityForResult(intent, 2);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (Build.VERSION.SDK_INT <= 24) {
            IntentResult result = IntentIntegrator.parseActivityResult(
                    IntentIntegrator.REQUEST_CODE, resultCode, data);
            switch (requestCode) {
                case 1: {
                    moveContentsFromET = findViewById(R.id.fromET);
                    moveContentsFromET.setText(result.getContents());
                }
                break;
                case 2: {
                    moveContentsToET = findViewById(R.id.toET);
                    moveContentsToET.setText(result.getContents());
                }
                break;
            }
        } else {
            if (data != null) {
                Barcode barcode = data.getParcelableExtra("barcode");
                if (barcode != null) {
                    switch (requestCode) {
                        case 1: {
                            if (resultCode == CommonStatusCodes.SUCCESS) {
                                moveContentsFromET.setText(barcode.displayValue);
                            }
                            break;
                        }
                        case 2: {
                            if (resultCode == CommonStatusCodes.SUCCESS) {
                                moveContentsToET.setText(barcode.displayValue);
                            }
                            break;
                        }
                        default:
                            super.onActivityResult(requestCode, resultCode, data);
                    }
                }
            }
        }
    }

    @Override
    public void displayData(String data, int responseCode, String responseMessage, int requestType) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                System.out.println(data);
                if (!(responseCode < HttpURLConnection.HTTP_BAD_REQUEST) || data == null) {
                    if (responseCode == 403) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MoveContents.this,
                                        "The token is not correct.", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(MoveContents.this, GetToken.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                MoveContents.this.startActivity(intent);
                            }
                        });
                    } else {
                        Toast.makeText(MoveContents.this, "There was a problem. Nothing was moved.",
                                Toast.LENGTH_LONG).show();
                        moveContentsFromET.requestFocus();
                    }
                } else if (data.contains("success")) {
                    Toast.makeText(MoveContents.this, "The contents were moved.",
                            Toast.LENGTH_LONG).show();
                    moveContentsFromET.requestFocus();
                    moveContentsFromET.setText("");
                    moveContentsToET.setText("");
                    moveContentsFromET.requestFocus();
                }
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