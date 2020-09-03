package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
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

public class Lookup extends BaseActivity implements AsyncResponse {

	private ListView listView;
	private LinkedList<String> lookupHistory = LookupHistoryHolder.getInstance().getLookupHistory();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lookup_get_barcode);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		toolbar.setBackgroundColor(Color.parseColor("#ff567b95"));

		// KeyListener listens if enter is pressed
		EditText barcodeInput = findViewById(R.id.editText1);

		barcodeInput.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// if "enter" is pressed
				if ((event.getAction() == KeyEvent.ACTION_DOWN)	&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
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

		// populate the history list
		listView = findViewById(R.id.listViewGetBarcodeHistory);

		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lookupHistory);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				DownloadData downloadClass = new DownloadData(Lookup.this, listView.getItemAtPosition(position).toString());
				downloadClass.delegate = Lookup.this;
				downloadClass.execute(listView.getItemAtPosition(position).toString());
			}
		});
	}

	private void openLookup() {
		String barcode = getBarcode();
		DownloadData downloadClass = new DownloadData(Lookup.this, barcode);
		downloadClass.delegate = this;
		downloadClass.execute(barcode);
	}

	@Override
	public void processFinish(String output) {
		String barcode = getBarcode();
		if (output.length() > 2) {
			if(!lookupHistory.contains(barcode)) {
				lookupHistory.add(0, barcode);
			}
			Intent intent = new Intent(this, LookupDisplay.class);
			intent.putExtra("barcode", barcode);
			intent.putExtra("rawJSON", output);
			this.startActivity(intent);
		} else {
			LayoutInflater inflater = this.getLayoutInflater();
			View layout = inflater.inflate(R.layout.lookup_toast_layout, (ViewGroup) ((Activity) this).findViewById(R.id.toast_error_root));
			Toast toast = new Toast(this.getApplicationContext());
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.setDuration(Toast.LENGTH_LONG);
			toast.setView(layout);
			toast.show();
		}
	}

	public String getBarcode(){
		EditText barcodeInput = findViewById(R.id.editText1);
		return barcodeInput.getText().toString();
	}
}
