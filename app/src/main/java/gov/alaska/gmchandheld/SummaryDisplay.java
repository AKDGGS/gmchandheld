package gov.alaska.gmchandheld;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.util.Date;
import java.util.HashMap;

public class SummaryDisplay extends BaseActivity implements HTTPRequestCallback {
    private ExpandableListView expandableListView;
    private String barcode;
    private StringBuilder sb = new StringBuilder();
    private ProgressDialog downloadingAlert;

    @Override
    public int getLayoutResource() {
        return R.layout.summary_display;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (downloadingAlert != null) {
            downloadingAlert.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (downloadingAlert != null) {
            downloadingAlert.dismiss();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        expandableListView = findViewById(R.id.expandableListView);
        SummaryLogicForDisplay summaryLogicForDisplayObj;
        summaryLogicForDisplayObj = SummaryDisplayObjInstance.getInstance()
                .summaryLogicForDisplayObj;
        SpannableString title = new SpannableString(summaryLogicForDisplayObj.getBarcodeQuery());
        SpannableString subtitle = new SpannableString(
                summaryLogicForDisplayObj.getNumberOfBoxes() + " Result(s)");
        if (getSupportActionBar() != null) {
            if ("GMC Handheld".contentEquals(getSupportActionBar().getTitle())) {
                title.setSpan(new StyleSpan(Typeface.BOLD), 0, title.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                this.getSupportActionBar().setTitle(title);
                if (summaryLogicForDisplayObj.getNumberOfBoxes() > 0) {
                    subtitle.setSpan(new ForegroundColorSpan(Color.BLACK),
                            0,
                            subtitle.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    this.getSupportActionBar().setSubtitle(subtitle);
                }
            }
        }
        intent = getIntent();
        barcode = intent.getStringExtra("barcode"); //refers to the query barcode.
        if (barcode != null) {
            title.setSpan(new StyleSpan(Typeface.BOLD), 0, title.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (summaryLogicForDisplayObj.getNumberOfBoxes() > 0) {
                subtitle.setSpan(new ForegroundColorSpan(Color.BLACK),
                        0,
                        subtitle.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        ExpandableListAdapter listAdapter = new LookupExpListAdapter(this,
                summaryLogicForDisplayObj.getKeyList(),
                summaryLogicForDisplayObj.getDisplayDict());
        expandableListView.setAdapter(listAdapter);
        if (listAdapter.getGroupCount() >= 1) {
            for (int i = 0; i < listAdapter.getGroupCount(); i++) {
                expandableListView.expandGroup(i);
            }
        }
        expandableListView.setOnGroupClickListener((parent, v, groupPosition, id) -> {
            return true; // Expander cannot be collapsed
        });
        if (BaseActivity.getUpdatable()) { //Set in UpdateBroadcastReceiver and Configuration
            downloadingAlert();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        AudioManager manager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        manager.adjustVolume(AudioManager.ADJUST_RAISE, 0);
        manager.adjustVolume(AudioManager.ADJUST_LOWER, 0);
        if ((event.isPrintingKey()) && (action == KeyEvent.ACTION_DOWN)) {
            sb = sb.append((char)event.getUnicodeChar());
        }
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_ENTER:
                if (action == KeyEvent.ACTION_DOWN){
                    barcode = sb.toString();
                    downloadingAlert = new ProgressDialog(this);
                    downloadingAlert.setMessage("Loading...\n " + barcode);
                    downloadingAlert.setCancelable(false);
                    downloadingAlert.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            thread.interrupt();
                            downloadingAlert.dismiss();//dismiss dialog
                        }
                    });
                    downloadingAlert.show();
                    if (!barcode.isEmpty()) {
                        HashMap<String, Object> params = new HashMap<>();
                        params.put("barcode", barcode);
                        try {
                            getHTTPRequest().setFetchDataObj(baseURL + "summary.json?",
                                    this,
                                    0,
                                    params,
                                    null);
                        } catch (Exception e) {
                            System.out.println("Summary Display Exception: " + e.getMessage());
                        }
                        return true;
                    }
                }
            case KeyEvent.KEYCODE_DEL:
                if(sb.length()!=0) {
                    sb = new StringBuilder();
                }
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_VOLUME_UP: {
                if (action == KeyEvent.ACTION_DOWN && event.isLongPress()) {
                    expandableListView.smoothScrollToPosition(0, 0);
                }
                if (KeyEvent.ACTION_UP == action) {
                    expandableListView.smoothScrollByOffset(-3);
                }
                return true;
            }
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_VOLUME_DOWN: {
                if (action == KeyEvent.ACTION_DOWN && event.isLongPress()) {
                    expandableListView.smoothScrollToPosition(expandableListView.getCount());
                }
                if (KeyEvent.ACTION_UP == action) {
                    expandableListView.smoothScrollByOffset(3);
                }
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
        if (!(responseCode < HttpURLConnection.HTTP_BAD_REQUEST) || data == null) {
            if (responseCode == 403) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SummaryDisplay.this,
                                "The token is not correct.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(SummaryDisplay.this, GetToken.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        SummaryDisplay.this.startActivity(intent);
                    }
                });
            } else if (responseCode == 404) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SummaryDisplay.this,
                                "The URL is not correct.", Toast.LENGTH_LONG).show();
                        BaseActivity.editor.putString("urlText", "").apply();
                        Intent intent = new Intent(SummaryDisplay.this, GetToken.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        SummaryDisplay.this.startActivity(intent);
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SummaryDisplay.this,
                                "There was an error looking up " + barcode + ".\n" +
                                        "Is the barcode a container?", Toast.LENGTH_LONG).show();
                        sb = new StringBuilder();
                        barcode = "";

                    }
                });
            }
        } else {
            SummaryLogicForDisplay summaryLogicForDisplayObj =
                    new SummaryLogicForDisplay();
            SummaryDisplayObjInstance.getInstance()
                    .summaryLogicForDisplayObj
                    = summaryLogicForDisplayObj;
            summaryLogicForDisplayObj.setBarcodeQuery(barcode);
            try {
                summaryLogicForDisplayObj.processRawJSON(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
            intent = new Intent(SummaryDisplay.this,
                    SummaryDisplay.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("barcode", barcode);
            startActivity(intent);
            if (!Summary.getSummaryHistory().isEmpty()) {
                Summary.setLastAdded(Summary.getSummaryHistory().get(0));
            }
            if (!barcode.equals(Lookup.getLastAdded())
                    & !barcode.isEmpty()) {
                Summary.getSummaryHistory().add(0, barcode);
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
                    sb = new StringBuilder();
                    barcode = "";
                }
            });
        }
    }
}