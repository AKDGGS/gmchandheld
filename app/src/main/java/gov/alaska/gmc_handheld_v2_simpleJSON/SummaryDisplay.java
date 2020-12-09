package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

public class SummaryDisplay extends BaseActivity {

	private ExpandableListView expandableListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.summary_display);
		expandableListView = findViewById(R.id.expandableListView);

		if(getSupportActionBar() != null) {
			getSupportActionBar().setDisplayShowHomeEnabled(true);
		}
		final EditText invisibleEditText = findViewById(R.id.invisibleEditText);

		invisibleEditText.setInputType(InputType.TYPE_NULL);
		final RemoteApiUIHandler remoteApiUIHandler = new RemoteApiUIHandler();

		invisibleEditText.setFocusable(true);

		invisibleEditText.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_DEL) {
					invisibleEditText.setText("");
				}

				if (invisibleEditText.getText().toString().trim().length() != 0) {
					if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
						remoteApiUIHandler.setDownloading(true);
						RemoteApiUIHandler.setQueryOrDestination(invisibleEditText.getText().toString());
						remoteApiUIHandler.processDataForDisplay(SummaryDisplay.this);
						return true;
					}
				} else {
					invisibleEditText.setText("");
				}
				return false;
			}
		});

		SummaryLogicForDisplay summaryLogicForDisplayObj;
		summaryLogicForDisplayObj = SummaryDisplayObjInstance.instance().summaryLogicForDisplayObj;
		if(getSupportActionBar() != null) {
			if ("GMC Handheld".contentEquals(getSupportActionBar().getTitle())) {
				SummaryDisplay.this.getSupportActionBar().setTitle(Html.fromHtml("<strong> <larger> <font color='#000000'>" + summaryLogicForDisplayObj.getBarcodeQuery() + "</font> </larger> </strong>"));
				if (summaryLogicForDisplayObj.getNumberOfBoxes() > 0) {
					SummaryDisplay.this.getSupportActionBar().setSubtitle(Html.fromHtml("<strong> <larger> <font color='#000000'>" + summaryLogicForDisplayObj.getNumberOfBoxes() + " Result(s) </font> </larger> </strong>"));
					System.out.println(summaryLogicForDisplayObj.getNumberOfBoxes());
				}
			}
		}

		if (summaryLogicForDisplayObj != null) {
			Intent intent = getIntent();
			String barcode = intent.getStringExtra("barcode");  //this barcode refers to the query barcode.

			if (barcode != null) {
				SummaryDisplay.this.getSupportActionBar().setTitle(Html.fromHtml("<strong> <larger> <font color='#000000'>" + summaryLogicForDisplayObj.getBarcodeQuery() + "</font> </larger> </strong>"));

				if (summaryLogicForDisplayObj.getNumberOfBoxes() > 0) {
					SummaryDisplay.this.getSupportActionBar().setSubtitle(Html.fromHtml("<strong> <larger> <font color='#000000'>" + summaryLogicForDisplayObj.getNumberOfBoxes() + " Result(s)" + "</font> </larger> </strong>"));
				}
			}

			ExpandableListAdapter listAdapter = new LookupExpListAdapter(SummaryDisplay.this, summaryLogicForDisplayObj.getKeyList(), summaryLogicForDisplayObj.getDisplayDict());
			expandableListView.setAdapter(listAdapter);

				if (listAdapter.getGroupCount() >= 1) {
					for (int i = 0; i < listAdapter.getGroupCount(); i++) {
						expandableListView.expandGroup(i);
					}
				}

				expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
					@Override
					public boolean onGroupClick(ExpandableListView parent, View v,
												int groupPosition, long id) {
						return true; // This way the expander cannot be collapsed
					}
				});
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		final EditText invisibleEditText = findViewById(R.id.invisibleEditText);
		String characterInput = (char) event.getUnicodeChar() + "";
		invisibleEditText.setText(characterInput);
		invisibleEditText.setSelection(invisibleEditText.getText().length());
		invisibleEditText.requestFocus();
		invisibleEditText.setVisibility(View.VISIBLE);
		return super.onKeyDown(keyCode, event);
	}

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
				System.out.println(keycode);
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