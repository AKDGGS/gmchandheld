package gov.alaska.gmchandheld;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class AuditDisplay extends BaseActivity implements HTTPRequestCallback {
    private ArrayList<String> containerList;
    private ArrayAdapter<String> adapter;
    private EditText remarkET, itemET;
    private Button clearAllBtn;
    private int clicks; //used to count double clicks for deletion
    private TextView countTV;
    private ProgressDialog downloadingAlert;

    public AuditDisplay() {
        clicks = 0;
    }

    @Override
    public int getLayoutResource() {
        return R.layout.audit_display;
    }

    @Override
    public void onRestart() {
        super.onRestart();
        this.recreate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Color.parseColor("#5e3c99"));

        itemET = findViewById(R.id.itemET);
        remarkET = findViewById(R.id.remarkET);
        countTV = findViewById(R.id.auditCountTV);
        clearAllBtn = findViewById(R.id.clearAllBtn);
        ListView auditContainerListLV = findViewById(R.id.listViewGetContainersToAudit);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        auditContainerListLV.setAdapter(adapter);
        containerList = AuditDisplayObjInstance.getInstance().getAuditList();
        adapter.addAll(containerList);
        countTV.setText(String.valueOf(containerList.size()));
        Button remarkCameraBtn = findViewById(R.id.cameraBtn);
        Button itemCameraBtn = findViewById(R.id.itemCameraBtn);
        if (!sp.getBoolean("cameraOn", false)) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.weight = 6.25f;
            remarkET.setLayoutParams(params);
            itemET.setLayoutParams(params);
            remarkCameraBtn.setVisibility(View.GONE);
            itemCameraBtn.setVisibility(View.GONE);
        } else {
            qrScan = new IntentIntegrator(this);
            qrScan.setBeepEnabled(true);
            qrScan = new IntentIntegrator(this);
            qrScan.setBeepEnabled(true);
        }
        remarkCameraBtn.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT <= 24) {
                intent = qrScan.createScanIntent();
            } else {
                intent = new Intent(AuditDisplay.this,
                        CameraToScanner.class);
            }
            startActivityForResult(intent, 1);
        });
        itemCameraBtn.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT <= 24) {
                intent = qrScan.createScanIntent();
            } else {
                intent = new Intent(AuditDisplay.this,
                        CameraToScanner.class);
            }
            startActivityForResult(intent, 2);
        });
        final Button addBtn = findViewById(R.id.addContainerBtn);
        addBtn.setOnClickListener(v -> {
            String container = itemET.getText().toString();
            if (!containerList.contains(container) && !container.isEmpty()) {
                containerList.add(0, container);
                adapter.insert(container, 0);
                adapter.notifyDataSetChanged();
                countTV.setText(String.valueOf(containerList.size()));
            }
            itemET.setText("");
            itemET.requestFocus();
        });
        clearAllBtn.setOnClickListener(v -> {
            itemET.setText("");
            itemET.requestFocus();
            containerList.clear();
            adapter.clear();
            adapter.notifyDataSetChanged();
            countTV.setText(String.valueOf(containerList.size()));
        });

        //double click to remove elements
        auditContainerListLV.setOnItemClickListener((adapterView, view, position, l) -> {
            clicks++;
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                if (clicks == 2) {
                    adapter.remove(containerList.get(position));
                    containerList.remove(position);
                    adapter.notifyDataSetChanged();
                    countTV.setText(String.valueOf(containerList.size()));
                }
                clicks = 0;
            }, 500);
        });
        // KeyListener listens if enter is pressed
        itemET.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_UP) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                addBtn.performClick();
                itemET.requestFocus();
                return true;
            }
            return false;
        });
        // KeyListener listens if enter is pressed
        remarkET.setOnKeyListener((v, keyCode, event) -> {
            if (!(TextUtils.isEmpty(remarkET.getText()))) {
                if ((event.getAction() == KeyEvent.ACTION_UP) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    itemET.requestFocus();
                    return true;
                }
            }
            return false;
        });
        // onClickListener listens if the submit button is clicked
        findViewById(R.id.submitBtn).setOnClickListener(v -> {
            if (!remarkET.getText().toString().isEmpty() || containerList.size() != 0) {
                HashMap<String, Object> params = new HashMap<>();
                params.put("remark", remarkET.getText().toString());
                params.put("c", containerList);

                try {
                    downloadingAlert = new ProgressDialog(this);
                    downloadingAlert.setMessage("Uploading the audit list to the database.");
                    downloadingAlert.setCancelable(false);
                    downloadingAlert.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            thread.interrupt();
                            downloadingAlert.dismiss();//dismiss dialog
                        }
                    });
                    downloadingAlert.show();
                    getHTTPRequest().setFetchDataObj(baseURL + "audit.json?",
                            this,
                            0,
                            params,
                            null);
                } catch (Exception e) {
                    System.out.println("Audit Display Exception: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(AuditDisplay.this,
                        "This request is empty.  Please add at least one item to the list or a" +
                                "remark.",
                        Toast.LENGTH_LONG).show();
            }
        });

        if (updatable) {
            downloadingAlert();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (Build.VERSION.SDK_INT <= 24) {
            switch (requestCode) {
                case 1: {
                    remarkET = findViewById(R.id.remarkET);
                    IntentResult result = IntentIntegrator.parseActivityResult(
                            IntentIntegrator.REQUEST_CODE, resultCode, data);
                    remarkET.setText(result.getContents());
                }
                break;
                case 2: {
                    itemET = findViewById(R.id.itemET);
                    IntentResult result = IntentIntegrator.parseActivityResult(
                            IntentIntegrator.REQUEST_CODE, resultCode, data);
                    itemET.setText(result.getContents());
                }
                break;
            }
        } else {
            switch (requestCode) {
                case 1: {
                    if (resultCode == CommonStatusCodes.SUCCESS && data != null) {
                        Barcode barcode = data.getParcelableExtra("barcode");
                        EditText edit_text = findViewById(R.id.remarkET);
                        if (barcode != null) {
                            edit_text.setText(barcode.displayValue);
                        }
                    }
                    break;
                }
                case 2: {
                    if (resultCode == CommonStatusCodes.SUCCESS && data != null) {
                        Barcode barcode = data.getParcelableExtra("barcode");
                        EditText edit_text = findViewById(R.id.itemET);
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

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (clearAllBtn.hasFocus()) {
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
        runOnUiThread(() -> {
            if (!(responseCode < HttpURLConnection.HTTP_BAD_REQUEST) || data == null) {
                if (responseCode == 403) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AuditDisplay.this,
                                    "The token is not correct.", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(AuditDisplay.this, GetToken.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            AuditDisplay.this.startActivity(intent);
                        }
                    });
                } else if (responseCode == 404) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AuditDisplay.this,
                                    "The URL is not correct.", Toast.LENGTH_LONG).show();
                            BaseActivity.editor.putString("urlText", "").apply();
                            Intent intent = new Intent(AuditDisplay.this, GetToken.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            AuditDisplay.this.startActivity(intent);
                        }
                    });
                } else {
                    Toast.makeText(AuditDisplay.this,
                            "There was a problem. The audit was not added. " + responseCode,
                            Toast.LENGTH_SHORT).show();
                    remarkET.requestFocus();
                }
            } else if (data.contains("success")) {
                Toast.makeText(AuditDisplay.this, "The audit was added.",
                        Toast.LENGTH_SHORT).show();
                remarkET.requestFocus();
                remarkET.setText("");
                itemET.setText("");
                containerList.clear();
                adapter.clear();
                adapter.notifyDataSetChanged();
                countTV.setText(String.valueOf(containerList.size()));
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
                    remarkET.requestFocus();
                }
            });
        }
    }
}