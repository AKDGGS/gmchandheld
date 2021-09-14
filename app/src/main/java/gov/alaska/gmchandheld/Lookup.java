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
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class Lookup extends BaseActivity {
	private ListView listView;
	private static LinkedList<String> lookupHistory;
	private EditText barcodeET;
	private static String lastAdded;
	private Exception exception;
	private String data, barcode;
	private volatile boolean isCancel = false;

	public Lookup() {
		lookupHistory = LookupDisplayObjInstance.getInstance().getLookupHistory();
	}

	public static LinkedList<String> getLookupHistory() {
		return lookupHistory;
	}

	public static String getLastAdded() {
		return Lookup.lastAdded;
	}

	public static void setLastAdded(String lastAdded) {
		Lookup.lastAdded = lastAdded;
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

		if (!downloading) {
			downloading = true;
			submitBtn.setOnClickListener(v -> {
				String barcode = barcodeET.getText().toString();
				processingAlert(this, barcode);
				if (!barcode.isEmpty()) {
					try {
						barcode = URLEncoder.encode(barcode, "utf-8");
					} catch (UnsupportedEncodingException e) {
						exception = new Exception(e.getMessage());
					}

					String url = baseURL+ "inventory.json?barcode=" + barcode;
					String finalBarcode = barcode;

					Runnable runnable = new Runnable(){
						@Override
						public void run() {
							if (thread.isInterrupted()){
								return;
							}
								final ExecutorService service = Executors.newFixedThreadPool(1);
								final Future<String> task = service.submit(new RemoteAPIDownload(url));
								try {
									data = task.get();
								} catch (ExecutionException e) {
									e.printStackTrace();
								} catch (InterruptedException e) {
									e.printStackTrace();
									return;
								}
								service.shutdownNow();

							if (data == null || data.length() <= 2) {
								if (alert != null){
									alert.dismiss();
									alert = null;
								}
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										Toast.makeText(Lookup.this,
												"There was an error looking up " + finalBarcode + ".", Toast.LENGTH_LONG).show();
									}
								});
								} else {
									LookupLogicForDisplay lookupLogicForDisplayObj;
									lookupLogicForDisplayObj = new LookupLogicForDisplay();
									LookupDisplayObjInstance.getInstance().lookupLogicForDisplayObj
											= lookupLogicForDisplayObj;
									lookupLogicForDisplayObj.setBarcodeQuery(finalBarcode);
									try {
										lookupLogicForDisplayObj.processRawJSON(data);
									} catch (Exception e) {
										e.printStackTrace();
									}
									intent = new Intent(Lookup.this, LookupDisplay.class);
									intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
											| Intent.FLAG_ACTIVITY_CLEAR_TASK);
									intent.putExtra("barcode", finalBarcode);
									Lookup.this.startActivity(intent);

									if (!lookupHistory.isEmpty()) {
										lastAdded = lookupHistory.get(0);
									}
									if (!finalBarcode.equals(lastAdded)) {
										lookupHistory.add(0, finalBarcode);
									}
								}
							downloading = false;
							}
					};

					thread = new Thread(runnable);
					thread.start();
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
				if (data != null) {
					Barcode barcode = data.getParcelableExtra("barcode");
					EditText edit_text = findViewById(R.id.barcodeET);
					if (barcode != null) {
						edit_text.setText(barcode.displayValue);
					}
				}
			} else {
				super.onActivityResult(requestCode, resultCode, data);
			}
		}
	}


}


