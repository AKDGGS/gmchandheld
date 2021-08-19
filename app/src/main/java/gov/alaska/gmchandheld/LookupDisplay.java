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
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

public class LookupDisplay extends BaseActivity {
    private ExpandableListView expandableListView;
    private EditText invisibleEditText;

    @Override
    public int getLayoutResource() {
        return R.layout.lookup_display;
    }

    @Override
    protected void onStart() {
        super.onStart();
        invisibleEditText = findViewById(R.id.invisibleEditText);
        invisibleEditText.setText("");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        expandableListView = findViewById(R.id.expandableListView);

        invisibleEditText = findViewById(R.id.invisibleEditText);
        invisibleEditText.setInputType(InputType.TYPE_NULL);
        final RemoteApiUIHandler remoteApiUIHandler = new RemoteApiUIHandler();
        invisibleEditText.setFocusable(true);
        invisibleEditText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    invisibleEditText.setText("");
                }
                if (invisibleEditText.getText().toString().trim().length() != 0) {
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        remoteApiUIHandler.setDownloading(true);
                        RemoteApiUIHandler.setUrlFirstParameter(invisibleEditText.getText().toString());
                        new RemoteApiUIHandler.ProcessDataForDisplay(LookupDisplay.this).execute();
                        return true;
                    }
                } else {
                    invisibleEditText.setText("");
                }
                return false;
            }
        });
        LookupLogicForDisplay lookupLogicForDisplayObj = LookupDisplayObjInstance.getInstance().lookupLogicForDisplayObj;
        SpannableString title = new SpannableString(lookupLogicForDisplayObj.getBarcodeQuery());
        SpannableString subtitle = new SpannableString(lookupLogicForDisplayObj.getKeyList().size() + " Result(s)");
        if (getSupportActionBar() != null) {
            if ("GMC Handheld".contentEquals(getSupportActionBar().getTitle())) {
                title.setSpan(new StyleSpan(Typeface.BOLD), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                LookupDisplay.this.getSupportActionBar().setTitle(title);
                if (lookupLogicForDisplayObj.getKeyList().size() > 0) {
                    subtitle.setSpan(new ForegroundColorSpan(Color.BLACK), 0, subtitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    LookupDisplay.this.getSupportActionBar().setSubtitle(subtitle);
                }
                if (lookupLogicForDisplayObj.getRadiationWarningFlag()) {
                    LookupDisplay.this.getSupportActionBar()
                                      .setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.colorRadiation)));
                }
            }
        }
        if (lookupLogicForDisplayObj != null) {
            Intent intent = getIntent();
            String barcode = intent.getStringExtra("barcode");  //this barcode refers to the query barcode.
            if (barcode != null) {
                title.setSpan(new StyleSpan(Typeface.BOLD), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

//                LookupDisplay.this.getSupportActionBar().setTitle(title);
//                if (lookupLogicForDisplayObj.getRadiationWarningFlag()) {
//                    LookupDisplay.this.getSupportActionBar()
//                                      .setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.colorRadiation)));
//                }
//                if (lookupLogicForDisplayObj.getKeyList().size() > 0) {
//                    subtitle.setSpan(new ForegroundColorSpan(Color.BLACK), 0, subtitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                    LookupDisplay.this.getSupportActionBar().setSubtitle(subtitle);
//                }
            }
            ExpandableListAdapter listAdapter = new LookupExpListAdapter(LookupDisplay.this, lookupLogicForDisplayObj.getKeyList(), lookupLogicForDisplayObj.getDisplayDict());
            expandableListView.setAdapter(listAdapter);
            if (listAdapter.getGroupCount() >= 1) {
                for (int i = 0; i < listAdapter.getGroupCount(); i++) {
                    expandableListView.expandGroup(i);
                }
            }
            expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                @Override
                public boolean onGroupClick(ExpandableListView parent, View v,
                                            int groupPosition, long id) {
                    return true; // This prevents the expander from being collapsed
                }
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