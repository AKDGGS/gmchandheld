package gov.alaska.gmchandheld;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

public class Recode extends BaseActivity {

	public static final String SHARED_PREFS = "sharedPrefs";

	@Override
	public void onRestart() {
		super.onRestart();
		this.recreate();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recode);

		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setBackgroundColor(Color.parseColor("#ff567b95"));
		setSupportActionBar(toolbar);

		final EditText barcodeInput = findViewById(R.id.oldBarcodeET);
		final EditText newBarcodeInput = findViewById(R.id.newBarcodeET);
		final Button submit_button = findViewById(R.id.submit_button);
		final RemoteApiUIHandler remoteApiUIHandler = new RemoteApiUIHandler();

		SharedPreferences sp = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		Boolean cameraOn = (sp.getBoolean("cameraOn", false));

		Button oldBarcodeCameraBtn = findViewById(R.id.oldBarcodeCameraBtn);
		Button newBarcodeCameraBtn = findViewById(R.id.newBarcodeCameraBtn);

		if(!cameraOn){
			newBarcodeCameraBtn.setVisibility(View.GONE);
			oldBarcodeCameraBtn.setVisibility(View.GONE);
		}



		oldBarcodeCameraBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(Recode.this, CameraToScanner.class);
				startActivityForResult(intent, 1);
			}
		});

		newBarcodeCameraBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(Recode.this, CameraToScanner.class);
				startActivityForResult(intent, 2);
			}
		});


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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		if (Build.VERSION.SDK_INT < 24) {
//			IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
//			editText.setText(result.getContents());
		} else {
			switch (requestCode) {
				case 1: {
					if (resultCode == CommonStatusCodes.SUCCESS) {
						Barcode barcode = data.getParcelableExtra("barcode");
						EditText edit_text = findViewById(R.id.oldBarcodeET);
						if (barcode != null) {
							edit_text.setText(barcode.displayValue);
						}
					}
					break;
				}
				case 2: {
					if (resultCode == CommonStatusCodes.SUCCESS) {
						Barcode barcode = data.getParcelableExtra("barcode");
						EditText edit_text = findViewById(R.id.newBarcodeET);
						if (barcode != null) {
							edit_text.setText(barcode.displayValue);
						}
					}
					break;
				}
				default:
					super.onActivityResult(requestCode, resultCode, data);
			}
		}
	}
}