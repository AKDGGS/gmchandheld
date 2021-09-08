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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Summary extends BaseActivity {
    private ListView listView;
    private static LinkedList<String> summaryHistory;
    private EditText barcodeET;
    private static String lastAdded;
    private String data;

    public Summary() {
        summaryHistory = SummaryDisplayObjInstance.getInstance().getSummaryHistory();
    }
    public static LinkedList<String> getSummaryHistory() {
        return summaryHistory;
    }

    public static String getLastAdded() {
        return Summary.lastAdded;
    }

    public static void setLastAdded(String lastAdded) {
        Summary.lastAdded = lastAdded;
    }

    public static void setSummaryHistory(LinkedList<String> summaryHistory) {
        Summary.summaryHistory = summaryHistory;
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
        // Submit barcode query

        if (!downloading) {
            downloading = true;
            submitBtn.setOnClickListener(v -> {
                if (!barcodeET.getText().toString().isEmpty()) {
                    String barcode = barcodeET.getText().toString();
                    processingAlert(this, barcode);
                    if (!barcode.isEmpty()) {
                        try {
                            barcode = URLEncoder.encode(barcode, "utf-8");
                        } catch (UnsupportedEncodingException e) {
//                            exception = new Exception(e.getMessage());
                        }
                        String url = baseURL + "summary.json?barcode=" + barcode;
                        String finalBarcode = barcode;

                        Runnable runnable = new Runnable(){
                            @Override
                            public void run() {
                                if (thread.isInterrupted()){
                                    return;
                                }
                                final ExecutorService service = Executors.newFixedThreadPool(1);
                                final Future<String> task = service.submit(new NewRemoteAPIDownload(url));
                                try {
                                    data = task.get();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                    return;
                                }
                                service.shutdownNow();

                                if (data == null || data.length() <= 2) {
                                    if (alert != null){
                                        alert.dismiss();
                                        alert = null;
                                    }
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(Summary.this,
                                                    "There was an error looking up " + finalBarcode + ".", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                } else {
                                    SummaryLogicForDisplay summaryLogicForDisplayObj;
                                    summaryLogicForDisplayObj = new SummaryLogicForDisplay();
                                    SummaryDisplayObjInstance.getInstance().summaryLogicForDisplayObj
                                            = summaryLogicForDisplayObj;
                                    summaryLogicForDisplayObj.setBarcodeQuery(finalBarcode);
                                    try {
                                        summaryLogicForDisplayObj.processRawJSON(data);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    intent = new Intent(Summary.this, SummaryDisplay.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.putExtra("barcode", finalBarcode);
                                    Summary.this.startActivity(intent);

                                    if (!summaryHistory.isEmpty()) {
                                        lastAdded = summaryHistory.get(0);
                                    }
                                    if (!finalBarcode.equals(lastAdded)) {
                                        summaryHistory.add(0, finalBarcode);
                                    }
                                }
                                downloading = false;
                            }

                        };

                        thread = new Thread(runnable);
                        thread.start();
                        barcodeET.setText("");
                    }
                }
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
}
