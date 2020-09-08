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
	private String barcodeFromHistory = null;

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
					openLookup();
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
				openLookup();
			}
		});

//		 populate the history list
		listView = findViewById(R.id.listViewGetBarcodeHistory);

		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lookupHistory);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				barcodeFromHistory = listView.getItemAtPosition(position).toString();
				openLookup();
			}
		});
	}

	@SuppressLint("StaticFieldLeak")
	private void openLookup() {
		final String barcode;
		if (barcodeFromHistory == null) {
			barcode = getBarcode();
		} else {
			barcode = barcodeFromHistory;
			barcodeFromHistory = null;
		}

		new AsyncTask<String, Void, String>() {
			@Override
			protected String doInBackground(String... strings) {
				DownloadData downloadData = new DownloadData();
				String s = downloadData.donwloadData(barcode);
				return s;
			}

			@Override
			protected void onPostExecute(String output) {
				super.onPostExecute(output);
				String barcode = getBarcode();

				if (output.contains("Exception")) {
					final String msg = output;
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

				} else if (output.length() > 2) {
					if (!lookupHistory.contains(barcode)) {
						lookupHistory.add(0, barcode);
					}
					Intent intent = new Intent(Lookup.this, LookupDisplay.class);
					intent.putExtra("barcode", barcode);
					intent.putExtra("rawJSON", output);
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
}
