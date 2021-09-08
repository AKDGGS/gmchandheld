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
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AddContainer extends BaseActivity {
	private IntentIntegrator qrScan;
	private EditText addContainerBarcodeET;
	private String data;

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
		if (!downloading) {
			downloading = true;
			// onClickListener listens if the submit button is clicked
			submit_button.setOnClickListener(v -> {
				if (!(TextUtils.isEmpty(addContainerBarcodeET.getText()))) {
					if (!addContainerBarcodeET.getText().toString().isEmpty()) {
						String barcode = null;
						String name = null;
						String remark = null;
						try {
							barcode = URLEncoder
									.encode(addContainerBarcodeET.getText().toString(),
											"utf-8");
							name = URLEncoder.encode(addContainerNameET.getText().toString(),
											"utf-8");
							remark = URLEncoder.encode(addContainerRemarkET.getText().toString(),
											"utf-8");
						} catch (UnsupportedEncodingException e) {
//								exception = new Exception(e.getMessage());
						}
						StringBuilder sb = new StringBuilder();
						if (barcode != null) {
							sb.append("barcode=").append(barcode);
						}
						if (name != null) {
							sb.append("&name=").append(name);
						}
						if (remark != null) {
							sb.append("&remark=").append(remark);
						}
						String url = baseURL + "addcontainer.json?" + sb.toString();
						Runnable runnable = new Runnable() {
							@Override
							public void run() {
								if (thread.isInterrupted()) {
									return;
								}
								final ExecutorService service =
										Executors.newFixedThreadPool(1);
								final Future<String> task =
										service.submit(new NewRemoteAPIDownload(url));
								try {
									data = task.get();
								} catch (ExecutionException e) {
									e.printStackTrace();
								} catch (InterruptedException e) {
									e.printStackTrace();
									return;
								}

								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										if (null == data){
											Toast.makeText(AddContainer.this,
													"There was a problem.  " +
															"The container was not added.",
													Toast.LENGTH_SHORT).show();
											addContainerBarcodeET.requestFocus();
										} else if (data.contains("success")){
											Toast.makeText(AddContainer.this,
													"The container was added.",
													Toast.LENGTH_SHORT).show();
											addContainerBarcodeET.requestFocus();
										}
									}
								});
							}
						};
						thread = new Thread(runnable);
						thread.start();
					}
					addContainerBarcodeET.setText("");
					addContainerNameET.setText("");
					addContainerRemarkET.setText("");
					addContainerBarcodeET.requestFocus();
				}
			});
			downloading = false;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		if (Build.VERSION.SDK_INT <= 24) {
			addContainerBarcodeET = findViewById(R.id.barcodeET);
			IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
			addContainerBarcodeET.setText(result.getContents());
		} else {
			if (resultCode == CommonStatusCodes.SUCCESS && data != null) {
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
