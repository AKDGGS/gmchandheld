package gov.alaska.gmchandheld;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLOutput;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

public class UpdateCheckLastModifiedDate extends AsyncTask<Void, Void, Long> {
	public static final String SHARED_PREFS = "sharedPrefs";
	Context mContext;

	public UpdateCheckLastModifiedDate(Context mContext) {
		this.mContext = mContext;
	}

	@Override
	protected Long doInBackground(Void... voids) {
		String urlStr = "http://maps.dggs.alaska.gov/gmcdev/app/current.apk";
		HttpURLConnection httpCon = null;
		System.setProperty("http.keepAlive", "false");
		long lastModified = 0;

		try {
			URL url = new URL(urlStr);
			httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setRequestMethod("HEAD");
			lastModified = httpCon.getLastModified();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lastModified;
	}

	@Override
	protected void onPostExecute(Long lastModifiedDate) {
		super.onPostExecute(lastModifiedDate);

		Date updateBuildDate = new Date(lastModifiedDate);
		Date buildDate = new Date(BuildConfig.TIMESTAMP);

		SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

		long lastRefusedUpdate = sharedPreferences.getLong("LAST_MODIFIED_DATE", 0);

		if (!(updateBuildDate.compareTo(new Date(lastRefusedUpdate)) == 0) & (buildDate.compareTo(updateBuildDate) < 0)) {

			// Update available
			final Intent intent = new Intent(mContext, UpdateTranslucentActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			mContext.startActivity(intent);
		} else {
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putLong("LAST_MODIFIED_DATE", lastModifiedDate);
			editor.apply();

			Toast t = Toast.makeText(mContext.getApplicationContext(),
					"No update available.",
					Toast.LENGTH_SHORT);
			t.show();

			Intent intent = new Intent(mContext, Lookup.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			mContext.startActivity(intent);
		}
	}
}
