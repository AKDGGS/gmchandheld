package gov.alaska.gmchandheld;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.HashMap;

public class LookupDisplay extends BaseActivity implements HTTPRequestCallback {
    private ExpandableListView expandableListView;
    private String barcode;
    private ProgressDialog downloadingAlert;
    private StringBuilder sb = new StringBuilder();

    @Override
    public int getLayoutResource() {
        return R.layout.lookup_display;
    }

    @Override
    protected void onRestart() {
        super.onRestart();

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        expandableListView = findViewById(R.id.expandableListView);
        LookupLogicForDisplay lookupLogicForDisplayObj = LookupDisplayObjInstance
                .getInstance().lookupLogicForDisplayObj;
        SpannableString title = new SpannableString(lookupLogicForDisplayObj.getBarcodeQuery());
        SpannableString subtitle = new SpannableString(
                lookupLogicForDisplayObj.getKeyList().size() + " Result(s)");
        if (getSupportActionBar() != null) {
            if ("GMC Handheld".contentEquals(getSupportActionBar().getTitle())) {
                title.setSpan(new StyleSpan(Typeface.BOLD), 0, title.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                LookupDisplay.this.getSupportActionBar().setTitle(title);
                if (lookupLogicForDisplayObj.getKeyList().size() > 0) {
                    subtitle.setSpan(new ForegroundColorSpan(Color.BLACK), 0,
                            subtitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    LookupDisplay.this.getSupportActionBar().setSubtitle(subtitle);
                }
                if (lookupLogicForDisplayObj.getRadiationWarningFlag()) {
                    LookupDisplay.this.getSupportActionBar()
                            .setBackgroundDrawable(new ColorDrawable(ContextCompat
                                    .getColor(this, R.color.colorRadiation)));
                }
            }
        }
        if (getIntent().getStringExtra("barcode") != null) {
            title.setSpan(new StyleSpan(Typeface.BOLD), 0, title.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        ExpandableListAdapter listAdapter = new LookupExpListAdapter(LookupDisplay.this,
                lookupLogicForDisplayObj.getKeyList(), lookupLogicForDisplayObj.getDisplayDict());
        expandableListView.setAdapter(listAdapter);
        if (listAdapter.getGroupCount() >= 1) {
            for (int i = 0; i < listAdapter.getGroupCount(); i++) {
                expandableListView.expandGroup(i);
            }
        }
        expandableListView.setOnGroupClickListener((parent, v, groupPosition, id) -> {
            return true; // This prevents the expander from being collapsed
        });

        if (BaseActivity.getUpdatable()) {  //Set in UpdateBroadcastReceiver and Configuration
            downloadingAlert();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        LookupLogicForDisplay lookupLogicForDisplayObj;
        lookupLogicForDisplayObj = LookupDisplayObjInstance.getInstance().lookupLogicForDisplayObj;
        if (lookupLogicForDisplayObj.getDisplayDict().toString().contains("radiation_risk")) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.radiation_menu, menu);
        } else {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.main_menu, menu);
        }
        return true;
    }


    //makes the volume keys scroll up/down
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        AudioManager manager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        manager.adjustVolume(AudioManager.ADJUST_RAISE, 0);
        manager.adjustVolume(AudioManager.ADJUST_LOWER, 0);
        if ((event.isPrintingKey()) && (action == KeyEvent.ACTION_DOWN)) {
            sb = sb.append((char)event.getUnicodeChar());
        }
        switch (event.getAction()){
            case KeyEvent.ACTION_DOWN:{
                switch (event.getKeyCode()){
                    case KeyEvent.KEYCODE_ENTER:
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
                                getHTTPRequest().setFetchDataObj(baseURL + "inventory.json?",
                                        this,
                                        0,
                                        params,
                                        null);
                            } catch (Exception e) {
                                System.out.println("Lookup Display Exception: " + e.getMessage());
                                Toast.makeText(LookupDisplay.this,
                                        "The there is a problem. " + e.getMessage(), Toast.LENGTH_LONG).show();
                                thread.interrupt();
                                if (downloadingAlert != null) {
                                    downloadingAlert.dismiss();
                                }
                            }
                            return true;
                        }
                    case KeyEvent.KEYCODE_DEL:
                        sb.setLength(0);
                    case KeyEvent.KEYCODE_DPAD_UP:
                    case KeyEvent.KEYCODE_VOLUME_UP: {
                        if (event.isLongPress()) {
                            expandableListView.smoothScrollToPosition(0, 0);
                        }
                        return true;
                    }
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                    case KeyEvent.KEYCODE_VOLUME_DOWN: {
                        if (event.isLongPress()) {
                            expandableListView.smoothScrollToPosition(expandableListView.getCount());
                        }
                        return true;
                    }
                }
            }
            case KeyEvent.ACTION_UP:{
                switch (event.getKeyCode()){
                    case KeyEvent.KEYCODE_DPAD_UP:
                    case KeyEvent.KEYCODE_VOLUME_UP: {
                        expandableListView.smoothScrollByOffset(-3);
                        return true;
                    }
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                    case KeyEvent.KEYCODE_VOLUME_DOWN: {
                        expandableListView.smoothScrollByOffset(3);
                        return true;
                    }
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void displayData(byte[] byteData, Date date, int responseCode, String responseMessage,
                            int requestType) {
        if (downloadingAlert != null) {
            downloadingAlert.dismiss();
        }
        String data = new String(byteData);
        if (!(responseCode < HttpURLConnection.HTTP_BAD_REQUEST) || data == null || data.length() <= 2) {
            switch (responseCode){
                case 403:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LookupDisplay.this,
                                    "The token is not correct.", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(LookupDisplay.this, GetToken.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            LookupDisplay.this.startActivity(intent);
                        }
                    });
                case 404:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LookupDisplay.this,
                                    "The URL is not correct.", Toast.LENGTH_LONG).show();
                            BaseActivity.editor.putString("urlText", "").apply();
                            Intent intent = new Intent(LookupDisplay.this, GetToken.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            LookupDisplay.this.startActivity(intent);
                        }
                    });
                default:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LookupDisplay.this,
                                    "There was an error looking up " + barcode + ".\n" +
                                            "Is the barcode in inventory?", Toast.LENGTH_LONG).show();
                            sb = new StringBuilder();
                            barcode = "";
                        }
                    });
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
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
            intent = new Intent(LookupDisplay.this, LookupDisplay.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("barcode", barcode);
            LookupDisplay.this.startActivity(intent);

            if (!Lookup.getLookupHistory().isEmpty()) {
                Lookup.setLastAdded(Lookup.getLookupHistory().get(0));
            }
            if (!barcode.equals(Lookup.getLastAdded()) & !barcode.isEmpty()) {
                Lookup.getLookupHistory().add(0, barcode);
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