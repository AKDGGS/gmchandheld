package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

public class Configuration extends BaseActivity {

	public static final String SHARED_PREFS = "sharedPrefs";
	public static final String URL_TEXT = "urlText";
	public static final String SOFT_KEYBOARD_STR = "softKeyboardStr";

	private SwitchCompat switch1;
	private SwitchCompat softKeyboardSwitch;
	private boolean switchOnOff;
	private EditText urlInput;
	private String url;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.configuration);

		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setBackgroundColor(Color.parseColor("#ff567b95"));
		setSupportActionBar(toolbar);

		urlInput = findViewById(R.id.url_editText);
		final Button save_button = findViewById(R.id.save_button);
		softKeyboardSwitch = findViewById(R.id.softKeyboardSwitch);
		switch1 = findViewById(R.id.softKeyboardSwitch);

		// onClickListener listens if the save button is clicked
		save_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveData();
			}
		});
		loadData();
		updateViews();
	}

	public String getUrl() {
		EditText urlInput = findViewById(R.id.url_editText);
		return urlInput.getText().toString();
	}

	public void saveData() {
		SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(URL_TEXT, getUrl());

		editor.putBoolean(SOFT_KEYBOARD_STR, switch1.isChecked());
		editor.apply();
		Toast.makeText(this, "Data saved", Toast.LENGTH_LONG).show();

	}

	public void loadData() {
		SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		url = sharedPreferences.getString(URL_TEXT, "");
		switchOnOff = sharedPreferences.getBoolean(SOFT_KEYBOARD_STR, false);
	}

	public void updateViews() {
		urlInput.setText(url);
		switch1.setChecked(switchOnOff);
	}
}