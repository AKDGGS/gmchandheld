package gov.alaska.gmchandheld;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
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

public class AddContainer extends BaseActivity {

	public static final String SHARED_PREFS = "sharedPrefs";
	private ToneGenerator toneGen1;
	private IntentIntegrator qrScan;
	EditText addContainerBarcodeET;

	@Override
	public void onRestart() {
		super.onRestart();
		this.recreate();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_container);

		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setBackgroundColor(Color.parseColor("#ff567b95"));
		setSupportActionBar(toolbar);

		addContainerBarcodeET = findViewById(R.id.barcodeET);
		final EditText addContainerNameET = findViewById(R.id.nameET);
		final EditText addContainerRemarkET = findViewById(R.id.remarkET);
		final Button submit_button = findViewById(R.id.submitBtn);

		final RemoteApiUIHandler remoteApiUIHandler = new RemoteApiUIHandler();

		SharedPreferences sp = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		Boolean cameraOn = (sp.getBoolean("cameraOn", false));

		Button cameraBtn = findViewById(R.id.cameraBtn);
		if(!cameraOn){
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
			params.weight = 7.75f;
			params.rightMargin = 15;

			addContainerBarcodeET.setLayoutParams(params);
			cameraBtn.setVisibility(View.GONE);
		}else{
			qrScan = new IntentIntegrator(this);
			//toneGen1 doesn't work in the emulator.
			toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
		}

		cameraBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (Build.VERSION.SDK_INT <= 24) {
					qrScan.initiateScan();
				} else {
					Intent intent = new Intent(AddContainer.this, CameraToScanner.class);
					startActivityForResult(intent, 0);
				}

			}
		});

		if (remoteApiUIHandler.isDownloading()) {
			// onClickListener listens if the submit button is clicked
			submit_button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					CheckConfiguration checkConfiguration = new CheckConfiguration();
					if (checkConfiguration.checkConfiguration(AddContainer.this)) {
						if (!(TextUtils.isEmpty(addContainerBarcodeET.getText()))) {

							String container = addContainerBarcodeET.getText().toString();
							if (!container.isEmpty()) {
								RemoteApiUIHandler remoteApiUIHandler = new RemoteApiUIHandler();
								RemoteApiUIHandler.setUrlFirstParameter(addContainerBarcodeET.getText().toString());
								RemoteApiUIHandler.setAddContainerName(addContainerNameET.getText().toString());
								RemoteApiUIHandler.setAddContainerRemark(addContainerRemarkET.getText().toString());

								remoteApiUIHandler.setDownloading(true);
								remoteApiUIHandler.processDataForDisplay(AddContainer.this);
							}
							addContainerBarcodeET.setText("");
							addContainerNameET.setText("");
							addContainerRemarkET.setText("");
							addContainerBarcodeET.requestFocus();
						}
					}
				}
			});
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		if (Build.VERSION.SDK_INT <= 24) {
			addContainerBarcodeET = findViewById(R.id.barcodeET);
			IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
			addContainerBarcodeET.setText(result.getContents());
		}else {
			if (requestCode == 0) {
				if (resultCode == CommonStatusCodes.SUCCESS) {
					Barcode barcode = data.getParcelableExtra("barcode");
					EditText edit_text = findViewById(R.id.barcodeET);
					edit_text.setText(barcode.displayValue);
				}
			} else {
				super.onActivityResult(requestCode, resultCode, data);
			}
		}
	}
}
