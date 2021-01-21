package gov.alaska.gmchandheld;

import android.Manifest;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;


public class Lookup extends BaseActivity {
	private ListView listView;
	private final LinkedList<String> lookupHistory = LookupHistoryHolder.getInstance().getLookupHistory();

	public static final String SHARED_PREFS = "sharedPrefs";
	public static final String URL_TEXT = "urlText";
	public static final String API_TEXT = "apiText";
	private String url;
	private String apiKey;


	// Storage Permissions
	private static final int REQUEST_EXTERNAL_STORAGE = 1;
	private static String[] PERMISSIONS_STORAGE = {
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE,
			Manifest.permission.REQUEST_INSTALL_PACKAGES
	};

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

		deleteApkFile();

// Used for the auto update feature
		Calendar now = Calendar.getInstance();
		int hour = now.get(Calendar.HOUR_OF_DAY);
		int minute = now.get(Calendar.MINUTE);
		int second = now.get(Calendar.SECOND);
		System.out.println(hour + " " + minute);
		updateAlarm(this);

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
					CheckConfiguration checkConfiguration = new CheckConfiguration();
					if (checkConfiguration.checkConfiguration(Lookup.this)) {
						if (!barcodeInput.getText().toString().isEmpty()) {
							remoteApiUIHandler.setDownloading(true);
							RemoteApiUIHandler.setUrlFirstParameter(barcodeInput.getText().toString());
							remoteApiUIHandler.processDataForDisplay(Lookup.this);
						}
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

	private void deleteApkFile(){
		File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString());

		if(dir.exists()) {
			File[] files = dir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					System.out.println(name + " " + name.matches("(gmc-app-[0-9]+-release\\.apk)"));
					return name.matches("(gmc-app-[0-9]+-release\\.apk)");

				}
			});

			if(files != null && files.length > 0) {
				for (File f : files) {
					f.delete();
				}
			}
		}
	}

	public static void updateAlarm(Context context){
		AlarmHandler alarmHandler = new AlarmHandler(context);
		alarmHandler.cancelAlarmManager();
		alarmHandler.setAlarmManager();
	}
}



