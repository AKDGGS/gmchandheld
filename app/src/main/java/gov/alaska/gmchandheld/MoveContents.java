package gov.alaska.gmchandheld;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
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

	public static final String SHARED_PREFS = "sharedPrefs";
	private ToneGenerator toneGen1;
	private IntentIntegrator fromQrScan;
	private IntentIntegrator toQrScan;
	private EditText fromET, toET;

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

		final EditText moveContentsFromET = findViewById(R.id.fromET);
		final EditText moveContentsToET = findViewById(R.id.toET);
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
		moveContentsToET.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// if "enter" is pressed
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
					submitBtn.performClick();
					return true;
				}
				return false;
			}
		});

		SharedPreferences sp = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		Boolean cameraOn = (sp.getBoolean("cameraOn", false));

		System.out.println(cameraOn);

		Button fromCameraBtn = findViewById(R.id.fromCameraBtn);
		Button toCameraBtn = findViewById(R.id.toCameraBtn);
		if(!cameraOn){
			fromCameraBtn.setVisibility(View.GONE);
			toCameraBtn.setVisibility(View.GONE);
		}else{
			fromQrScan = new IntentIntegrator(this);
			toQrScan = new IntentIntegrator(this);
			toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
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
		remoteApiUIHandler.processDataForDisplay(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		if (Build.VERSION.SDK_INT <= 24) {
			switch (requestCode){
				case 1: {
					fromET = findViewById(R.id.fromET);
					IntentResult result = IntentIntegrator.parseActivityResult(IntentIntegrator.REQUEST_CODE, resultCode, data);
					toET.setText(result.getContents());
				}
				break;
				case 2:{
					toET = findViewById(R.id.toET);
					IntentResult result = IntentIntegrator.parseActivityResult(IntentIntegrator.REQUEST_CODE, resultCode, data);
					toET.setText(result.getContents());
				}
				break;
			}
		}else {
			switch (requestCode){
				case 1: {
					System.out.println(requestCode);
					if (resultCode == CommonStatusCodes.SUCCESS) {
						Barcode barcode = data.getParcelableExtra("barcode");
						EditText edit_text = findViewById(R.id.fromET);
						if (barcode != null) {
							edit_text.setText(barcode.displayValue);
						}
					}
					break;
				}
				case 2:{
					System.out.println(requestCode);
					if (resultCode == CommonStatusCodes.SUCCESS) {
						Barcode barcode = data.getParcelableExtra("barcode");
						EditText edit_text = findViewById(R.id.toET);
						if(barcode != null) {
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