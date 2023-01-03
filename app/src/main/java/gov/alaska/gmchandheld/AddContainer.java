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

public class AddContainer extends BaseActivity implements HTTPRequestCallback {
    private IntentIntegrator qrScan;
    private EditText addContainerBarcodeET, addContainerNameET, addContainerRemarkET;
    private ProgressDialog downloadingAlert;

    @Override
    public int getLayoutResource() {
        return R.layout.add_container;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        this.recreate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addContainerBarcodeET = findViewById(R.id.barcodeET);
        addContainerNameET = findViewById(R.id.nameET);
        addContainerRemarkET = findViewById(R.id.remarkET);
        Button submit_button = findViewById(R.id.submitBtn);
        Button cameraBtn = findViewById(R.id.cameraBtn);
        if (!sp.getBoolean("cameraOn", false)) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.weight = 7.75f;
            addContainerBarcodeET.setLayoutParams(params);
            addContainerNameET.setLayoutParams(params);
            addContainerRemarkET.setLayoutParams(params);
            cameraBtn.setVisibility(View.GONE);
        } else {
            qrScan = new IntentIntegrator(this);
            qrScan.setBeepEnabled(true);
        }
        cameraBtn.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT <= 24) {
                qrScan.initiateScan();
            } else {
                Intent intent = new Intent(AddContainer.this, CameraToScanner.class);
                startActivityForResult(intent, 0);
            }
        });
        // KeyListener listens if enter is pressed
        addContainerBarcodeET.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                addContainerNameET.requestFocus();
                return true;
            }
            return false;
        });
        // KeyListener listens if enter is pressed
        addContainerNameET.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                addContainerRemarkET.requestFocus();
                return true;
            }
            return false;
        });
        // onClickListener listens if the submit button is clicked
        submit_button.setOnClickListener(v -> {
            if (!(TextUtils.isEmpty(addContainerBarcodeET.getText()))) {
                if (!addContainerBarcodeET.getText().toString().isEmpty()) {
                    HashMap<String, Object> params = new HashMap<>();
                    params.put("barcode", addContainerBarcodeET.getText().toString());
                    params.put("name", addContainerNameET.getText().toString());
                    params.put("remark", addContainerRemarkET.getText().toString());
                    try {
                        downloadingAlert = new ProgressDialog(this);
                        downloadingAlert.setMessage("Adding container...\n" + addContainerBarcodeET.getText().toString());
                        downloadingAlert.setCancelable(false);
                        downloadingAlert.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                thread.interrupt();
                                downloadingAlert.dismiss();//dismiss dialog
                            }
                        });
                        downloadingAlert.show();
                        getHTTPRequest().setFetchDataObj(baseURL + "addcontainer.json?",
                                this,
                                0,
                                params,
                                null);
                    } catch (Exception e) {
                        System.out.println("Add Container Exception: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });
        if (BaseActivity.getUpdatable()) { //Set in UpdateBroadcastReceiver and Configuration
            downloadingAlert();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (Build.VERSION.SDK_INT <= 24) {
            addContainerBarcodeET = findViewById(R.id.barcodeET);
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            addContainerBarcodeET.setText(result.getContents());
        } else {
            if (resultCode == CommonStatusCodes.SUCCESS && data != null) {
                Barcode barcode = data.getParcelableExtra("barcode");
                EditText edit_text = findViewById(R.id.barcodeET);
                assert barcode != null;
                edit_text.setText(barcode.displayValue);
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void displayData(byte[] byteData, Date d, int responseCode, String responseMessage, int requestType) {
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
                                Toast.makeText(AddContainer.this,
                                        "The token is not correct.", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(AddContainer.this, GetToken.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                AddContainer.this.startActivity(intent);
                            }
                        });
                    case 404:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AddContainer.this,
                                        "The URL is not correct.", Toast.LENGTH_LONG).show();
                                BaseActivity.editor.putString("urlText", "").apply();
                                Intent intent = new Intent(AddContainer.this, GetToken.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                AddContainer.this.startActivity(intent);
                            }
                        });
                    default:
                        Toast.makeText(AddContainer.this,
                                "There was a problem. The container was not added.",
                                Toast.LENGTH_SHORT).show();
                        addContainerBarcodeET.requestFocus();
                }
            } else if (data.contains("success")) {
                Toast.makeText(AddContainer.this, "The container was added.",
                        Toast.LENGTH_SHORT).show();
                addContainerBarcodeET.setText("");
                addContainerNameET.setText("");
                addContainerRemarkET.setText("");
                addContainerBarcodeET.requestFocus();
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
                    System.out.println("Add Container Exception: " + e.getMessage());
                }
            });
        }
    }
}
