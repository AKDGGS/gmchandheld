package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import java.text.DateFormat;
import java.util.Date;

public class Configuration extends BaseActivity {

	public static final String SHARED_PREFS = "sharedPrefs";
	public static final String URL_TEXT = "urlText";
	public static final String API_TEXT = "apiText";

	private EditText urlInput;
	private EditText apiInput;
	private TextView buildDateTV;
	private String url;
	private String apiKey;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.configuration);

		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setBackgroundColor(Color.parseColor("#ff567b95"));
		setSupportActionBar(toolbar);

		buildDateTV = findViewById(R.id.buildDateTV);
		Date buildDate = new Date(BuildConfig.TIMESTAMP);
		buildDateTV.setText(DateFormat.getDateTimeInstance().format(buildDate));

		urlInput = findViewById(R.id.url_editText);
		apiInput = findViewById(R.id.api_editText);

		final Button save_button = findViewById(R.id.save_button);

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

		url = urlInput.getText().toString();
		if (url.length() > 1 && url.charAt(url.length() - 1) != ('/')) {
			url = url + '/';
		}
		return url;

	}

	public String getApiKey() {
		apiInput = findViewById(R.id.api_editText);
		return apiInput.getText().toString();
	}

	public void saveData() {
		SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();

		if ("".equals(getUrl()) || getUrl().equals(null)) {
			Toast.makeText(this, "You did not enter an URL.", Toast.LENGTH_LONG).show();
		}else if("".equals(getApiKey()) || getApiKey().equals(null)) {
			Toast.makeText(this, "You did not enter an API key.", Toast.LENGTH_LONG).show();
		}else{
			editor.putString(URL_TEXT, getUrl());
			editor.putString(API_TEXT, getApiKey());

			editor.apply();
			Toast.makeText(this, "Changes to configuration saved.", Toast.LENGTH_LONG).show();

			Intent intent = new Intent(this, Lookup.class);
			startActivity(intent);
		}

	}

	public void loadData() {
		SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		url = sharedPreferences.getString(URL_TEXT, "");
		apiKey = sharedPreferences.getString(API_TEXT, "");
//		url = sharedPreferences.getString(URL_TEXT, "http://maps.dggs.alaska.gov/gmc/");
//		apiKey = sharedPreferences.getString(API_TEXT, "thXAgLfS68TRpmixfvr2nksFQYrzZf5F");
	}

	public void updateViews() {
		urlInput.setText(url);
		apiInput.setText(apiKey);
	}

//	@Override
//	public void onBackPressed() {
//		Intent get_barcode = new Intent(this, MainActivity.class);
//		startActivity(get_barcode);
//	}
}