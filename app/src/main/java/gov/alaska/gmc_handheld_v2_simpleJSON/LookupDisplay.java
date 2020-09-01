package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

public class LookupDisplay extends BaseActivity {
	ExpandableListView expandableListView;
	ExpandableListAdapter listAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lookup);

		getSupportActionBar().setDisplayShowHomeEnabled(true);

		LookupProcessJsonForDisplay lookupProcessJsonForDisplayObj = new LookupProcessJsonForDisplay();

		Intent intent = getIntent();
		String barcode = intent.getStringExtra("barcode");
		String rawJSON = intent.getStringExtra("rawJSON");

		try {
			lookupProcessJsonForDisplayObj.processRawJSON(rawJSON);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (barcode != null) {
			LookupDisplay.this.getSupportActionBar().setTitle(barcode);

			if (lookupProcessJsonForDisplayObj.getKeyList().size() > 0) {
				LookupDisplay.this.getSupportActionBar().setSubtitle(lookupProcessJsonForDisplayObj.getKeyList().size() + " Result(s)");
			}
		}

		expandableListView = findViewById(R.id.expandableListView);
		listAdapter = new LookupExpListAdapter(LookupDisplay.this, lookupProcessJsonForDisplayObj.getKeyList(), lookupProcessJsonForDisplayObj.getDisplayDict());
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