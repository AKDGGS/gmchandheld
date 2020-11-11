package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import androidx.core.content.ContextCompat;


public class LookupDisplay extends BaseActivity {
	private ExpandableListView expandableListView;
	private int listAdapterLength;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lookup_display);
		expandableListView = findViewById(R.id.expandableListView);


		getSupportActionBar().setDisplayShowHomeEnabled(true);

		final EditText invisibleEditText = findViewById(R.id.invisibleEditText);

		invisibleEditText.setInputType(InputType.TYPE_NULL);
		final RemoteAPITask remoteAPITaskObj = new RemoteAPITask();

		remoteAPITaskObj.setDownloading(true);
		invisibleEditText.setFocusable(true);

		invisibleEditText.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_DEL) {
					invisibleEditText.setText("");
				}
				if (invisibleEditText.getText().toString().trim().length() != 0) {

					if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
						remoteAPITaskObj.processDataForDisplay(invisibleEditText.getText().toString(), null, LookupDisplay.this);
						return true;
					}
				}else {
					invisibleEditText.setText("");
				}
				return false;
			}

		});

		LookupLogicForDisplay lookupLogicForDisplayObj;
		lookupLogicForDisplayObj = LookupDisplayObjInstance.instance().lookupLogicForDisplayObj;

		if ("GMC Handheld".contentEquals(getSupportActionBar().getTitle())) {
			LookupDisplay.this.getSupportActionBar().setTitle(Html.fromHtml("<strong> <small> <font color='#000000'>" + lookupLogicForDisplayObj.getBarcodeQuery() + "</font> </small> </strong>"));
			if (lookupLogicForDisplayObj.getKeyList().size() > 0) {
				LookupDisplay.this.getSupportActionBar().setSubtitle(Html.fromHtml("<font color='#000000'>" + lookupLogicForDisplayObj.getKeyList().size() + " Result(s) </font>"));
			}

			if (lookupLogicForDisplayObj.getRadiationWarningFlag()) {
				LookupDisplay.this.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.colorRadiation)));
			}
		}

		if (lookupLogicForDisplayObj != null) {
			Intent intent = getIntent();
			String barcode = intent.getStringExtra("barcode");  //this barcode refers to the query barcode.

			if (barcode != null) {
				LookupDisplay.this.getSupportActionBar().setTitle(Html.fromHtml("<strong> <small> <font color='#000000'>" + barcode + "</font> </small> </strong>"));

				if (lookupLogicForDisplayObj.getRadiationWarningFlag()) {
					LookupDisplay.this.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.colorRadiation)));
				}

				if (lookupLogicForDisplayObj.getKeyList().size() > 0) {
					LookupDisplay.this.getSupportActionBar().setSubtitle(Html.fromHtml("<font color='#000000'>" + lookupLogicForDisplayObj.getKeyList().size() + " Result(s) </font>"));
				}
			}


			ExpandableListAdapter listAdapter = new LookupExpListAdapter(LookupDisplay.this, lookupLogicForDisplayObj.getKeyList(), lookupLogicForDisplayObj.getDisplayDict());
			expandableListView.setAdapter(listAdapter);

			listAdapterLength = listAdapter.getGroupCount();
			if (listAdapter.getGroupCount() >= 1) {
//				for (int i = 0; i < listAdapter.getGroupCount(); i++) {
//					expandableListView.expandGroup(i);
//				}
			}

//			expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
//				@Override
//				public boolean onGroupClick(ExpandableListView parent, View v,
//											int groupPosition, long id) {
//					return true; // This way the expander cannot be collapsed
//				}
//			});
		}
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		final EditText invisibleEditText = findViewById(R.id.invisibleEditText);

		invisibleEditText.setText((char) event.getUnicodeChar() + "");
		invisibleEditText.setSelection(invisibleEditText.getText().length());
		invisibleEditText.requestFocus();
		invisibleEditText.setVisibility(View.VISIBLE);
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		LookupLogicForDisplay lookupLogicForDisplayObj;
		lookupLogicForDisplayObj = LookupDisplayObjInstance.instance().lookupLogicForDisplayObj;

		if (lookupLogicForDisplayObj.getDisplayDict().toString().contains("radiation_risk")) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.radiation_menu, menu);

		} else {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.main_menu, menu);
		}
		return true;
	}

//	@Override
//	public void onBackPressed() {
//		Intent get_barcode = new Intent(this, MainActivity.class);
//		startActivity(get_barcode);
//	}


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
					expandableListView.smoothScrollToPosition(0, 0);
				}
				if (KeyEvent.ACTION_UP == action) {
					expandableListView.smoothScrollByOffset(-3);
				}
				return true;
			}
			case KeyEvent.KEYCODE_DPAD_DOWN:
			case KeyEvent.KEYCODE_VOLUME_DOWN: {

				if (action == KeyEvent.ACTION_DOWN && event.isLongPress()) {
					expandableListView.smoothScrollToPosition(expandableListView.getCount());
				}

				if (KeyEvent.ACTION_UP == action) {
					expandableListView.smoothScrollByOffset(3);
				}
				return true;
			}
		}
		return super.dispatchKeyEvent(event);
	}


}