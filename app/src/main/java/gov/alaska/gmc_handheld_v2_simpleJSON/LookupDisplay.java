package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;


public class LookupDisplay extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lookup_display);

		getSupportActionBar().setDisplayShowHomeEnabled(true);


		final EditText invisibleEditText = findViewById(R.id.invisibleEditText);

		invisibleEditText.setVisibility(View.GONE);

		invisibleEditText.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
					final OpenLookup openLookupObj = new OpenLookup();
					openLookupObj.processDataForDisplay(invisibleEditText.getText().toString(), LookupDisplay.this);
					return true;
				}
				return false;
			}
		});

		LookupLogicForDisplay lookupLogicForDisplayObj;
		lookupLogicForDisplayObj = LookupDisplayObjInstance.instance().lookupLogicForDisplayObj;

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

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		final EditText invisibleEditText = findViewById(R.id.invisibleEditText);
		invisibleEditText.setText((char)event.getUnicodeChar()+"");
		invisibleEditText.setSelection(invisibleEditText.getText().length());
		invisibleEditText.requestFocus();
		invisibleEditText.setVisibility(View.VISIBLE);
		return super.onKeyDown(keyCode, event);
	}

}