package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

public class Configuration extends BaseActivity {

	public static final String SHARED_PREFS = "sharedPrefs";
	public static final String URL_TEXT = "urlText";
	public static final String API_TEXT = "apiText";
	public static final String SOFT_KEYBOARD_STR = "softKeyboardStr";


	private SwitchCompat softKeyboardSwitch;
	private boolean switchOnOff;
	private EditText urlInput;
	private EditText apiInput;
	private String url;
	private String apiKey;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.configuration);

		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setBackgroundColor(Color.parseColor("#ff567b95"));
		setSupportActionBar(toolbar);

		urlInput = findViewById(R.id.url_editText);
		apiInput = findViewById(R.id.api_editText);

		SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
		boolean onOff = sharedPreferences.getBoolean("softKeyboardStr", false);

		if (onOff == false) {
			urlInput.setInputType(InputType.TYPE_NULL);
			apiInput.setInputType(InputType.TYPE_NULL);
		}


		final Button save_button = findViewById(R.id.save_button);
		softKeyboardSwitch = findViewById(R.id.softKeyboardSwitch);

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
		urlInput = findViewById(R.id.url_editText);
		return urlInput.getText().toString();
	}

	public String getApiKey() {
		apiInput = findViewById(R.id.api_editText);
		return apiInput.getText().toString();
	}

	public void saveData() {
		SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putString(URL_TEXT, getUrl());
		editor.putString(API_TEXT, getApiKey());
		editor.putBoolean(SOFT_KEYBOARD_STR, softKeyboardSwitch.isChecked());

		editor.apply();
		Toast.makeText(this, "Data saved", Toast.LENGTH_LONG).show();

	}

	public void loadData() {
		SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		url = sharedPreferences.getString(URL_TEXT, "");
		apiKey = sharedPreferences.getString(API_TEXT, "");
		switchOnOff = sharedPreferences.getBoolean(SOFT_KEYBOARD_STR, false);
	}

	public void updateViews() {
		urlInput.setText(url);
		apiInput.setText(apiKey);
		softKeyboardSwitch.setChecked(switchOnOff);
	}
}