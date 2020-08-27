package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class DownloadDataBackground extends AsyncTask<String, Void, String> {
// https://www.youtube.com/watch?v=ARnLydTCRrE

	Context context;

	public DownloadDataBackground(Context context) {
		this.context = context;
	}

	@Override
	protected String doInBackground(String... strings) {

		String url = strings[0];
		InputStream inputStream;

		try {
			URL myURL = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) myURL.openConnection();
			connection.setReadTimeout(10000);
			connection.setConnectTimeout(200000);
			connection.setRequestMethod("GET");
			connection.connect();

			inputStream = connection.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

			StringBuilder builder = new StringBuilder();
			String line;

			while ((line = reader.readLine()) != null) {
				builder.append(line + "\n");
			}
			inputStream.close();
			reader.close();
			return builder.toString();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(String s) {
		LookupBuildTree LookupBuildTreeObj = null;
		LookupBuildTreeObj = new LookupBuildTree();
		try {
			LookupBuildTreeObj.processRawJSON(s);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Bridge.instance().lookupBuildTree = LookupBuildTreeObj;

		Intent intent = new Intent(context, Lookup.class);
		intent.putStringArrayListExtra("test", (ArrayList<String>) LookupBuildTreeObj.getKeyList());
		context.startActivity(intent);
	}
}

