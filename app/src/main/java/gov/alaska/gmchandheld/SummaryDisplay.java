package gov.alaska.gmchandheld;

import android.content.Context;
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
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SummaryDisplay extends BaseActivity {
    private ExpandableListView expandableListView;
    private EditText invisibleET;
    private String data;

    @Override
    public int getLayoutResource() {
        return R.layout.summary_display;
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
    protected void onRestart() {
        super.onRestart();
        invisibleET = findViewById(R.id.invisibleET);
        invisibleET.setText("");
        this.recreate();
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
                        processingAlert(this, barcode);
                        if (!barcode.isEmpty()) {
                            try {
                                barcode = URLEncoder.encode(barcode, "utf-8");
                            } catch (UnsupportedEncodingException e) {
//                                exception = new Exception(e.getMessage());
                            }

                            final String finalBarcode = barcode;
                            final String finalBase = baseURL;

                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    if (thread.isInterrupted()) {
                                        return;
                                    }
                                    final ExecutorService service;
                                    final Future<String> task;

                                    service = Executors.newFixedThreadPool(1);
                                    task = service.submit(new NewRemoteAPIDownload(finalBase
                                            + "summary.json?barcode=" + finalBarcode));

                                    try {
                                        data = task.get();
                                    } catch (final InterruptedException ex) {
                                        ex.printStackTrace();
                                    } catch (final ExecutionException ex) {
                                        ex.printStackTrace();
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
                                                Toast.makeText(SummaryDisplay.this,
                                                        "There was an error looking up " + finalBarcode + ".", Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    } else {
                                        SummaryLogicForDisplay summaryLogicForDisplayObj =
                                                new SummaryLogicForDisplay();
                                        SummaryDisplayObjInstance.getInstance()
                                                .summaryLogicForDisplayObj
                                                = summaryLogicForDisplayObj;
                                        summaryLogicForDisplayObj.setBarcodeQuery(finalBarcode);
                                        try {
                                            summaryLogicForDisplayObj.processRawJSON(data);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        intent = new Intent(SummaryDisplay.this,
                                                SummaryDisplay.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        intent.putExtra("barcode", finalBarcode);
                                        startActivity(intent);

                                        if (!Summary.getSummaryHistory().isEmpty()) {
                                            Summary.setLastAdded(Summary.getSummaryHistory().get(0));
                                        }
                                        if (!finalBarcode.equals(Lookup.getLastAdded())
                                                & !finalBarcode.isEmpty()) {
                                            Summary.getSummaryHistory().add(0, finalBarcode);
                                        }
                                    }
                                    downloading = false;
                                }
                            };
                            invisibleET.setText("");
                            thread = new Thread(runnable);
                            thread.start();
                            return true;
                        }
                    }
                }
                return false;
            });
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
            String barcode = intent.getStringExtra("barcode"); //refers to the query barcode.
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
                    expandableListView.smoothScrollToPosition( expandableListView.getCount());
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