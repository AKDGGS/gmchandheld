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
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class Recode extends BaseActivity {

	private IntentIntegrator oldBarcodeQrScan;
	private IntentIntegrator newBarcodeQrScan;
	private EditText oldBarcodeET, newBarcodeET;

	@Override
	public int getLayoutResource() {
		return R.layout.recode;
	}

	@Override
	public void onRestart() {
		super.onRestart();
		this.recreate();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setBackgroundColor(Color.parseColor("#ff567b95"));
		setSupportActionBar(toolbar);

		oldBarcodeET = findViewById(R.id.oldBarcodeET);
		newBarcodeET = findViewById(R.id.newBarcodeET);
		final Button submit_button = findViewById(R.id.submitBtn);
		final RemoteApiUIHandler remoteApiUIHandler = new RemoteApiUIHandler();

		SharedPreferences sp = getSharedPreferences(Configuration.SHARED_PREFS, MODE_PRIVATE);
		boolean cameraOn = (sp.getBoolean("cameraOn", false));

		Button oldBarcodeCameraBtn = findViewById(R.id.oldBarcodeCameraBtn);
		Button newBarcodeCameraBtn = findViewById(R.id.newBarcodeCameraBtn);

		if(!cameraOn){
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
			params.weight = 8.25f;

			oldBarcodeET.setLayoutParams(params);
			newBarcodeET.setLayoutParams(params);

			newBarcodeCameraBtn.setVisibility(View.GONE);
			oldBarcodeCameraBtn.setVisibility(View.GONE);
		}else{
			oldBarcodeQrScan = new IntentIntegrator(this);
			oldBarcodeQrScan.setBeepEnabled(true);
			newBarcodeQrScan = new IntentIntegrator(this);
			newBarcodeQrScan.setBeepEnabled(true);
		}

		oldBarcodeCameraBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (Build.VERSION.SDK_INT <= 24) {
					Intent intent = oldBarcodeQrScan.createScanIntent();
					startActivityForResult(intent, 1);
				} else {
					Intent intent = new Intent(Recode.this, CameraToScanner.class);
					startActivityForResult(intent, 1);
				}
			}
		});

		newBarcodeCameraBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (Build.VERSION.SDK_INT <= 24) {
					Intent intent = newBarcodeQrScan.createScanIntent();
					startActivityForResult(intent, 2);
				} else {
					Intent intent = new Intent(Recode.this, CameraToScanner.class);
					startActivityForResult(intent, 2);
				}
			}
		});

		if (remoteApiUIHandler.isDownloading()) {
			// onClickListener listens if the submit button is clicked
			submit_button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					CheckConfiguration checkConfiguration = new CheckConfiguration();
					if (checkConfiguration.checkConfiguration(Recode.this)) {
						if ((!oldBarcodeET.getText().toString().isEmpty()) &&(!newBarcodeET.getText().toString().isEmpty()) ) {
							remoteApiUIHandler.setDownloading(true);
							RemoteApiUIHandler.setUrlFirstParameter(oldBarcodeET.getText().toString());
							RemoteApiUIHandler.setGetNewBarcode(newBarcodeET.getText().toString());
							new RemoteApiUIHandler.ProcessDataForDisplay(Recode.this).execute();
							newBarcodeET.setText("");
							oldBarcodeET.setText("");
							oldBarcodeET.requestFocus();
						}
					}
				}
			});

			// KeyListener listens if enter is pressed
			oldBarcodeET.setOnKeyListener(new View.OnKeyListener() {
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					// if "enter" is pressed
					if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
						newBarcodeET.requestFocus();
						return true;
					}
					return false;
				}
			});
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		if (Build.VERSION.SDK_INT <= 24) {
			switch (requestCode){
				case 1: {
					EditText oldBarcodeET = findViewById(R.id.oldBarcodeET);
					IntentResult result = IntentIntegrator.parseActivityResult(IntentIntegrator.REQUEST_CODE, resultCode, data);
					oldBarcodeET.setText(result.getContents());
				}
				break;
				case 2:{
					EditText newBarcodeET = findViewById(R.id.newBarcodeET);
					IntentResult result = IntentIntegrator.parseActivityResult(IntentIntegrator.REQUEST_CODE, resultCode, data);
					newBarcodeET.setText(result.getContents());
				}
				break;
			}
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