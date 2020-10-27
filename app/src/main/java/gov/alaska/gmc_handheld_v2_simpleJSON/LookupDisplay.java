package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import androidx.core.content.ContextCompat;


public class LookupDisplay extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lookup_display);

		getSupportActionBar().setDisplayShowHomeEnabled(true);

		final EditText invisibleEditText = findViewById(R.id.invisibleEditText);

		invisibleEditText.setInputType(InputType.TYPE_NULL);
		final OpenLookup openLookupObj = new OpenLookup();

		openLookupObj.setDownloading(true);
		invisibleEditText.setFocusable(true);

		invisibleEditText.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_DEL) {
					invisibleEditText.setText("");
				}

				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
					openLookupObj.processDataForDisplay(invisibleEditText.getText().toString(), LookupDisplay.this);
					return true;
				}
				return false;
			}
		});

		LookupLogicForDisplay lookupLogicForDisplayObj;
		lookupLogicForDisplayObj = LookupDisplayObjInstance.instance().lookupLogicForDisplayObj;

		if ("GMC_handheld".contentEquals(getSupportActionBar().getTitle())) {
			LookupDisplay.this.getSupportActionBar().setTitle(Html.fromHtml("<strong> <small> <font color='#000000'>" + lookupLogicForDisplayObj.getBarcodeQuery() + "</font> </small> </strong>"));
			if (lookupLogicForDisplayObj.getKeyList().size() > 0) {
				LookupDisplay.this.getSupportActionBar().setSubtitle(Html.fromHtml("<font color='#000000'>" + lookupLogicForDisplayObj.getKeyList().size() + " Result(s) </font>"));
			}

			if (lookupLogicForDisplayObj.getRadiationWarningFlag()) {
				LookupDisplay.this.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.colorRadiation)));
			}
		}

		if (lookupLogicForDisplayObj != null) {
			Intent intent = getIntent();
			String barcode = intent.getStringExtra("barcode");  //this barcode refers to the query barcode.

			if (barcode != null) {
				LookupDisplay.this.getSupportActionBar().setTitle(Html.fromHtml("<strong> <small> <font color='#000000'>" + barcode + "</font> </small> </strong>"));

				if (lookupLogicForDisplayObj.getRadiationWarningFlag()) {
					LookupDisplay.this.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.colorRadiation)));
				}

				if (lookupLogicForDisplayObj.getKeyList().size() > 0) {
					LookupDisplay.this.getSupportActionBar().setSubtitle(Html.fromHtml("<font color='#000000'>" + lookupLogicForDisplayObj.getKeyList().size() + " Result(s) </font>"));
				}
			}

			ExpandableListView expandableListView = findViewById(R.id.expandableListView);
			ExpandableListAdapter listAdapter = new LookupExpListAdapter(LookupDisplay.this, lookupLogicForDisplayObj.getKeyList(), lookupLogicForDisplayObj.getDisplayDict());
			expandableListView.setAdapter(listAdapter);

			System.out.println(lookupLogicForDisplayObj.getDisplayDict().toString());
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		LookupLogicForDisplay lookupLogicForDisplayObj;
		lookupLogicForDisplayObj = LookupDisplayObjInstance.instance().lookupLogicForDisplayObj;

		if (lookupLogicForDisplayObj.getDisplayDict().toString().contains("radiation_risk")) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.radiation_menu, menu);

		} else {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.main_menu, menu);
		}
		return true;
	}

}