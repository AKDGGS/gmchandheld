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

		LookupBuildTree lookupBuildTreeObj;
		lookupBuildTreeObj = Bridge.instance().lookupBuildTree;

		Intent intent = getIntent();
		String barcode = intent.getStringExtra("barcode");

		if (barcode != null) {
			LookupDisplay.this.getSupportActionBar().setTitle(barcode);

			if (lookupBuildTreeObj.getKeyList().size() > 0) {
				LookupDisplay.this.getSupportActionBar().setSubtitle(lookupBuildTreeObj.getKeyList().size() + " Result(s)");
			}
		}

		expandableListView = findViewById(R.id.expandableListView);
		listAdapter = new LookupExpListAdapter(LookupDisplay.this, lookupBuildTreeObj.getKeyList(), lookupBuildTreeObj.getDisplayDict());
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