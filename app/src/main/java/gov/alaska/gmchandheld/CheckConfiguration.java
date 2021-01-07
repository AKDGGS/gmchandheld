package gov.alaska.gmchandheld;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class CheckConfiguration {
	public static final String SHARED_PREFS = "sharedPrefs";
	public static final String URL_TEXT = "urlText";
	public static final String API_TEXT = "apiText";
	private String url;
	private String apiKey;

	public boolean checkConfiguration(final Context mContext) {
		SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		url = sharedPreferences.getString(URL_TEXT, "");
		apiKey = sharedPreferences.getString(API_TEXT, "");

		if ((url.isEmpty()) || (apiKey.isEmpty())) {
			// setup the alert builder
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setTitle("Problem with the URL or API Key");
			builder.setMessage("Go to configuration page to correct the URL or the API.");        // add a button
			builder.setPositiveButton("Go to Configuration", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intentConfiguration = new Intent(mContext.getApplicationContext(), Configuration.class);
					intentConfiguration.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					mContext.startActivity(intentConfiguration);
				}
			});
			builder.setNegativeButton("Dismiss", null);
			AlertDialog dialog = builder.create();
			dialog.show();
			return false;
		}else{
			return true;
		}
	}
}
