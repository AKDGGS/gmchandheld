package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.widget.Toolbar;

import java.util.LinkedList;

public class Lookup extends BaseActivity {

	private ListView listView;
	private LinkedList<String> lookupHistory = LookupHistoryHolder.getInstance().getLookupHistory();
	private boolean submitted = false;

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

		final OpenLookup openLookupObj = new OpenLookup();

		// onClickListener listens if the submit button is clicked
		submit_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openLookupObj.processDataForDisplay(getBarcode(), Lookup.this);
				submitted = false;
			}
		});

		// populates the history list
		listView = findViewById(R.id.listViewGetBarcodeHistory);

		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
		adapter.addAll(lookupHistory);
		adapter.notifyDataSetChanged();
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (submitted == false) {
					barcodeInput.setText(listView.getItemAtPosition(position).toString());
					submit_button.performClick();
					submitted = true;
				}
			}
		});
	}

	public String getBarcode() {
		EditText barcodeInput = findViewById(R.id.editText1);
		return barcodeInput.getText().toString();
	}
}
