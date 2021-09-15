package gov.alaska.gmchandheld;

import android.content.Context;
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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LookupDisplay extends BaseActivity {
    private ExpandableListView expandableListView;
    private EditText invisibleET;
    private volatile String data;

    @Override
    public int getLayoutResource() {
        return R.layout.lookup_display;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        invisibleET = findViewById(R.id.invisibleET);
        invisibleET.setText("");
        this.recreate();
    }

    @Override
	protected void onStop() {
        super.onStop();
        if (alert != null) {
            alert.dismiss();
            alert = null;
        }
    }

    @Override
	protected void onDestroy() {
        super.onDestroy();
        if (alert != null) {
            alert.dismiss();
            alert = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkAPIkeyExists(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        expandableListView = findViewById(R.id.expandableListView);
        invisibleET = findViewById(R.id.invisibleET);
        invisibleET.setInputType(InputType.TYPE_NULL);

        if (!downloading) {
            downloading = true;
            invisibleET.setFocusable(true);
            invisibleET.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    invisibleET.setText("");
                }
                if (invisibleET.getText().toString().trim().length() != 0) {
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                            (keyCode == KeyEvent.KEYCODE_ENTER)) {

                        String barcode = invisibleET.getText().toString();
                        processingAlert(LookupDisplay.this, barcode);
                        if (!barcode.isEmpty()) {
                            try {
                                barcode = URLEncoder.encode(barcode, "utf-8");
                            } catch (UnsupportedEncodingException e) {
//                                exception = new Exception(e.getMessage());
                            }
                            final String finalBarcode = barcode;
                            final String finalBase = baseURL;

//                            Runnable runnable = new Runnable() {
//                                @Override
//                                public void run() {
//                                    if (thread.isInterrupted()) {
//                                        return;
//                                    }
//                                    final ExecutorService service;
//                                    final Future<String> task;
//
//                                    service = Executors.newFixedThreadPool(1);
//                                    task = service.submit(new RemoteAPIDownload(finalBase
//                                            + "inventory.json?barcode=" + finalBarcode));
//
//                                    try {
//                                        data = task.get();
//                                    } catch (final InterruptedException ex) {
//                                        ex.printStackTrace();
//                                    } catch (final ExecutionException ex) {
//                                        ex.printStackTrace();
//                                    }
//                                    service.shutdownNow();
//                                    if (data == null || data.length() <= 2) {
//                                        if (alert != null){
//                                            alert.dismiss();
//                                            alert = null;
//                                        }
//                                        runOnUiThread(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                Toast.makeText(LookupDisplay.this,
//                                                        "There was an error looking up " + finalBarcode + ".", Toast.LENGTH_LONG).show();
//                                            }
//                                        });
//
//                                    } else {
//                                        LookupLogicForDisplay lookupLogicForDisplayObj =
//                                                new LookupLogicForDisplay();
//                                        LookupDisplayObjInstance.getInstance()
//                                                .lookupLogicForDisplayObj
//                                                = lookupLogicForDisplayObj;
//                                        lookupLogicForDisplayObj.setBarcodeQuery(finalBarcode);
//                                        try {
//                                            lookupLogicForDisplayObj.processRawJSON(data);
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//                                        intent = new Intent(LookupDisplay.this,
//                                                LookupDisplay.class);
//                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
//                                                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                        intent.putExtra("barcode", finalBarcode);
//
//                                        startActivity(intent);
//
//                                        if (!Lookup.getLookupHistory().isEmpty()) {
//                                            Lookup.setLastAdded(Lookup.getLookupHistory().get(0));
//                                        }
//                                        if (!finalBarcode.equals(Lookup.getLastAdded()) & !finalBarcode.isEmpty()) {
//                                            Lookup.getLookupHistory().add(0, finalBarcode);
//                                        }
//                                    }
//                                    downloading = false;
//                                }
//                            };
//                            invisibleET.setText("");
//
//                            thread = new Thread(runnable);
//                            thread.start();
                            return true;
                        }
                    }
                }
                return false;
            });

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
        switch (event.getKeyCode()) {
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
}