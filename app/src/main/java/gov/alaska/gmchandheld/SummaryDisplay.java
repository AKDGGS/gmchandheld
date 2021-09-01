package gov.alaska.gmchandheld;

import android.content.Context;
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
    EditText invisibleET;

    @Override
    public int getLayoutResource() {
        return R.layout.summary_display;
    }

    @Override
    protected void onStart() {
        super.onStart();
        EditText invisibleET = findViewById(R.id.invisibleET);
        invisibleET.setText("");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkAPIkeyExists(this);
        expandableListView = findViewById(R.id.expandableListView);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        invisibleET = findViewById(R.id.invisibleET);
        invisibleET.setInputType(InputType.TYPE_NULL);
        invisibleET.setFocusable(true);
        invisibleET.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                invisibleET.setText("");
            }
            if (invisibleET.getText().toString().trim().length() != 0) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode ==
                        KeyEvent.KEYCODE_ENTER)) {
                    new RemoteApiUIHandler(this, invisibleET.getText().toString()).execute();
                    invisibleET.setText("");
                    return true;
                }
            } else {
                invisibleET.setText("");
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        String characterInput = String.valueOf(event.getUnicodeChar());
        invisibleET.setText(characterInput);
        invisibleET.setSelection(invisibleET.getText().length());
        invisibleET.requestFocus();
        invisibleET.setVisibility(View.VISIBLE);
        return super.onKeyDown(keyCode, event);
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