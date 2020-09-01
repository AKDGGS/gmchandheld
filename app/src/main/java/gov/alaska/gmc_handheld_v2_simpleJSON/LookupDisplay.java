package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

public class LookupDisplay extends BaseActivity {
	private ExpandableListView expandableListView;
	private ExpandableListAdapter listAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lookup);

		getSupportActionBar().setDisplayShowHomeEnabled(true);

		LookupLogicForDisplay lookupLogicForDisplayObj = new LookupLogicForDisplay();

		Intent intent = getIntent();
		String barcode = intent.getStringExtra("barcode");
		String rawJSON = intent.getStringExtra("rawJSON");

		try {
			lookupLogicForDisplayObj.processRawJSON(rawJSON);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (barcode != null) {
			LookupDisplay.this.getSupportActionBar().setTitle(barcode);

			if (lookupLogicForDisplayObj.getKeyList().size() > 0) {
				LookupDisplay.this.getSupportActionBar().setSubtitle(lookupLogicForDisplayObj.getKeyList().size() + " Result(s)");
			}
		}

		expandableListView = findViewById(R.id.expandableListView);
		listAdapter = new LookupExpListAdapter(LookupDisplay.this, lookupLogicForDisplayObj.getKeyList(), lookupLogicForDisplayObj.getDisplayDict());
		expandableListView.setAdapter(listAdapter);

		if (listAdapter.getGroupCount() >= 1) {
			//expands only the first element
//							expandableListView.expandGroup(0);
			//expands all
			for (int i = 0; i< listAdapter.getGroupCount(); i++) {
				expandableListView.expandGroup(i);
			}
		}
	}

}