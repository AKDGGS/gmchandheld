package gov.alaska.gmchandheld;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;


public class Lookup extends BaseActivity {
    private static LinkedList<String> lookupHistory;
    private static String lastAdded;
    private ListView listView;
    private EditText barcodeET;
    private String barcode;
    private Button submitBtn;
    private ProgressDialog downloadingAlert;
    private StringBuilder sb = new StringBuilder();

    public Lookup() {
        lookupHistory = LookupDisplayObjInstance.getInstance().getLookupHistory();
    }

    public static LinkedList<String> getLookupHistory() {
        return lookupHistory;
    }

    public static void setLookupHistory(LinkedList<String> lookupHistory) {
        Lookup.lookupHistory = lookupHistory;
    }

    public static String getLastAdded() {
        return lastAdded;
    }

    public static void setLastAdded(String lastAdded) {
        Lookup.lastAdded = lastAdded;
    }

    @Override
    public int getLayoutResource() {
        return R.layout.lookup_main;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        checkUrlUsesHttps(this);
        EditText barcodeET = findViewById(R.id.barcodeET);
        barcodeET.selectAll();
        this.recreate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        barcodeET = findViewById(R.id.barcodeET);
        barcodeET.requestFocus();
        loadLookup();
        if (BaseActivity.getUpdatable()) {  //Set in UpdateBroadcastReceiver and Configuration
            downloadingAlert();
        }
    }

    public void loadLookup() {
        LookupDisplayObjInstance.getInstance().lookupLogicForDisplayObj = null;
        Button cameraBtn = findViewById(R.id.cameraBtn);
        if (!sp.getBoolean("cameraOn", false)) {
            cameraBtn.setVisibility(View.GONE);
        } else {
            qrScan = new IntentIntegrator(this);
            qrScan.setOrientationLocked(false);
            qrScan.setBeepEnabled(true);
        }
        cameraBtn.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT <= 24) {
                qrScan.initiateScan();
            } else {
                intent = new Intent(Lookup.this, CameraToScanner.class);
                startActivityForResult(intent, 0);
            }
        });
        barcodeET = findViewById(R.id.barcodeET);
        submitBtn = findViewById(R.id.submitBtn);
        // populates the history list
        listView = findViewById(R.id.listViewBarcodeHistory);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1);
        adapter.addAll(lookupHistory);
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
        submitBtn.setOnClickListener(v -> {
            submitBtn.setEnabled(false);
            barcode = barcodeET.getText().toString();
            if (!barcode.isEmpty()) {
                downloadingAlert = new ProgressDialog(this);
                downloadingAlert.setMessage("Loading...\n" + barcode);
                downloadingAlert.setCancelable(false);
                downloadingAlert.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        thread.interrupt();
                        downloadingAlert.dismiss();
                    }
                });
                downloadingAlert.show();
                HashMap<String, Object> params = new HashMap<>();
                params.put("barcode", barcode);
                try {
                    getHTTPRequest().setFetchDataObj(baseURL + "inventory.json?",
                            this,
                            0,
                            params,
                            null);
                } catch (Exception e) {
                    System.out.println("Lookup Exception: " + e.getMessage());
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
    public boolean dispatchKeyEvent(KeyEvent event) {
        if ((event.isPrintingKey()) && (event.getAction() == KeyEvent.ACTION_DOWN)) {
            sb = sb.append((char)event.getUnicodeChar());
            String s =  "" + (char)event.getUnicodeChar();
            barcodeET.append(s);
        }

        if(event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
            if(sb.length()!=0) {
                sb = new StringBuilder(sb.substring(0, sb.length() - 1));
                barcode = sb.toString();
                barcodeET.setText(barcode);
                barcodeET.setSelection(barcode.length());
            }
        }

        if ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN)){
            barcode = sb.toString();
            downloadingAlert = new ProgressDialog(this);
            downloadingAlert.setMessage("Loading...\n" + barcode);
            downloadingAlert.setCancelable(false);
            downloadingAlert.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    thread.interrupt();
                    downloadingAlert.dismiss();
                }
            });
            downloadingAlert.show();
            HashMap<String, Object> params = new HashMap<>();
            params.put("barcode", barcode);
            try {
                getHTTPRequest().setFetchDataObj(baseURL + "inventory.json?",
                        this,
                        0,
                        params,
                        null);
            } catch (Exception e) {
                System.out.println("Lookup Exception: " + e.getMessage());
            }
                barcodeET.setText("");
                return true;
            }

        return super.onKeyDown(event.getKeyCode(), event);
    }

//    //makes the volume keys scroll up/down
//    @Override
//    public boolean dispatchKeyEvent(KeyEvent event) {
//        int action = event.getAction();
//        AudioManager manager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
//        manager.adjustVolume(AudioManager.ADJUST_RAISE, 0);
//        manager.adjustVolume(AudioManager.ADJUST_LOWER, 0);
//        switch (event.getKeyCode()) {
//            case KeyEvent.KEYCODE_DPAD_UP:
//            case KeyEvent.KEYCODE_VOLUME_UP: {
//                if (action == KeyEvent.ACTION_DOWN && event.isLongPress()) {
//                    listView.smoothScrollToPosition(0, 0);
//                }
//                if (KeyEvent.ACTION_UP == action) {
//                    listView.smoothScrollByOffset(-3);
//                }
//                return true;
//            }
//            case KeyEvent.KEYCODE_DPAD_DOWN:
//            case KeyEvent.KEYCODE_VOLUME_DOWN: {
//                if (action == KeyEvent.ACTION_DOWN && event.isLongPress()) {
//                    listView.smoothScrollToPosition(listView.getCount());
//                }
//                if (KeyEvent.ACTION_UP == action) {
//                    listView.smoothScrollByOffset(3);
//                }
//                return true;
//            }
//        }
//        return super.dispatchKeyEvent(event);
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (Build.VERSION.SDK_INT <= 24) {
            barcodeET = findViewById(R.id.barcodeET);
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode,
                    data);
            barcodeET.setText(result.getContents());
        } else {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra("barcode");
                    EditText edit_text = findViewById(R.id.barcodeET);
                    if (barcode != null) {
                        edit_text.setText(barcode.displayValue);
                    }
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void displayData(byte[] byteData, Date date, int responseCode, String responseMessage, int requestType) {
        if (downloadingAlert != null) {
            downloadingAlert.dismiss();
        }
        String data = new String(byteData);
        if (!(responseCode < HttpURLConnection.HTTP_BAD_REQUEST) || data == null || data.length() <= 2) {
            if (responseCode == 403) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Lookup.this,
                                "The token is not correct.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(Lookup.this, GetToken.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        Lookup.this.startActivity(intent);
                    }
                });
            } else if (responseCode == 404) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Lookup.this,
                                "The URL is not correct.", Toast.LENGTH_LONG).show();
                        BaseActivity.editor.putString("urlText", "").apply();
                        Intent intent = new Intent(Lookup.this, GetToken.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        Lookup.this.startActivity(intent);
                    }
                });
            } else {
                runOnUiThread(() -> Toast.makeText(Lookup.this,
                        "There was an error looking up " + barcode + ".\n" +
                                "Does the barcode exist? " + responseCode, Toast.LENGTH_LONG).show());
            }
        } else {
            LookupLogicForDisplay lookupLogicForDisplayObj;
            lookupLogicForDisplayObj = new LookupLogicForDisplay();
            LookupDisplayObjInstance.getInstance().lookupLogicForDisplayObj
                    = lookupLogicForDisplayObj;
            lookupLogicForDisplayObj.setBarcodeQuery(barcode);
            try {
                lookupLogicForDisplayObj.processRawJSON(data);
            } catch (Exception e) {
                Toast.makeText(Lookup.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
            intent = new Intent(Lookup.this, LookupDisplay.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("barcode", barcode);
            Lookup.this.startActivity(intent);

            if (!lookupHistory.isEmpty()) {
                lastAdded = lookupHistory.get(0);
            }
            if (!barcode.equals(lastAdded)) {
                lookupHistory.add(0, barcode);
            }
        }
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
                    barcodeET.setText("");
                }
            });
        }
    }
}


