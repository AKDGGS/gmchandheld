package gov.alaska.gmchandheld;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.LinkedList;

public class Summary extends BaseActivity {
    private ListView listView;
    private final LinkedList<String> summaryHistory;
    private EditText barcodeET;
    private IntentIntegrator qrScan;

    public Summary() {
        summaryHistory = SummaryDisplayObjInstance.getInstance().getSummaryHistory();
    }

    @Override
    public int getLayoutResource() {
        return R.layout.summary_get_barcode;
    }

    @Override
    public void onRestart() {
        super.onRestart();
        EditText barcodeInput = findViewById(R.id.barcodeET);
        barcodeInput.selectAll();
        this.recreate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkAPIkeyExists(this);
        SummaryDisplayObjInstance.getInstance().summaryLogicForDisplayObj = null;
        barcodeET = findViewById(R.id.barcodeET);
        Button submitButton = findViewById(R.id.submitBtn);
        final RemoteApiUIHandler remoteApiUIHandler = new RemoteApiUIHandler();
        // populates the history list
        listView = findViewById(R.id.listViewSummaryHistory);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
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
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT <= 24) {
                    qrScan.initiateScan();
                } else {
                    Intent intent = new Intent(Summary.this, CameraToScanner.class);
                    startActivityForResult(intent, 0);
                }
            }
        });
        // Submit barcode query
        if (remoteApiUIHandler.isDownloading()) {
            // onClickListener listens if the submit button is clicked
            submitButton.setOnClickListener(v -> {
//                CheckConfiguration checkConfiguration = new CheckConfiguration();
//                if (checkConfiguration.checkConfiguration(Summary.this)) {
                    if (!getBarcode().isEmpty()) {
                        remoteApiUIHandler.setDownloading(true);
                        RemoteApiUIHandler.setUrlFirstParameter(getBarcode());
                        new RemoteApiUIHandler.ProcessDataForDisplay(Summary.this).execute();
                    }
//                }
            });
            // KeyListener listens if enter is pressed
            barcodeET.setOnKeyListener((v, keyCode, event) -> {
                // if "enter" is pressed
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    submitButton.performClick();
                    return true;
                }
                return false;
            });
            // Clicking barcode in history list.
            listView.setOnItemClickListener((parent, view, position, id) -> {
                barcodeET.setText(listView.getItemAtPosition(position).toString());
                submitButton.performClick();
            });
        }
    }

    public String getBarcode() {
        EditText barcodeInput = findViewById(R.id.barcodeET);
        return barcodeInput.getText().toString();
    }

    @Override
    public void onBackPressed() {
        SummaryDisplayObjInstance.getInstance().summaryLogicForDisplayObj = null;
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (Build.VERSION.SDK_INT <= 24) {
            barcodeET = findViewById(R.id.barcodeET);
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            barcodeET.setText(result.getContents());
        } else {
            if (requestCode == 0) {
                if (resultCode == CommonStatusCodes.SUCCESS) {
                    Barcode barcode = data.getParcelableExtra("barcode");
                    EditText edit_text = findViewById(R.id.barcodeET);
                    edit_text.setText(barcode.displayValue);
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    //makes the volume keys scroll up/down
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action, keycode;
        action = event.getAction();
        keycode = event.getKeyCode();
        AudioManager manager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        switch (keycode) {
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_VOLUME_UP: {
                manager.adjustVolume(AudioManager.ADJUST_RAISE, 0);
                manager.adjustVolume(AudioManager.ADJUST_LOWER, 0);
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
}
