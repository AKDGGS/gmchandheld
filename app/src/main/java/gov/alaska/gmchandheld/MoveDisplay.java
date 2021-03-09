package gov.alaska.gmchandheld;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
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

public class MoveDisplay extends BaseActivity {

	private ArrayList<String> containerList;
	private ArrayAdapter<String> adapter;
	private EditText destinationET;
	private EditText itemET;
	private IntentIntegrator destinationQrScan;

	int clicks = 0;  //used to count double clicks for deletion

	@Override
	protected void onRestart() {
		this.recreate();
		super.onRestart();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.move_display);

		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setBackgroundColor(Color.parseColor("#ff567b95"));
		setSupportActionBar(toolbar);

		itemET = findViewById(R.id.itemET);
		destinationET = findViewById(R.id.toET);
		final TextView moveCountTV = findViewById(R.id.moveCountTV);
		final Button moveBtn = findViewById(R.id.moveBtn);
		final Button addBtn = findViewById(R.id.addContainerBtn);
		final Button clearAllBtn = findViewById(R.id.clearAllBtn);
		ListView containerListLV = findViewById(R.id.listViewContainersToMove);

		adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
		containerListLV.setAdapter(adapter);

		containerList = MoveDisplayObjInstance.getInstance().getMoveList();
		adapter.addAll(containerList);

		moveCountTV.setText(String.valueOf(containerList.size()));

		final SharedPreferences sp = getSharedPreferences(Configuration.SHARED_PREFS, MODE_PRIVATE);

		Boolean cameraOn = (sp.getBoolean("cameraOn", false));

		Button cameraBtn = findViewById(R.id.cameraBtn);
		Button itemCameraBtn = findViewById(R.id.itemCameraBtn);
		if(!cameraOn){
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
			params.weight = 7.0f;

			itemET.setLayoutParams(params);
			cameraBtn.setVisibility(View.GONE);
			itemCameraBtn.setVisibility(View.GONE);
		}else{
			destinationQrScan = new IntentIntegrator(this);
			IntentIntegrator itemQrScan = new IntentIntegrator(this);
			ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
		}


		cameraBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (Build.VERSION.SDK_INT <= 24) {
					Intent intent = destinationQrScan.createScanIntent();
					startActivityForResult(intent, 1);
				} else {
					Intent intent = new Intent(MoveDisplay.this, CameraToScanner.class);
					startActivityForResult(intent, 1);
				}
			}
		});


		itemCameraBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				if (Build.VERSION.SDK_INT <= 24) {
					Intent intent = destinationQrScan.createScanIntent();
					startActivityForResult(intent, 2);
				} else {
					Intent intent = new Intent(MoveDisplay.this, CameraToScanner.class);
					startActivityForResult(intent, 2);
				}

			}
		});


		addBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String container = itemET.getText().toString();
					if (!container.isEmpty()) {
						if (!(container.equals(destinationET.getText().toString()) && (!containerList.contains(container)))) {
							containerList.add(0, container);
							adapter.insert(container, 0);
							adapter.notifyDataSetChanged();
							moveCountTV.setText(String.valueOf(containerList.size()));
						}
						itemET.setText("");
					}
					itemET.requestFocus();
				}

		});

		clearAllBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				itemET.setText("");
				itemET.requestFocus();
				containerList.clear();
				adapter.clear();
				adapter.notifyDataSetChanged();
				moveCountTV.setText(String.valueOf(containerList.size()));
			}
		});

		final RemoteApiUIHandler remoteApiUIHandler = new RemoteApiUIHandler();

		if (remoteApiUIHandler.isDownloading()) {
			//double click to remove elements
			containerListLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
								adapter.notifyDataSetChanged();
								moveCountTV.setText(String.valueOf(containerList.size()));
							}
							clicks = 0;
						}
					}, 500);
				}
			});

			// KeyListener listens if enter is pressed
			itemET.setOnKeyListener(new View.OnKeyListener() {
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					// if "enter" is pressed
					if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
						addBtn.performClick();
						itemET.requestFocus();
						return true;
					}
					return false;
				}
			});

			// KeyListener listens if enter is pressed
			destinationET.setOnKeyListener(new View.OnKeyListener() {
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if (!(TextUtils.isEmpty(destinationET.getText()))) {
						// if "enter" is pressed
						if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
							itemET.requestFocus();
							return true;
						}
					}
					return false;
				}
			});

			// onClickListener listens if the submit button is clicked
			moveBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					CheckConfiguration checkConfiguration = new CheckConfiguration();
					if (checkConfiguration.checkConfiguration(MoveDisplay.this)) {
						if (!(TextUtils.isEmpty(destinationET.getText())) && (containerList.size() > 0)) {
							moveContainer(destinationET.getText().toString());
							itemET.setText("");
							destinationET.setText("");
							moveCountTV.setText("");
						}
					}
				}
			});
		}
	}

	public void moveContainer(String destinationInput) {
		RemoteApiUIHandler remoteApiUIHandler = new RemoteApiUIHandler();
		RemoteApiUIHandler.setUrlFirstParameter(destinationInput);
		RemoteApiUIHandler.setContainerList(containerList);

		remoteApiUIHandler.setDownloading(true);
		remoteApiUIHandler.processDataForDisplay(this);
	}

//	@Override
//	public void onBackPressed() {
//		String[] containerArray = containerList.toArray(new String[0]);
//		Set<String> containerSet = new HashSet<>(Arrays.asList(containerArray));
//		final EditText moveDestinationET = findViewById(R.id.toET);
//
//		super.onBackPressed();
//	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		if (Build.VERSION.SDK_INT <= 24) {

			switch (requestCode){
				case 1: {
					destinationET = findViewById(R.id.toET);
					IntentResult result = IntentIntegrator.parseActivityResult(IntentIntegrator.REQUEST_CODE, resultCode, data);
					destinationET.setText(result.getContents());
				}
				break;
				case 2:{
					itemET = findViewById(R.id.itemET);
					IntentResult result = IntentIntegrator.parseActivityResult(IntentIntegrator.REQUEST_CODE, resultCode, data);
					itemET.setText(result.getContents());
				}
				break;
			}

		}else {
			switch (requestCode){
				case 1: {
					if (resultCode == CommonStatusCodes.SUCCESS) {
						Barcode barcode = data.getParcelableExtra("barcode");
						EditText destinationEt = findViewById(R.id.toET);
						if (barcode != null) {
							destinationEt.setText(barcode.displayValue);
						}
					}
					break;
				}
				case 2:{
					if (resultCode == CommonStatusCodes.SUCCESS) {
						Barcode barcode = data.getParcelableExtra("barcode");
						EditText itemEt = findViewById(R.id.itemET);
						if(barcode != null) {
							itemEt.setText(barcode.displayValue);
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