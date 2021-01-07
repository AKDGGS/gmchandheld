package gov.alaska.gmchandheld;

import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.widget.Toolbar;

public class Recode extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recode);

		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setBackgroundColor(Color.parseColor("#ff567b95"));
		setSupportActionBar(toolbar);

		final EditText barcodeInput = findViewById(R.id.getOldBarcodeEditText);
		final EditText newBarcodeInput = findViewById(R.id.getNewBarcodeEditText);
		final Button submit_button = findViewById(R.id.submit_button);
		final RemoteApiUIHandler remoteApiUIHandler = new RemoteApiUIHandler();


		if (remoteApiUIHandler.isDownloading()) {
			// onClickListener listens if the submit button is clicked
			submit_button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					CheckConfiguration checkConfiguration = new CheckConfiguration();
					if (checkConfiguration.checkConfiguration(Recode.this)) {
						if ((!barcodeInput.getText().toString().isEmpty()) &&(!newBarcodeInput.getText().toString().isEmpty()) ) {
							remoteApiUIHandler.setDownloading(true);
							RemoteApiUIHandler.setUrlFirstParameter(barcodeInput.getText().toString());
							RemoteApiUIHandler.setGetNewBarcode(newBarcodeInput.getText().toString());
							remoteApiUIHandler.processDataForDisplay(Recode.this);
							newBarcodeInput.setText("");
							barcodeInput.setText("");
							barcodeInput.requestFocus();
						}
					}
				}
			});

			// KeyListener listens if enter is pressed
			newBarcodeInput.setOnKeyListener(new View.OnKeyListener() {
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					// if "enter" is pressed
					if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
							submit_button.performClick();
						return true;
					}
					return false;
				}
			});
		}
	}
}