package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

public class LookupDisplay extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lookup);

		getSupportActionBar().setDisplayShowHomeEnabled(true);

		LookupLogicForDisplay lookupLogicForDisplayObj;
		lookupLogicForDisplayObj = Bridge.instance().lookupLogicForDisplayObj;

		if (lookupLogicForDisplayObj != null) {
			Intent intent = getIntent();
			String barcode = intent.getStringExtra("barcode");  //this barcode refers to the query barcode.

			if (barcode != null) {
				LookupDisplay.this.getSupportActionBar().setTitle(barcode);

				if (lookupLogicForDisplayObj.getKeyList().size() > 0) {
					LookupDisplay.this.getSupportActionBar().setSubtitle(lookupLogicForDisplayObj.getKeyList().size() + " Result(s)");
				}
			}

			ExpandableListView expandableListView = findViewById(R.id.expandableListView);
			ExpandableListAdapter listAdapter = new LookupExpListAdapter(LookupDisplay.this, lookupLogicForDisplayObj.getKeyList(), lookupLogicForDisplayObj.getDisplayDict());
			expandableListView.setAdapter(listAdapter);

			if (listAdapter.getGroupCount() >= 1) {
				//expands only the first element
//							expandableListView.expandGroup(0);
				//expands all
				for (int i = 0; i < listAdapter.getGroupCount(); i++) {
					expandableListView.expandGroup(i);
				}
			}
		}
	}
}