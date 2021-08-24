package gov.alaska.gmchandheld;


import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.security.ProviderInstaller;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import java.io.File;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

public class Lookup extends BaseActivity {
	private ListView listView;
	private final LinkedList<String> lookupHistory = LookupDisplayObjInstance.getInstance().getLookupHistory();
	private EditText barcodeET;
	private IntentIntegrator qrScan;

	@Override
	public int getLayoutResource() {
		return R.layout.lookup_main;
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		EditText barcodeInput = findViewById(R.id.barcodeET);
		barcodeInput.selectAll();
		this.recreate();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (null == BaseActivity.apiKeyBase){
			Intent intentGetBarcode = new Intent(this.getApplicationContext(), GetToken.class);
			intentGetBarcode.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			this.getApplicationContext().startActivity(intentGetBarcode);
		}
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		barcodeET = findViewById(R.id.barcodeET);
		barcodeET.requestFocus();
		checkUrlUsesHttps();
		deleteApkFile();
		loadLookup();
	}

	public void loadLookup() {
		LookupDisplayObjInstance.getInstance().lookupLogicForDisplayObj = null;
//		sp = getSharedPreferences(Configuration.SHARED_PREFS, MODE_PRIVATE);
		boolean cameraOn = sp.getBoolean("cameraOn", false);

		Button cameraBtn = findViewById(R.id.cameraBtn);
		if (!cameraOn) {
			cameraBtn.setVisibility(View.GONE);
		} else {
			qrScan = new IntentIntegrator(this);
			qrScan.setOrientationLocked(false);
			qrScan.setBeepEnabled(true);
		}

		cameraBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (Build.VERSION.SDK_INT <= 24) {
					qrScan.initiateScan();
				} else {
					Intent intent = new Intent(Lookup.this, CameraToScanner.class);
					startActivityForResult(intent, 0);
				}
			}
		});
		barcodeET = findViewById(R.id.barcodeET);
		final Button submitBtn = findViewById(R.id.submitBtn);
		final RemoteApiUIHandler remoteApiUIHandler = new RemoteApiUIHandler();
		// populates the history list
		listView = findViewById(R.id.listViewBarcodeHistory);
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
		adapter.addAll(lookupHistory);
		adapter.notifyDataSetChanged();
		listView.setAdapter(adapter);
		// Submit barcode query
		if (remoteApiUIHandler.isDownloading()) {
			// onClickListener listens if the submit button is clicked
			submitBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					sp = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
					String url = sp.getString("urlText", "");
					CheckConfiguration checkConfiguration = new CheckConfiguration();
					if (checkConfiguration.checkConfiguration(Lookup.this)) {
						if (!barcodeET.getText().toString().isEmpty()) {
							remoteApiUIHandler.setDownloading(true);
							RemoteApiUIHandler.setUrlFirstParameter(barcodeET.getText().toString());
							new RemoteApiUIHandler.ProcessDataForDisplay(Lookup.this).execute();
						}
					}
				}
			});
			// KeyListener listens if enter is pressed
			barcodeET.setOnKeyListener(new View.OnKeyListener() {
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					// if "enter" is pressed
					if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
						submitBtn.performClick();
						return true;
					}
					return false;
				}
			});
			// Clicking barcode in history list.
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					barcodeET.setText(listView.getItemAtPosition(position).toString());
					submitBtn.performClick();
				}
			});
		}
	}

	//makes the volume keys scroll up/down
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int action, keycode;
		action = event.getAction();
		keycode = event.getKeyCode();

		AudioManager manager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

		switch (keycode) {
			case KeyEvent.KEYCODE_DPAD_UP:
			case KeyEvent.KEYCODE_VOLUME_UP: {
				manager.adjustVolume(AudioManager.ADJUST_RAISE, 0);
				manager.adjustVolume(AudioManager.ADJUST_LOWER, 0);
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
			IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
			barcodeET.setText(result.getContents());
		} else {
			if (resultCode == CommonStatusCodes.SUCCESS) {
				Barcode barcode = data.getParcelableExtra("barcode");
				EditText edit_text = findViewById(R.id.barcodeET);
				if (null != barcode.displayValue) {
					edit_text.setText(barcode.displayValue);
				}
			} else {
				super.onActivityResult(requestCode, resultCode, data);
			}
		}
	}

	public void checkUrlUsesHttps() {
		sp = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
		String url = sp.getString("urlText", "");
		if (!url.startsWith("https")) {
			Intent intent = new Intent(Lookup.this, Configuration.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			Lookup.this.startActivity(intent);
			Toast.makeText(Lookup.this, "The URL must be https.", Toast.LENGTH_LONG).show();
		}
	}


}


