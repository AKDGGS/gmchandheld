package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

public class LookupDisplay extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lookup);

		final EditText invisibleEditText = findViewById(R.id.invisibleEditText);

		invisibleEditText.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// if "enter" is pressed
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
					invisibleEditText.setVisibility(View.VISIBLE);

				}
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
//					Intent intent = new Intent(LookupDisplay.this, LookupDisplay.class);
//					LookupDisplay.this.startActivity(intent);

					openLookup(invisibleEditText.getText().toString());
					return true;
				}
				return false;
			}
		});


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

	@SuppressLint("StaticFieldLeak")
	public void openLookup(final String barcodeQuery) {
		final String barcode = barcodeQuery;
		final String websiteURL;


		int APILevel = android.os.Build.VERSION.SDK_INT;
		if (APILevel < 18) {
			websiteURL = "http://maps.dggs.alaska.gov/gmc/inventory.json?barcode=" + barcode;

			//dev address -- used for testing
//			websiteURL = "http://maps.dggs.alaska.gov/gmcdev/inventory.json?barcode=" + barcode;

		} else {
			websiteURL = "https://maps.dggs.alaska.gov/gmc/inventory.json?barcode=" + barcode;
		}

		new AsyncTask<String, Void, DownloadData>() {
//			Button submit_button = findViewById(R.id.submit_button);
//			EditText barcodeInputBtn = findViewById(R.id.editText1);

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
//				barcodeInputBtn.setFocusable(false);
//				barcodeInputBtn.setEnabled(false);
//
////				asyncCalled = true;
//				//----disable button---
//				submit_button.setEnabled(false);
//				submit_button.setClickable(false);
//
//				//---show message----
//				submit_button.setAlpha(.5f);
			}

			@Override
			protected DownloadData doInBackground(String... strings) {
				DownloadData downloadData = new DownloadData(websiteURL, LookupDisplay.this);
				downloadData.getDataFromURL();
				return downloadData;
			}

			@Override
			protected void onPostExecute(DownloadData obj) {

				if (obj.isErrored()) {
					final String msg = obj.getException().toString();
					LayoutInflater inflater = LookupDisplay.this.getLayoutInflater();
					View layout = inflater.inflate(R.layout.lookup_error_display, (ViewGroup) (LookupDisplay.this).findViewById(R.id.lookup_error_root));

					AlertDialog.Builder alertDialog = new AlertDialog.Builder(LookupDisplay.this);
					alertDialog.setTitle("Exception Thrown");
					alertDialog.setMessage(msg);

					alertDialog.setView(layout);
					alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
					AlertDialog alert = alertDialog.create();
					alert.setCanceledOnTouchOutside(false);
					alert.show();

				} else if (obj.getRawJson().length() > 2) {
//					if (!lookupHistory.contains(barcode)) {
//						lookupHistory.add(0, barcode);
//					}
					LookupLogicForDisplay lookupLogicForDisplayObj;
					lookupLogicForDisplayObj = new LookupLogicForDisplay();
					Bridge.instance().lookupLogicForDisplayObj = lookupLogicForDisplayObj;

					try {
						lookupLogicForDisplayObj.processRawJSON(obj.getRawJson());
					} catch (Exception e) {
						e.printStackTrace();
					}

					Intent intent = new Intent(LookupDisplay.this, LookupDisplay.class);
					intent.putExtra("barcode", barcode);  //this barcode refers to the query barcode.
					LookupDisplay.this.startActivity(intent);
				} else {
					LayoutInflater inflater = LookupDisplay.this.getLayoutInflater();
					View layout = inflater.inflate(R.layout.lookup_toast_layout, (ViewGroup) (LookupDisplay.this).findViewById(R.id.toast_error_root));
					Toast toast = new Toast(LookupDisplay.this);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.setDuration(Toast.LENGTH_LONG);
					toast.setView(layout);
					toast.show();
				}
//				submit_button.setEnabled(true);
//				submit_button.setAlpha(1f);
//				submit_button.setText(null);
//				asyncCalled = false;
			}
		}.execute();

	}
}