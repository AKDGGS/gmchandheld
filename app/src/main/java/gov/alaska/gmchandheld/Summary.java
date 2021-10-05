package gov.alaska.gmchandheld;

import android.content.Context;
import android.content.Intent;
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

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.LinkedList;

public class Summary extends BaseActivity implements RemoteAPIDownloadCallback {
    private static LinkedList<String> summaryHistory;
    private static String lastAdded;
    private ListView listView;
    private EditText barcodeET;
    private String barcode;

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

    @Override
    public int getLayoutResource() {
        return R.layout.summary_get_barcode;
    }

    @Override
    public void onRestart() {
        super.onRestart();
        EditText barcodeET = findViewById(R.id.barcodeET);
        barcodeET.selectAll();
        this.recreate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SummaryDisplayObjInstance.getInstance().summaryLogicForDisplayObj = null;
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
        Button submitBtn = findViewById(R.id.submitBtn);
        barcodeET = findViewById(R.id.barcodeET);

        submitBtn.setOnClickListener(v -> {
            submitBtn.setEnabled(false);
            if (!barcodeET.getText().toString().isEmpty()) {
                barcode = barcodeET.getText().toString();
                processingAlert(this, barcode);
                if (!barcode.isEmpty()) {
                    try {
                        barcode = URLEncoder.encode(barcode, "utf-8");
                    } catch (Exception e) {
//                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    try {
                        remoteAPIDownload.setFetchDataObj(
                                baseURL + "summary.json?barcode=" + barcode,
                                BaseActivity.apiKeyBase,
                                null,
                                this);
                    } catch (Exception e) {
                        System.out.println("Exception: " + e.getMessage());
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

    //makes the volume keys scroll up/down
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
    public void displayData(String data, int responseCode, String responseMessage) {
        if (data == null || !(responseCode < HttpURLConnection.HTTP_BAD_REQUEST)) {
            if (alert != null) {
                alert.dismiss();
                alert = null;
            }
            runOnUiThread(() -> Toast.makeText(Summary.this,
                    "There was an error looking up " + barcode + ".\n" +
                            "Does the barcode a container?", Toast.LENGTH_LONG).show());
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
        if (e.getMessage() != null) {
            if (alert != null) {
                alert.dismiss();
                alert = null;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    barcodeET.setText("");
                }
            });
        }
    }
}
