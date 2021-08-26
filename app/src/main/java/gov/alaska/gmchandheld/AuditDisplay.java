package gov.alaska.gmchandheld;

import android.content.Intent;
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
import androidx.annotation.Nullable;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import java.util.ArrayList;


public class AuditDisplay extends BaseActivity {
    private ArrayList<String> containerList;
    private ArrayAdapter<String> adapter;
    private EditText auditRemarkET, auditItemET;
    private Button clearAllBtn;
    int clicks;  //used to count double clicks for deletion

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
        checkAPIkeyExists(this);
        auditItemET = findViewById(R.id.itemET);
        auditRemarkET = findViewById(R.id.remarkET);
        final TextView auditCountTV = findViewById(R.id.auditCountTV);
        final Button submitBtn = findViewById(R.id.submitBtn);
        final Button addBtn = findViewById(R.id.addContainerBtn);
        clearAllBtn = findViewById(R.id.clearAllBtn);
        ListView auditContainerListLV = findViewById(R.id.listViewGetContainersToAudit);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        auditContainerListLV.setAdapter(adapter);
        containerList = AuditDisplayObjInstance.getInstance().getAuditList();
        adapter.addAll(containerList);
        auditCountTV.setText(String.valueOf(containerList.size()));
        Button remarkCameraBtn = findViewById(R.id.cameraBtn);
        Button itemCameraBtn = findViewById(R.id.itemCameraBtn);
        if (!sp.getBoolean("cameraOn", false)) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.weight = 6.25f;
            auditRemarkET.setLayoutParams(params);
            auditItemET.setLayoutParams(params);
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
        addBtn.setOnClickListener(v -> {
            String container = auditItemET.getText().toString();
            if (!containerList.contains(container) && !container.isEmpty() ) {
                containerList.add(0, container);
                adapter.insert(container, 0);
                adapter.notifyDataSetChanged();
                auditCountTV.setText(String.valueOf(containerList.size()));
            }
            auditItemET.setText("");
            auditItemET.requestFocus();
        });
        clearAllBtn.setOnClickListener(v -> {
            auditItemET.setText("");
            auditItemET.requestFocus();
            containerList.clear();
            adapter.clear();
            adapter.notifyDataSetChanged();
            auditCountTV.setText(String.valueOf(containerList.size()));
        });
        final RemoteApiUIHandler remoteApiUIHandler = new RemoteApiUIHandler();
        if (remoteApiUIHandler.isDownloading()) {
            //double click to remove elements
            auditContainerListLV.setOnItemClickListener((adapterView, view, position, l) -> {
                                clicks++;
                                Handler handler = new Handler();
                                handler.postDelayed(() -> {
                                    if (clicks == 2) {
                                        adapter.remove(containerList.get(position));
                                        containerList.remove(position);
                                        adapter.notifyDataSetChanged();
                                        auditCountTV.setText(String.valueOf(containerList.size()));
                                    }
                                    clicks = 0;
                                }, 500);
                            });
            // KeyListener listens if enter is pressed
            auditItemET.setOnKeyListener((v, keyCode, event) -> {
                if ((event.getAction() == KeyEvent.ACTION_UP) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    addBtn.performClick();
                    auditItemET.requestFocus();
                    return true;
                }
                return false;
            });
            // KeyListener listens if enter is pressed
            auditRemarkET.setOnKeyListener((v, keyCode, event) -> {
                if (!(TextUtils.isEmpty(auditRemarkET.getText()))) {
                    if ((event.getAction() == KeyEvent.ACTION_UP) &&
                            (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        auditItemET.requestFocus();
                        return true;
                    }
                }
                return false;
            });
            // onClickListener listens if the submit button is clicked
            submitBtn.setOnClickListener(v -> {
                        auditContainerFn(auditRemarkET.getText().toString());
                        auditItemET.setText("");
                        auditRemarkET.setText("");
                        auditCountTV.setText("");
            });
        }
    }

    public void auditContainerFn(String remarkInput) {
        RemoteApiUIHandler remoteApiUIHandler = new RemoteApiUIHandler();
        RemoteApiUIHandler.setUrlFirstParameter(remarkInput);
        RemoteApiUIHandler.setContainerList(containerList);
        remoteApiUIHandler.setDownloading(true);
        new RemoteApiUIHandler.ProcessDataForDisplay(AuditDisplay.this).execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (Build.VERSION.SDK_INT <= 24) {
            switch (requestCode) {
                case 1: {
                    auditRemarkET = findViewById(R.id.remarkET);
                    IntentResult result = IntentIntegrator.parseActivityResult(
                            IntentIntegrator.REQUEST_CODE, resultCode, data);
                    auditRemarkET.setText(result.getContents());
                }
                break;
                case 2: {
                    auditItemET = findViewById(R.id.itemET);
                    IntentResult result = IntentIntegrator.parseActivityResult(
                            IntentIntegrator.REQUEST_CODE, resultCode, data);
                    auditItemET.setText(result.getContents());
                }
                break;
            }
        } else {
            switch (requestCode) {
                case 1: {
                    if (resultCode == CommonStatusCodes.SUCCESS && null != data) {
                        Barcode barcode = data.getParcelableExtra("barcode");
                        EditText edit_text = findViewById(R.id.remarkET);
                        if (barcode != null) {
                            edit_text.setText(barcode.displayValue);
                        }
                    }
                    break;
                }
                case 2: {
                    if (resultCode == CommonStatusCodes.SUCCESS && null != data) {
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
}
