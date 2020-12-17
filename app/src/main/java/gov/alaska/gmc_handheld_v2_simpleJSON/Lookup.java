package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.util.LinkedList;


public class Lookup extends BaseActivity {
	private ListView listView;
	private final LinkedList<String> lookupHistory = LookupHistoryHolder.getInstance().getLookupHistory();


	@Override
	public void onRestart() {
		super.onRestart();
		EditText barcodeInput = findViewById(R.id.getBarcodeEditText);
		barcodeInput.selectAll();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lookup_main);
		LookupDisplayObjInstance.instance().lookupLogicForDisplayObj = null;

		File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/app-release-1.apk");
		if(file.exists()) {
			file.delete();
//			Toast.makeText(getBaseContext(), "The file is deleted.", Toast.LENGTH_SHORT).show();
		}else{
//			Toast.makeText(getBaseContext(), "The file doesn't exist.", Toast.LENGTH_SHORT).show();
		}

////         test for accessing lookupHistory from shared preferences.
//        SharedPreferences sp = getApplicationContext().getSharedPreferences("LookupHistorySP", Context.MODE_PRIVATE);
//        String s2 =  sp.getString("lookupHistoryString", "");
//        System.out.println("TEST " + s2);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		toolbar.setBackgroundColor(Color.parseColor("#ff567b95"));

		final EditText barcodeInput = findViewById(R.id.getBarcodeEditText);
		final Button submit_button = findViewById(R.id.submit_button);
		final RemoteApiUIHandler remoteApiUIHandler = new RemoteApiUIHandler();

		// populates the history list
		listView = findViewById(R.id.listViewGetBarcodeHistory);
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
		adapter.addAll(lookupHistory);
		adapter.notifyDataSetChanged();
		listView.setAdapter(adapter);

		// Submit barcode query
		if (remoteApiUIHandler.isDownloading()) {
			// onClickListener listens if the submit button is clicked
			submit_button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (!barcodeInput.getText().toString().isEmpty()) {
						remoteApiUIHandler.setDownloading(true);
						RemoteApiUIHandler.setQueryOrDestination(barcodeInput.getText().toString());
						remoteApiUIHandler.processDataForDisplay(Lookup.this);
					}
				}
			});

			// KeyListener listens if enter is pressed
			barcodeInput.setOnKeyListener(new View.OnKeyListener() {
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					// if "enter" is pressed
					if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
						submit_button.performClick();
						return true;
					}
					return false;
				}
			});

			// Clicking barcode in history list.
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					barcodeInput.setText(listView.getItemAtPosition(position).toString());
					submit_button.performClick();
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
}



