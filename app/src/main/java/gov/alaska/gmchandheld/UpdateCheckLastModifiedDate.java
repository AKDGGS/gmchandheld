package gov.alaska.gmchandheld;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class UpdateCheckLastModifiedDate extends AsyncTask<Void, Void, Long> {
	public static final String SHARED_PREFS = "sharedPrefs";
	Context mContext;

	public UpdateCheckLastModifiedDate(Context mContext) {
		this.mContext = mContext;
	}

	@Override
	protected Long doInBackground(Void... voids) {
		String urlStr;

		SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		urlStr = sharedPreferences.getString("urlText", "") + "app/current.apk";

		HttpURLConnection httpCon;
		System.setProperty("http.keepAlive", "false");
		long lastModified = 0;

		try {
			URL url = new URL(urlStr);
			System.out.println(url.toString());
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

		//gets the last refused modified date from shared preferences. (The last refused modified date comes from
		//UpdateDownloadAPKHandler
		SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		long lastRefusedUpdate = sharedPreferences.getLong("ignoreUpdateDateSP", 0);


		if (!(updateBuildDate.compareTo(new Date(lastRefusedUpdate)) == 0) & (buildDate.compareTo(updateBuildDate) < 0)) {
			// Update available
			final Intent intent = new Intent(mContext, UpdateDownloadAPKHandler.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			//each update passes the Last modified date to UpdateDownloadAPKHandler where an update can occur or not depending
			// on the user's preference
			intent.putExtra("LAST_MODIFIED_DATE", lastModifiedDate);
			mContext.startActivity(intent);
		} else {
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
