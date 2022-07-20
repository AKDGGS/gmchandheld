package gov.alaska.gmchandheld;

import android.app.ProgressDialog;
import android.content.DialogInterface;
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

import java.net.HttpURLConnection;
import java.util.Date;
import java.util.HashMap;

public class MoveContents extends BaseActivity implements HTTPRequestCallback {
    private EditText moveContentsFromET, moveContentsToET;
    private ProgressDialog downloadingAlert;

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
                HashMap<String, Object> params = new HashMap<>();
                params.put("src", moveContentsFromET.getText().toString());
                params.put("dest", moveContentsToET.getText().toString());

                try {
                    downloadingAlert = new ProgressDialog(this);
                    downloadingAlert.setMessage("Moving the contents.");
                    downloadingAlert.setCancelable(false);
                    downloadingAlert.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            thread.interrupt();
                            downloadingAlert.dismiss();//dismiss dialog
                        }
                    });
                    downloadingAlert.show();
                    getHTTPRequest().setFetchDataObj(baseURL + "movecontents.json?",
                            this,
                            0,
                            params,
                            null);
                } catch (Exception e) {
                    System.out.println("Move Contents Exception: " + e.getMessage());
                    e.printStackTrace();
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

        if (updatable) {
            downloadingAlert();
        }
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
    public void displayData(byte[] byteData, Date date, int responseCode, String responseMessage, int requestType) {
        if (downloadingAlert != null) {
            downloadingAlert.dismiss();
        }
        String data = new String(byteData);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
                    } else if (responseCode == 404) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MoveContents.this,
                                        "The URL is not correct.", Toast.LENGTH_LONG).show();
                                BaseActivity.editor.putString("urlText", "").apply();
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
        if (downloadingAlert != null) {
            downloadingAlert.dismiss();
        }
        if (e.getMessage() != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    System.out.println("Move Content exception: " + e.getMessage());
                }
            });
        }
    }
}