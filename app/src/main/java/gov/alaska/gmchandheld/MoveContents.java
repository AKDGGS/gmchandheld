package gov.alaska.gmchandheld;

import androidx.annotation.Nullable;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MoveContents extends BaseActivity {
	private EditText moveContentsFromET, moveContentsToET;

	@Override
	public int getLayoutResource() {
		return R.layout.move_contents;
	}

	@Override
	public void onRestart() {
		super.onRestart();
		this.recreate();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		checkAPIkeyExists(this);
		moveContentsFromET = findViewById(R.id.fromET);
		moveContentsToET = findViewById(R.id.toET);
		// onClickListener listens if the submit button is clicked
		if (!RemoteApiUIHandler.isDownloading()) {
			findViewById(R.id.submitBtn).setOnClickListener(v -> {
				if (!(TextUtils.isEmpty(moveContentsFromET.getText())) &
						!(TextUtils.isEmpty(moveContentsToET.getText()))) {

					new RemoteApiUIHandler(this, moveContentsFromET.getText().toString(),
							moveContentsToET.getText().toString()).execute();
					moveContentsFromET.setText("");
					moveContentsToET.setText("");
					moveContentsFromET.requestFocus();
				}
			});
		}
		// KeyListener listens if enter is pressed
		moveContentsFromET.setOnKeyListener((v, keyCode, event) -> {
			if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
				moveContentsToET.requestFocus();
				return true;
			}
			return false;
		});
		Button fromCameraBtn = findViewById(R.id.fromCameraBtn);
		Button toCameraBtn = findViewById(R.id.toCameraBtn);
		if (!sp.getBoolean("cameraOn", false)){
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			params.weight = 8.25f;
			moveContentsFromET.setLayoutParams(params);
			moveContentsToET.setLayoutParams(params);
			fromCameraBtn.setVisibility(View.GONE);
			toCameraBtn.setVisibility(View.GONE);
		} else {
			qrScan = new IntentIntegrator(this);
			qrScan.setBeepEnabled(true);
		}
		fromCameraBtn.setOnClickListener(view -> {
			if (Build.VERSION.SDK_INT <= 24) {
				intent = qrScan.createScanIntent();
			} else {
				intent = new Intent(MoveContents.this, CameraToScanner.class);
			}
			startActivityForResult(intent, 1);
		});
		toCameraBtn.setOnClickListener(view -> {
			if (Build.VERSION.SDK_INT <= 24) {
				intent = qrScan.createScanIntent();
			} else {
				intent = new Intent(MoveContents.this, CameraToScanner.class);
			}
			startActivityForResult(intent, 2);
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		if (Build.VERSION.SDK_INT <= 24) {
			IntentResult result = IntentIntegrator.parseActivityResult(
					IntentIntegrator.REQUEST_CODE, resultCode, data);
			switch (requestCode){
				case 1: {
					moveContentsFromET = findViewById(R.id.fromET);
					moveContentsFromET.setText(result.getContents());
				}
				break;
				case 2:{
					moveContentsToET = findViewById(R.id.toET);
					moveContentsToET.setText(result.getContents());
				}
				break;
			}
		} else {
			if (data != null) {
				Barcode barcode = data.getParcelableExtra("barcode");
				if (barcode != null) {
					switch (requestCode) {
						case 1: {
							if (resultCode == CommonStatusCodes.SUCCESS) {
								moveContentsFromET.setText(barcode.displayValue);
							}
							break;
						}
						case 2: {
							if (resultCode == CommonStatusCodes.SUCCESS) {
								moveContentsToET.setText(barcode.displayValue);
							}
							break;
						}
						default:
							super.onActivityResult(requestCode, resultCode, data);
					}
				}
			}
		}
	}
}