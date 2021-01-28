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
		Date currentTime = Calendar.getInstance().getTime();
		System.out.println("Current Time: " + currentTime);

		String urlStr = "http://maps.dggs.alaska.gov/gmcdev/app/version.json";

		HttpURLConnection httpCon = null;
		System.setProperty("http.keepAlive", "false");
		long lastModified = 0;

		try {
			URL url = new URL(urlStr);
			httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setRequestMethod("HEAD");

			lastModified = httpCon.getLastModified();
			System.out.println("Date as long: " + lastModified);
			System.out.println("Last Modified: " + new Date(lastModified));

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

		if(updateBuildDate.compareTo(buildDate) > 0){
			System.out.println("Update Ready.");
		}else{
			System.out.println("No update.");
			Intent i = new Intent(mContext, Lookup.class);
			mContext.startActivity(i);
		}

	}
}
