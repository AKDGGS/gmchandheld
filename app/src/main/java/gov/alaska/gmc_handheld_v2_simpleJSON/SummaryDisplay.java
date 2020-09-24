package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

public class SummaryDisplay extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.summary_display);

//        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final EditText invisibleEditText = findViewById(R.id.invisibleEditText);

        invisibleEditText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // if "enter" is pressed
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    invisibleEditText.setVisibility(View.VISIBLE);

                }
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    final OpenLookup openLookupObj = new OpenLookup();
                    System.out.println(invisibleEditText.getText().toString());
                    openLookupObj.processDataForDisplay(invisibleEditText.getText().toString(), SummaryDisplay.this);
                    return true;
                }
                return false;
            }
        });

        SummaryLogicForDisplay summaryLogicForDisplayObj;
        summaryLogicForDisplayObj = SummaryDisplayObjInstance.instance().summaryLogicForDisplay;

        if (summaryLogicForDisplayObj != null) {
            Intent intent = getIntent();
            String barcode = intent.getStringExtra("barcode");  //this barcode refers to the query barcode.

//            if (barcode != null) {
//                SummaryDisplay.this.getSupportActionBar().setTitle(barcode);
//
//                if (summaryLogicForDisplayObj.getKeyList().size() > 0) {
//                    SummaryDisplay.this.getSupportActionBar().setSubtitle(summaryLogicForDisplayObj.getKeyList().size() + " Result(s)");
//                }
//            }

            ExpandableListView expandableListView = findViewById(R.id.expandableListView);
            ExpandableListAdapter listAdapter = new LookupExpListAdapter(SummaryDisplay.this, summaryLogicForDisplayObj.getKeyList(), summaryLogicForDisplayObj.getDisplayDict());
            expandableListView.setAdapter(listAdapter);
        }
    }
}