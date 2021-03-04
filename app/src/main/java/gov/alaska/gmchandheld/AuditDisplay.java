package gov.alaska.gmchandheld;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class AuditDisplay extends BaseActivity {
//	public static final String SHARED_PREFS = "sharedPrefs";
	private SharedPreferences.Editor editor;

	private ArrayList<String> containerList;
	private ArrayAdapter<String> adapter;
	private IntentIntegrator remarkQrScan;
	private IntentIntegrator itemQrScan;
	private EditText auditRemarkET, auditItemET;
	int clicks = 0;  //used to count double clicks for deletion

	@Override
	public void onRestart() {
		super.onRestart();
		this.recreate();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.audit_display);

		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setBackgroundColor(Color.parseColor("#ff567b95"));
		setSupportActionBar(toolbar);

		auditItemET = findViewById(R.id.itemET);
		auditRemarkET = findViewById(R.id.remarkET);
		final TextView auditCountTV = findViewById(R.id.auditCountTV);
		final Button submitBtn = findViewById(R.id.submitBtn);
		final Button addBtn = findViewById(R.id.addContainerBtn);
		final Button clearAllBtn = findViewById(R.id.clearAllBtn);
		ListView auditContainerListLV = findViewById(R.id.listViewGetContainersToAudit);

		adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
		auditContainerListLV.setAdapter(adapter);

		final SharedPreferences sp = getSharedPreferences(Configuration.SHARED_PREFS, MODE_PRIVATE);
		if (sp.getString(Configuration.SHARED_PREFS, "savedAuditRemark") != null) {
			auditRemarkET.setText(sp.getString("savedAuditRemark", ""));
		}
		if (sp.getStringSet("savedAuditContainerList", null) != null) {
			containerList = new ArrayList<>(sp.getStringSet("savedAuditContainerList", null));
			adapter.addAll(containerList);
		} else {
			containerList = new ArrayList<>();
		}

		Boolean cameraOn = (sp.getBoolean("cameraOn", false));

		Button remarkCameraBtn = findViewById(R.id.cameraBtn);
		Button itemCameraBtn = findViewById(R.id.itemCameraBtn);
		if (!cameraOn) {
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
			params.weight = 6.75f;

			auditItemET.setLayoutParams(params);
			remarkCameraBtn.setVisibility(View.GONE);
			itemCameraBtn.setVisibility(View.GONE);
		}else{
			remarkQrScan = new IntentIntegrator(this);
			remarkQrScan.setBeepEnabled(true);
			itemQrScan = new IntentIntegrator(this);
			itemQrScan.setBeepEnabled(true);
		}


		remarkCameraBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (Build.VERSION.SDK_INT <= 24) {
					Intent intent = remarkQrScan.createScanIntent();
					startActivityForResult(intent, 1);
				} else {
					Intent intent = new Intent(AuditDisplay.this, CameraToScanner.class);
					startActivityForResult(intent, 1);
				}
			}
		});


		itemCameraBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (Build.VERSION.SDK_INT <= 24) {
					Intent intent = itemQrScan.createScanIntent();
					startActivityForResult(intent, 2);
				} else {
					Intent intent = new Intent(AuditDisplay.this, CameraToScanner.class);
					startActivityForResult(intent, 2);
				}
			}
		});

		addBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String container = auditItemET.getText().toString();
				if (!container.isEmpty() && !containerList.contains(container)) {
					containerList.add(0, container);
					editor = sp.edit();
					String[] containerArray = containerList.toArray(new String[0]);
					Set<String> containerSet = new HashSet<>(Arrays.asList(containerArray));
					editor.putStringSet("savedAuditContainerList", containerSet).commit();
					adapter.insert(container, 0);
					adapter.notifyDataSetChanged();
					auditCountTV.setText(String.valueOf(containerList.size()));
				}
				auditItemET.setText("");
				auditItemET.requestFocus();
			}

		});


		clearAllBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				auditItemET.setText("");
				auditItemET.requestFocus();
				containerList.clear();
				adapter.clear();
				adapter.notifyDataSetChanged();
				auditCountTV.setText(String.valueOf(containerList.size()));
				sp.edit().remove("savedAuditContainerList").commit();

			}
		});

		final RemoteApiUIHandler remoteApiUIHandler = new RemoteApiUIHandler();

		if (remoteApiUIHandler.isDownloading()) {
			//double click to remove elements
			auditContainerListLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				final long startTime = System.currentTimeMillis();

				@Override
				public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {
					clicks++;

					Handler handler = new Handler();
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							if (clicks == 2) {
								adapter.remove(containerList.get(position));
								containerList.remove(position);
								editor = sp.edit();
								String[] containerArray = containerList.toArray(new String[0]);
								Set<String> containerSet = new HashSet<>(Arrays.asList(containerArray));
								editor.putStringSet("savedAuditContainerList", containerSet).commit();
								adapter.notifyDataSetChanged();
								auditCountTV.setText(String.valueOf(containerList.size()));
							}
							clicks = 0;
						}
					}, 500);
				}
			});

			// KeyListener listens if enter is pressed
			auditItemET.setOnKeyListener(new View.OnKeyListener() {
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					// if "enter" is pressed
					if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
						addBtn.performClick();
						auditItemET.requestFocus();
						return true;
					}
					return false;
				}
			});

			// KeyListener listens if enter is pressed
			auditRemarkET.setOnKeyListener(new View.OnKeyListener() {
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if (!(TextUtils.isEmpty(auditRemarkET.getText()))) {
						// if "enter" is pressed
						if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
							auditItemET.requestFocus();
							return true;
						}
					}
					return false;
				}
			});

			// onClickListener listens if the submit button is clicked
			submitBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					CheckConfiguration checkConfiguration = new CheckConfiguration();
					if (checkConfiguration.checkConfiguration(AuditDisplay.this)) {
						if ((containerList.size() > 0)) {
							auditContainerFn(auditRemarkET.getText().toString());
							auditItemET.setText("");
							auditRemarkET.setText("");
							auditCountTV.setText("");
							sp.edit().remove("savedAuditContainerList").apply();
							sp.edit().remove("savedAuditRemark").apply();
						}
					}
				}
			});
		}
	}

	public void auditContainerFn(String remarkInput) {
		RemoteApiUIHandler remoteApiUIHandler = new RemoteApiUIHandler();
		RemoteApiUIHandler.setUrlFirstParameter(remarkInput);
		RemoteApiUIHandler.setContainerList(containerList);
		remoteApiUIHandler.setDownloading(true);
		remoteApiUIHandler.processDataForDisplay(this);
	}

	@Override
	public void onBackPressed() {
		String[] containerArray = containerList.toArray(new String[0]);
		Set<String> containerSet = new HashSet<>(Arrays.asList(containerArray));
		final EditText remarkET = findViewById(R.id.remarkET);
		final SharedPreferences sp = getSharedPreferences(Configuration.SHARED_PREFS, MODE_PRIVATE);
		editor = sp.edit();
		editor.putStringSet("savedAuditContainerList", containerSet);
		editor.putString("savedAuditRemark", remarkET.getText().toString());
		editor.apply();

		super.onBackPressed();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		if (Build.VERSION.SDK_INT <= 24) {
			switch (requestCode){
				case 1: {
					auditRemarkET = findViewById(R.id.remarkET);
					IntentResult result = IntentIntegrator.parseActivityResult(IntentIntegrator.REQUEST_CODE, resultCode, data);
					auditRemarkET.setText(result.getContents());
				}
				break;
				case 2:{
					auditItemET = findViewById(R.id.itemET);
					IntentResult result = IntentIntegrator.parseActivityResult(IntentIntegrator.REQUEST_CODE, resultCode, data);
					auditItemET.setText(result.getContents());
				}
				break;
			}
		} else {
			switch (requestCode) {
				case 1: {
					if (resultCode == CommonStatusCodes.SUCCESS) {
						Barcode barcode = data.getParcelableExtra("barcode");
						EditText edit_text = findViewById(R.id.remarkET);
						if (barcode != null) {
							edit_text.setText(barcode.displayValue);
						}
					}
					break;
				}
				case 2: {
					if (resultCode == CommonStatusCodes.SUCCESS) {
						Barcode barcode = data.getParcelableExtra("barcode");
						EditText edit_text = findViewById(R.id.itemET);
						if (barcode != null) {
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
