package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

public class LookupDisplay extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lookup);

		getSupportActionBar().setDisplayShowHomeEnabled(true);

		LookupLogicForDisplay lookupLogicForDisplayObj = new LookupLogicForDisplay();

		Intent intent = getIntent();
		String barcode = intent.getStringExtra("barcode");

		SharedPreferences sp = getApplicationContext().getSharedPreferences("userPreferences", Context.MODE_PRIVATE);
		String rawJSON = sp.getString("downloadedDataString", "");

		//The downloaded data is not preserved between downloads
		sp.edit().remove("downloadDataString").commit();

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

		ExpandableListView expandableListView = findViewById(R.id.expandableListView);
		ExpandableListAdapter listAdapter = new LookupExpListAdapter(LookupDisplay.this, lookupLogicForDisplayObj.getKeyList(), lookupLogicForDisplayObj.getDisplayDict());
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