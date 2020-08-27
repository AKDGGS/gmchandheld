package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.widget.Toolbar;

import java.util.LinkedList;

public class GetBarcode extends BaseActivity {

	EditText barcodeInput;
	Button submit_button;
	ListView listView;
	LinkedList<SpannableString> lookupHistory = LookupHistoryHolder.getInstance().lookupHistory;
	public static ArrayAdapter<SpannableString> adapter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.get_barcode);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		toolbar.setBackgroundColor(Color.parseColor("#ff567b95"));

		submit_button = findViewById(R.id.submit_button);
		listView = findViewById(R.id.listViewGetBarcodeHistory);


		adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lookupHistory);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(GetBarcode.this, LookupDisplay.class);
				intent.putExtra(EXTRA_TEXT, listView.getItemAtPosition(position).toString());
				startActivity(intent);
			}
		});

		onButtonClick();
		addKeyListener();
	}


	public void onButtonClick() {

		submit_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				switch (MainActivity.getButton_pushed()) {
					case "Summary":
					case "Lookup":
						openLookup();
						break;
					default:
						System.out.println("Error");
				}
			}
		});
	}

	private void addKeyListener() {
		barcodeInput = findViewById(R.id.editText1);

		// add a keylistener to keep track user input
		barcodeInput.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				// if "enter" is pressed
				if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {

					switch (MainActivity.getButton_pushed()) {
						case "Summary":
						case "Lookup":
							openLookup();
							break;
						default:
							System.out.println("Error");
					}
					return true;

				}

				return false;
			}
		});

	}


	private void openLookup() {
		EditText editText1 = findViewById(R.id.editText1);
		String BARCODE = editText1.getText().toString();

		String websiteURL;

		int APILevel = android.os.Build.VERSION.SDK_INT;
		if (APILevel < 18) {
			websiteURL = "http://maps.dggs.alaska.gov/gmc/inventory.json?barcode=" + BARCODE;
		}else{
			websiteURL = "https://maps.dggs.alaska.gov/gmc/inventory.json?barcode=" + BARCODE;
		}
			DownloadData downloadClass = new DownloadData(GetBarcode.this, BARCODE);
			downloadClass.execute(websiteURL);

		}

	}