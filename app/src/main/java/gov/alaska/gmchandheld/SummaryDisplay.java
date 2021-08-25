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
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

public class SummaryDisplay extends BaseActivity {
    private ExpandableListView expandableListView;

    @Override
    public int getLayoutResource() {
        return R.layout.summary_display;
    }

    @Override
    protected void onStart() {
        super.onStart();
        EditText invisibleEditText = findViewById(R.id.invisibleEditText);
        invisibleEditText.setText("");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkAPIkeyExists(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        expandableListView = findViewById(R.id.expandableListView);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        final EditText invisibleEditText = findViewById(R.id.invisibleEditText);
        invisibleEditText.setInputType(InputType.TYPE_NULL);
        final RemoteApiUIHandler remoteApiUIHandler = new RemoteApiUIHandler();
        invisibleEditText.setFocusable(true);
        invisibleEditText.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                invisibleEditText.setText("");
            }
            if (invisibleEditText.getText().toString().trim().length() != 0) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode ==
                        KeyEvent.KEYCODE_ENTER)) {
                    remoteApiUIHandler.setDownloading(true);
                    RemoteApiUIHandler.setUrlFirstParameter(invisibleEditText.getText().toString());
                    new RemoteApiUIHandler.ProcessDataForDisplay(SummaryDisplay.this).execute();
                    return true;
                }
            } else {
                invisibleEditText.setText("");
            }
            return false;
        });
        SummaryLogicForDisplay summaryLogicForDisplayObj;
        summaryLogicForDisplayObj = SummaryDisplayObjInstance.getInstance().summaryLogicForDisplayObj;
        SpannableString title = new SpannableString(summaryLogicForDisplayObj.getBarcodeQuery());
        SpannableString subtitle = new SpannableString(
                summaryLogicForDisplayObj.getNumberOfBoxes() + " Result(s)");

        if (getSupportActionBar() != null) {
            if ("GMC Handheld".contentEquals(getSupportActionBar().getTitle())) {
                title.setSpan(new StyleSpan(Typeface.BOLD), 0, title.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                SummaryDisplay.this.getSupportActionBar().setTitle(title);

                if (summaryLogicForDisplayObj.getNumberOfBoxes() > 0) {
                    subtitle.setSpan(new ForegroundColorSpan(Color.BLACK), 0, subtitle.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    SummaryDisplay.this.getSupportActionBar().setSubtitle(subtitle);
                }
            }
        }
        if (summaryLogicForDisplayObj != null) {
            Intent intent = getIntent();
            //this barcode refers to the query barcode.
            String barcode = intent.getStringExtra("barcode");
            if (barcode != null) {
                title.setSpan(new StyleSpan(Typeface.BOLD), 0, title.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (summaryLogicForDisplayObj.getNumberOfBoxes() > 0) {
                    subtitle.setSpan(new ForegroundColorSpan(Color.BLACK), 0, subtitle.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            ExpandableListAdapter listAdapter = new LookupExpListAdapter(SummaryDisplay.this,
                    summaryLogicForDisplayObj.getKeyList(),
                    summaryLogicForDisplayObj.getDisplayDict());
            expandableListView.setAdapter(listAdapter);
            if (listAdapter.getGroupCount() >= 1) {
                for (int i = 0; i < listAdapter.getGroupCount(); i++) {
                    expandableListView.expandGroup(i);
                }
            }
            expandableListView.setOnGroupClickListener((parent, v, groupPosition, id) -> {
                return true; // This way the expander cannot be collapsed
            });
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        final EditText invisibleEditText = findViewById(R.id.invisibleEditText);
        String characterInput = String.valueOf(event.getUnicodeChar());
        invisibleEditText.setText(characterInput);
        invisibleEditText.setSelection(invisibleEditText.getText().length());
        invisibleEditText.requestFocus();
        invisibleEditText.setVisibility(View.VISIBLE);
        return super.onKeyDown(keyCode, event);
    }

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