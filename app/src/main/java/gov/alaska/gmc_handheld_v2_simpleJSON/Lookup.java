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

//public class Lookup extends BaseActivity implements AsyncResponse {
public class Lookup extends BaseActivity {

	private ListView listView;
	private LinkedList<String> lookupHistory = LookupHistoryHolder.getInstance().getLookupHistory();



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lookup_get_barcode);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		toolbar.setBackgroundColor(Color.parseColor("#ff567b95"));

		EditText barcodeInput = findViewById(R.id.editText1);
		// KeyListener listens if enter is pressed
		barcodeInput.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// if "enter" is pressed
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
					openLookup(getBarcode());
					return true;
				}
				return false;
			}
		});

		// onClickListener listens if the submit button is clicked
		Button submit_button = findViewById(R.id.submit_button);
		submit_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openLookup(getBarcode());
			}
		});

//		 populate the history list
		listView = findViewById(R.id.listViewGetBarcodeHistory);

		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lookupHistory);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				openLookup(listView.getItemAtPosition(position).toString());
			}
		});
	}

	@SuppressLint("StaticFieldLeak")
	private void openLookup(String barcodeInput) {
		final String barcode = barcodeInput;

		final String websiteURL;

		int APILevel = android.os.Build.VERSION.SDK_INT;
		if (APILevel < 18) {
//			websiteURL = "http://maps.dggs.alaska.gov/gmc/inventory.json?barcode=" + barcode;
			websiteURL = "http://maps.dggs.alaska.gov/gmcdev/inventory.json?barcode=" + barcode;

		} else {
			websiteURL = "https://maps.dggs.alaska.gov/gmc/inventory.json?barcode=" + barcode;
		}

		new AsyncTask<String, Void, DownloadData>() {
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
					Intent intent = new Intent(Lookup.this, LookupDisplay.class);
					intent.putExtra("barcode", barcode);
//					SharedPreferences sp = getApplicationContext().getSharedPreferences("downloadedData", Context.MODE_PRIVATE);
//					String s = sp.getString("downloadDataString", "");
//					intent.putExtra("rawJSON", obj.getRawJson());
//					intent.putExtra("rawJSON", "[{\"ID\":11276,\"barcode\":\"GMC-000096345\",\"boreholes\":[{\"ID\":1459,\"name\":\"UA-1\",\"onshore\":true,\"prospect\":{\"ID\":4,\"name\":\"Amchitka Island\"}}],\"boxNumber\":\"192\",\"collection\":{\"ID\":29,\"name\":\"UAF\"},\"containerPath\":\"ANC/RE/15/07/B\",\"intervalBottom\":3440.00,\"intervalTop\":3020.00,\"intervalUnit\":{\"ID\":2,\"abbr\":\"ft\",\"name\":\"feet\"},\"keywords\":[\"raw\",\"cuttings\",\"washed\"],\"remark\":\"cuttings, interval depths, bad box, orphans replaced in box, samples (3280-3290) \\u0026 (3420-3430) missing\"}]");

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
			}
		}.execute();
	}


	public String getBarcode() {
		EditText barcodeInput = findViewById(R.id.editText1);
		return barcodeInput.getText().toString();
	}
//
//	public void setDefaults(String key, String value, Context context) {
//		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
//		SharedPreferences.Editor editor = preferences.edit();
//		editor.putString(key, value);
//		editor.commit();
//	}
//
//
//	public static String getDefaults(String key, Context context) {
//		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
//		return preferences.getString(key, null);
//	}
}
