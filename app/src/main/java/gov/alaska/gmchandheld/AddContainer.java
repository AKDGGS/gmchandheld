package gov.alaska.gmchandheld;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class AddContainer extends BaseActivity {
	private IntentIntegrator qrScan;
	EditText addContainerBarcodeET;

	@Override
	public int getLayoutResource() {
		return R.layout.add_container;
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		this.recreate();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		checkAPIkeyExists(this);
		addContainerBarcodeET = findViewById(R.id.barcodeET);
		EditText addContainerNameET = findViewById(R.id.nameET);
		EditText addContainerRemarkET = findViewById(R.id.remarkET);
		Button submit_button = findViewById(R.id.submitBtn);
		final RemoteApiUIHandler remoteApiUIHandler = new RemoteApiUIHandler();
		Button cameraBtn = findViewById(R.id.cameraBtn);
		if (!sp.getBoolean("cameraOn", false)){
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			params.weight = 7.75f;
			addContainerBarcodeET.setLayoutParams(params);
			addContainerNameET.setLayoutParams(params);
			addContainerRemarkET.setLayoutParams(params);
			cameraBtn.setVisibility(View.GONE);
		} else {
			qrScan = new IntentIntegrator(this);
			qrScan.setBeepEnabled(true);
		}
		cameraBtn.setOnClickListener(view -> {
			if (Build.VERSION.SDK_INT <= 24) {
				qrScan.initiateScan();
			} else {
				Intent intent = new Intent(AddContainer.this, CameraToScanner.class);
				startActivityForResult(intent, 0);
			}
		});
		// KeyListener listens if enter is pressed
		addContainerBarcodeET.setOnKeyListener((v, keyCode, event) -> {
			if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
				addContainerNameET.requestFocus();
				return true;
			}
			return false;
		});
		// KeyListener listens if enter is pressed
		addContainerNameET.setOnKeyListener((v, keyCode, event) -> {
			if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
					(keyCode == KeyEvent.KEYCODE_ENTER)){
				addContainerRemarkET.requestFocus();
				return true;
			}
			return false;
		});
		if (remoteApiUIHandler.isDownloading()) {
			// onClickListener listens if the submit button is clicked
			submit_button.setOnClickListener(v -> {
					if (!(TextUtils.isEmpty(addContainerBarcodeET.getText()))) {
						String container = addContainerBarcodeET.getText().toString();
						if (!container.isEmpty()) {
							RemoteApiUIHandler remoteApiUIHandler1 = new RemoteApiUIHandler();
							RemoteApiUIHandler.setUrlFirstParameter(
									addContainerBarcodeET.getText().toString());
							RemoteApiUIHandler.setAddContainerName(
									addContainerNameET.getText().toString());
							RemoteApiUIHandler.setAddContainerRemark(
									addContainerRemarkET.getText().toString());
							remoteApiUIHandler1.setDownloading(true);
							new RemoteApiUIHandler.ProcessDataForDisplay(
									AddContainer.this).execute();
						}
						addContainerBarcodeET.setText("");
						addContainerNameET.setText("");
						addContainerRemarkET.setText("");
						addContainerBarcodeET.requestFocus();
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
			if (resultCode == CommonStatusCodes.SUCCESS && null != data) {
				Barcode barcode = data.getParcelableExtra("barcode");
				EditText edit_text = findViewById(R.id.barcodeET);
				assert barcode != null;
				edit_text.setText(barcode.displayValue);
			} else {
				super.onActivityResult(requestCode, resultCode, data);
			}
		}
	}
}
