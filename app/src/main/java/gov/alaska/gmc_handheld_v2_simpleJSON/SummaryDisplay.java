package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

public class SummaryDisplay extends BaseActivity {

    private ExpandableListView expandableListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.summary_display);
        expandableListView = findViewById(R.id.expandableListView);


        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final EditText invisibleEditText = findViewById(R.id.invisibleEditText);

        invisibleEditText.setInputType(InputType.TYPE_NULL);
        final RemoteAPITask remoteAPITaskObj = new RemoteAPITask();

        remoteAPITaskObj.setDownloading(true);
        invisibleEditText.setFocusable(true);

        invisibleEditText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    invisibleEditText.setText("");
                }

                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    remoteAPITaskObj.processDataForDisplay(invisibleEditText.getText().toString(), null,SummaryDisplay.this);
                    return true;
                }
                return false;
            }
        });

        SummaryLogicForDisplay summaryLogicForDisplayObj;
        summaryLogicForDisplayObj = SummaryDisplayObjInstance.instance().summaryLogicForDisplayObj;

        if("GMC Handheld".contentEquals(getSupportActionBar().getTitle())){
            SummaryDisplay.this.getSupportActionBar().setTitle(Html.fromHtml("<strong> <small> <font color='#000000'>" + summaryLogicForDisplayObj.getBarcodeQuery() +"</font> </small> </strong>"));
            if (summaryLogicForDisplayObj.getKeyList().size() > 0) {
                SummaryDisplay.this.getSupportActionBar().setSubtitle(Html.fromHtml("<font color='#000000'>" + summaryLogicForDisplayObj.getKeyList().size() + " Result(s) </font>"));
            }
        }

        if (summaryLogicForDisplayObj != null) {
            Intent intent = getIntent();
            String barcode = intent.getStringExtra("barcode");  //this barcode refers to the query barcode.

            if (barcode != null) {
                SummaryDisplay.this.getSupportActionBar().setTitle(barcode);

                if (summaryLogicForDisplayObj.getKeyList().size() > 0) {
                    SummaryDisplay.this.getSupportActionBar().setSubtitle(summaryLogicForDisplayObj.getKeyList().size() + " Result(s)");
                }
            }

            ExpandableListAdapter listAdapter = new LookupExpListAdapter(SummaryDisplay.this, summaryLogicForDisplayObj.getKeyList(), summaryLogicForDisplayObj.getDisplayDict());
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
                    return true; // This way the expander cannot be collapsed
                }
            });
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        final EditText invisibleEditText = findViewById(R.id.invisibleEditText);

        invisibleEditText.setText((char) event.getUnicodeChar() + "");
        invisibleEditText.setSelection(invisibleEditText.getText().length());
        invisibleEditText.requestFocus();
        invisibleEditText.setVisibility(View.VISIBLE);
        return super.onKeyDown(keyCode, event);
    }

//    @Override
//    public void onBackPressed() {
//        Intent get_barcode = new Intent(this, Summary.class);
//        startActivity(get_barcode);
//    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event){
        int action, keycode;

        action = event.getAction();
        keycode = event.getKeyCode();
        AudioManager manager = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);


        switch (keycode){
            case KeyEvent.KEYCODE_VOLUME_UP:{
                manager.adjustVolume(AudioManager.ADJUST_RAISE, 0);
                manager.adjustVolume(AudioManager.ADJUST_LOWER, 0);

                if(KeyEvent.ACTION_UP == action){

                    expandableListView.smoothScrollByOffset(3);
                }
                return true;
            }
            case KeyEvent.KEYCODE_VOLUME_DOWN:{

                if(KeyEvent.ACTION_UP == action){
                    expandableListView.smoothScrollByOffset(-3);
                }
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }
}