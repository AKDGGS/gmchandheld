package gov.alaska.gmchandheld;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

public class UpdateCheckLastModifiedDate extends AsyncTask<Void, Void, Long> {

	Context mContext;
	public UpdateCheckLastModifiedDate(Context mContext) {
		this.mContext = mContext;
	}

	@Override
	protected Long doInBackground(Void... voids) {
		String urlStr = "http://maps.dggs.alaska.gov/gmcdev/app/version.json";

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

		if(updateBuildDate.compareTo(buildDate) < 0){
			// Update available
			final Intent intent = new Intent(mContext, UpdateTranslucentActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			mContext.startActivity(intent);
		}else{
			// No update available
			Intent intent2 = new Intent(mContext, Lookup.class);
			intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			mContext.startActivity(intent2);
		}
	}


}
