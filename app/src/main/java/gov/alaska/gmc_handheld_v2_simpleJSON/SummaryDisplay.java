package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

public class SummaryDisplay extends BaseActivity {

    public static final String SHARED_PREFS = "sharedPrefs";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.summary_display);

        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final EditText invisibleEditText = findViewById(R.id.invisibleEditText);

        invisibleEditText.setInputType(InputType.TYPE_NULL);
        invisibleEditText.setVisibility(View.GONE);

        invisibleEditText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    invisibleEditText.setText("");
                }

                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    final OpenLookup openLookupObj = new OpenLookup();
                    openLookupObj.processDataForDisplay(invisibleEditText.getText().toString(), SummaryDisplay.this);
                    return true;
                }
                return false;
            }
        });

        SummaryLogicForDisplay summaryLogicForDisplayObj;
        summaryLogicForDisplayObj = SummaryDisplayObjInstance.instance().summaryLogicForDisplayObj;

        if("GMC_handheld".equals(getSupportActionBar().getTitle())){
            SummaryDisplay.this.getSupportActionBar().setTitle(Html.fromHtml("<strong> <small> <font color='#000000'>" + summaryLogicForDisplayObj.getBarcodeQuery() +"</font> </small> </strong>"));
            if (summaryLogicForDisplayObj.getKeyList().size() > 0) {
                SummaryDisplay.this.getSupportActionBar().setSubtitle(Html.fromHtml("<font color='#000000'>" + summaryLogicForDisplayObj.getKeyList().size() + " Result(s) </font>"));
            }

//            if(summaryLogicForDisplayObj.getradiationWarningFlag()) {
//                LookupDisplay.this.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.colorRadiation)));
//            }

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

            ExpandableListView expandableListView = findViewById(R.id.expandableListView);
            ExpandableListAdapter listAdapter = new LookupExpListAdapter(SummaryDisplay.this, summaryLogicForDisplayObj.getKeyList(), summaryLogicForDisplayObj.getDisplayDict());
            expandableListView.setAdapter(listAdapter);

            if (listAdapter.getGroupCount() >= 1) {
                for (int i = 0; i < listAdapter.getGroupCount(); i++) {
                    expandableListView.expandGroup(i);
                }
            }
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

}