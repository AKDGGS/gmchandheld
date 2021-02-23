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

import java.sql.SQLOutput;

public class MoveContents extends BaseActivity {

	public static final String SHARED_PREFS = "sharedPrefs";

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

		final EditText moveContentsSourceET = findViewById(R.id.sourceET);
		final EditText moveContentsDestinationET = findViewById(R.id.destinationET);
		final Button submit_button = findViewById(R.id.submit_button);

		// onClickListener listens if the submit button is clicked
		submit_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CheckConfiguration checkConfiguration = new CheckConfiguration();
				if (checkConfiguration.checkConfiguration(MoveContents.this)) {
					if (!(TextUtils.isEmpty(moveContentsSourceET.getText())) & !(TextUtils.isEmpty(moveContentsDestinationET.getText()))) {
						moveContents(moveContentsSourceET.getText().toString(), moveContentsDestinationET.getText().toString());
						moveContentsSourceET.setText("");
						moveContentsDestinationET.setText("");
					}
				}
			}
		});

		// KeyListener listens if enter is pressed
		moveContentsDestinationET.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// if "enter" is pressed
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
					submit_button.performClick();
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
		}

		fromCameraBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(MoveContents.this, CameraToScanner.class);
				startActivityForResult(intent, 1);
			}
		});

		toCameraBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(MoveContents.this, CameraToScanner.class);
				startActivityForResult(intent, 2);
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
		if (Build.VERSION.SDK_INT < 24) {
//			IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
//			editText.setText(result.getContents());
		}else {
			switch (requestCode){
				case 1: {
					System.out.println(requestCode);
					if (resultCode == CommonStatusCodes.SUCCESS) {
						Barcode barcode = data.getParcelableExtra("barcode");
						EditText edit_text = findViewById(R.id.sourceET);
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
						EditText edit_text = findViewById(R.id.destinationET);
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