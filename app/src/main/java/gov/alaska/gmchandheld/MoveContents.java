package gov.alaska.gmchandheld;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MoveContents extends BaseActivity {

	private IntentIntegrator fromQrScan;
	private EditText moveContentsFromET, moveContentsToET;

	@Override
	public void onRestart() {
		super.onRestart();
		this.recreate();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.move_contents);

		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setBackgroundColor(Color.parseColor("#ff567b95"));
		setSupportActionBar(toolbar);

		moveContentsFromET = findViewById(R.id.fromET);
		moveContentsToET = findViewById(R.id.toET);
		final Button submitBtn = findViewById(R.id.submitBtn);

		// onClickListener listens if the submit button is clicked
		submitBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CheckConfiguration checkConfiguration = new CheckConfiguration();
				if (checkConfiguration.checkConfiguration(MoveContents.this)) {
					if (!(TextUtils.isEmpty(moveContentsFromET.getText())) & !(TextUtils.isEmpty(moveContentsToET.getText()))) {
						moveContents(moveContentsFromET.getText().toString(), moveContentsToET.getText().toString());
						moveContentsFromET.setText("");
						moveContentsToET.setText("");
					}
				}
			}
		});

		// KeyListener listens if enter is pressed
		moveContentsFromET.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// if "enter" is pressed
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
					moveContentsToET.requestFocus();
					return true;
				}
				return false;
			}
		});
		
		SharedPreferences sp = getSharedPreferences(Configuration.SHARED_PREFS, MODE_PRIVATE);
		boolean cameraOn = (sp.getBoolean("cameraOn", false));

		Button fromCameraBtn = findViewById(R.id.fromCameraBtn);
		Button toCameraBtn = findViewById(R.id.toCameraBtn);
		if(!cameraOn){
			fromCameraBtn.setVisibility(View.GONE);
			toCameraBtn.setVisibility(View.GONE);
		}else{
			fromQrScan = new IntentIntegrator(this);
			fromQrScan.setBeepEnabled(true);
			IntentIntegrator toQrScan = new IntentIntegrator(this);
			toQrScan.setBeepEnabled(true);
		}

		fromCameraBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (Build.VERSION.SDK_INT <= 24) {
					Intent intent = fromQrScan.createScanIntent();
					startActivityForResult(intent, 1);
				} else {
					Intent intent = new Intent(MoveContents.this, CameraToScanner.class);
					startActivityForResult(intent, 1);
				}
			}
		});

		toCameraBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (Build.VERSION.SDK_INT <= 24) {
					Intent intent = fromQrScan.createScanIntent();
					startActivityForResult(intent, 2);
				} else {
					Intent intent = new Intent(MoveContents.this, CameraToScanner.class);
					startActivityForResult(intent, 2);
				}
			}
		});
	}

	public void moveContents(String sourceInput, String destinationInput) {
		RemoteApiUIHandler remoteApiUIHandler = new RemoteApiUIHandler();
		RemoteApiUIHandler.setUrlFirstParameter(sourceInput);
		RemoteApiUIHandler.setDestinationBarcode(destinationInput);
		remoteApiUIHandler.setDownloading(true);
		new RemoteApiUIHandler.ProcessDataForDisplay(MoveContents.this).execute();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		if (Build.VERSION.SDK_INT <= 24) {
			switch (requestCode){
				case 1: {
					moveContentsFromET = findViewById(R.id.fromET);
					IntentResult result = IntentIntegrator.parseActivityResult(IntentIntegrator.REQUEST_CODE, resultCode, data);
					moveContentsFromET.setText(result.getContents());
				}
				break;
				case 2:{
					moveContentsToET = findViewById(R.id.toET);
					IntentResult result = IntentIntegrator.parseActivityResult(IntentIntegrator.REQUEST_CODE, resultCode, data);
					moveContentsToET.setText(result.getContents());
				}
				break;
			}
		}else {
			switch (requestCode){
				case 1: {
					if (resultCode == CommonStatusCodes.SUCCESS) {
						Barcode barcode = data.getParcelableExtra("barcode");
						EditText moveContentsFromET = findViewById(R.id.fromET);
						if (barcode != null) {
							moveContentsFromET.setText(barcode.displayValue);
						}
					}
					break;
				}
				case 2:{
					if (resultCode == CommonStatusCodes.SUCCESS) {
						Barcode barcode = data.getParcelableExtra("barcode");
						EditText moveContentsToET= findViewById(R.id.toET);
						if(barcode != null) {
							moveContentsToET.setText(barcode.displayValue);
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