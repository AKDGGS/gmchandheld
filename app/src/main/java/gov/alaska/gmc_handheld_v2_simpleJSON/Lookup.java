package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import java.util.LinkedList;

public class Lookup extends BaseActivity {

	private ListView listView;
	private LinkedList<String> lookupHistory = LookupHistoryHolder.getInstance().getLookupHistory();
	private boolean asyncCalled;

	private String barcode;

	@Override
	public void onRestart() {
		super.onRestart();
		finish();
		startActivity(getIntent());
	}



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lookup_get_barcode);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		toolbar.setBackgroundColor(Color.parseColor("#ff567b95"));

		final EditText barcodeInput = findViewById(R.id.editText1);
		final Button submit_button = findViewById(R.id.submit_button);

		// KeyListener listens if enter is pressed
		barcodeInput.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// if "enter" is pressed
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
					submit_button.performClick();
					return true;
				}
				return false;
			}
		});

		// onClickListener listens if the submit button is clicked
		submit_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openLookup(getBarcode());
			}
		});

		// populates the history list
		listView = findViewById(R.id.listViewGetBarcodeHistory);

		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lookupHistory);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (asyncCalled == false) {
					barcodeInput.setText(listView.getItemAtPosition(position).toString());
					submit_button.performClick();
				}
			}
		});
	}

	@SuppressLint("StaticFieldLeak")
	public void openLookup(final String barcodeQuery) {
		final String barcode = barcodeQuery;
		final String websiteURL;


		int APILevel = android.os.Build.VERSION.SDK_INT;
		if (APILevel < 18) {
//			websiteURL = "http://maps.dggs.alaska.gov/gmc/inventory.json?barcode=" + barcode;

			//dev address -- used for testing
			websiteURL = "http://maps.dggs.alaska.gov/gmcdev/inventory.json?barcode=" + barcode;

		} else {
			websiteURL = "https://maps.dggs.alaska.gov/gmc/inventory.json?barcode=" + barcode;
		}

		new AsyncTask<String, Void, DownloadData>() {
			Button submit_button = findViewById(R.id.submit_button);
			EditText barcodeInput = findViewById(R.id.editText1);

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
//				barcodeInput.setFocusable(false);
//				barcodeInput.setEnabled(false);

				asyncCalled = true;
				//----disable button---
				submit_button.setEnabled(false);
//				submit_button.setClickable(false);

				//---show message----
				submit_button.setAlpha(.5f);
			}

			@Override
			protected DownloadData doInBackground(String... strings) {
				DownloadData downloadData = new DownloadData(websiteURL, Lookup.this);
				downloadData.getDataFromURL();
				return downloadData;
			}

			@Override
			protected void onPostExecute(DownloadData obj) {

				if (obj.isErrored()) {
					final String msg = obj.getException().toString();
					LayoutInflater inflater = Lookup.this.getLayoutInflater();
					View layout = inflater.inflate(R.layout.lookup_error_display, (ViewGroup) (Lookup.this).findViewById(R.id.lookup_error_root));

					AlertDialog.Builder alertDialog = new AlertDialog.Builder(Lookup.this);
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
					if (!lookupHistory.contains(barcode)) {
						lookupHistory.add(0, barcode);
					}
					LookupLogicForDisplay lookupLogicForDisplayObj;
					lookupLogicForDisplayObj = new LookupLogicForDisplay();
					Bridge.instance().lookupLogicForDisplayObj = lookupLogicForDisplayObj;

					try {
						lookupLogicForDisplayObj.processRawJSON(obj.getRawJson());
					} catch (Exception e) {
						e.printStackTrace();
					}

					Intent intent = new Intent(Lookup.this, LookupDisplay.class);
					intent.putExtra("barcode", barcode);  //this barcode refers to the query barcode.
					Lookup.this.startActivity(intent);
				} else {

					LayoutInflater inflater = Lookup.this.getLayoutInflater();
					View layout = inflater.inflate(R.layout.lookup_toast_layout, (ViewGroup) (Lookup.this).findViewById(R.id.toast_error_root));
					Toast toast = new Toast(Lookup.this);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.setDuration(Toast.LENGTH_LONG);
					toast.setView(layout);
					toast.show();
				}
				barcodeInput.setFocusable(true);
				barcodeInput.setEnabled(true);
				//----disable button---
				submit_button.setEnabled(true);
				submit_button.setClickable(true);
				submit_button.setAlpha(1f);
				submit_button.setText(null);
				asyncCalled = false;
			}
		}.execute();

	}

	public String getBarcode() {
		EditText barcodeInput = findViewById(R.id.editText1);
		return barcodeInput.getText().toString();
	}
}
