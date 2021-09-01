package gov.alaska.gmchandheld;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import androidx.annotation.Nullable;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import java.io.File;
import java.util.LinkedList;

public class Lookup extends BaseActivity {
	private ListView listView;
	private final LinkedList<String> lookupHistory;
	private EditText barcodeET;

	public Lookup() {
		lookupHistory = LookupDisplayObjInstance.getInstance().getLookupHistory();
	}

	@Override
	public int getLayoutResource() {
		return R.layout.lookup_main;
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		checkUrlUsesHttps(this);
		EditText barcodeET = findViewById(R.id.barcodeET);
		barcodeET.selectAll();
		this.recreate();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		enableTSL(this);
		barcodeET = findViewById(R.id.barcodeET);
		barcodeET.requestFocus();
		deleteApkFile();
		loadLookup();
	}

	public void loadLookup() {
		LookupDisplayObjInstance.getInstance().lookupLogicForDisplayObj = null;
		Button cameraBtn = findViewById(R.id.cameraBtn);
		if (!sp.getBoolean("cameraOn", false)) {
			cameraBtn.setVisibility(View.GONE);
		} else {
			qrScan = new IntentIntegrator(this);
			qrScan.setOrientationLocked(false);
			qrScan.setBeepEnabled(true);
		}
		cameraBtn.setOnClickListener(view -> {
			if (Build.VERSION.SDK_INT <= 24) {
				qrScan.initiateScan();
			} else {
				intent = new Intent(Lookup.this, CameraToScanner.class);
				startActivityForResult(intent, 0);
			}
		});
		barcodeET = findViewById(R.id.barcodeET);
		Button submitBtn = findViewById(R.id.submitBtn);

		// populates the history list
		listView = findViewById(R.id.listViewBarcodeHistory);
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
				android.R.layout.simple_list_item_1);
		adapter.addAll(lookupHistory);
		adapter.notifyDataSetChanged();
		listView.setAdapter(adapter);

		// Submit barcode query
		if (!RemoteApiUIHandler.isDownloading()) {
			submitBtn.setOnClickListener(v -> {
				if (!barcodeET.getText().toString().isEmpty()) {
					new RemoteApiUIHandler(this, barcodeET.getText().toString()).execute();
					barcodeET.setText("");
				}
			});
			// KeyListener listens if enter is pressed
			barcodeET.setOnKeyListener((v, keyCode, event) -> {
				if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
						(keyCode == KeyEvent.KEYCODE_ENTER)) {
					submitBtn.performClick();
					return true;
				}
				return false;
			});
			// Clicking barcode in history list.
			listView.setOnItemClickListener((parent, view, position, id) -> {
				barcodeET.setText(listView.getItemAtPosition(position).toString());
				submitBtn.performClick();
			});
		}
	}

	//makes the volume keys scroll up/down
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int action = event.getAction();
		AudioManager manager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		manager.adjustVolume(AudioManager.ADJUST_RAISE, 0);
		manager.adjustVolume(AudioManager.ADJUST_LOWER, 0);
		switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_DPAD_UP:
			case KeyEvent.KEYCODE_VOLUME_UP: {
				if (action == KeyEvent.ACTION_DOWN && event.isLongPress()) {
					listView.smoothScrollToPosition(0, 0);
				}
				if (KeyEvent.ACTION_UP == action) {
					listView.smoothScrollByOffset(-3);
				}
				return true;
			}
			case KeyEvent.KEYCODE_DPAD_DOWN:
			case KeyEvent.KEYCODE_VOLUME_DOWN: {
				if (action == KeyEvent.ACTION_DOWN && event.isLongPress()) {
					listView.smoothScrollToPosition(listView.getCount());
				}
				if (KeyEvent.ACTION_UP == action) {
					listView.smoothScrollByOffset(3);
				}
				return true;
			}
		}
		return super.dispatchKeyEvent(event);
	}

	private void deleteApkFile() {
		File dir = getExternalCacheDir();
		File file = new File(dir, "current.apk");
		file.delete();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		if (Build.VERSION.SDK_INT <= 24) {
			barcodeET = findViewById(R.id.barcodeET);
			IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode,
					data);
			barcodeET.setText(result.getContents());
		} else {
			if (resultCode == CommonStatusCodes.SUCCESS) {
				if (null != data) {
					Barcode barcode = data.getParcelableExtra("barcode");
					EditText edit_text = findViewById(R.id.barcodeET);
					if (null != barcode) {
						edit_text.setText(barcode.displayValue);
					}
				}
			} else {
				super.onActivityResult(requestCode, resultCode, data);
			}
		}
	}
}


