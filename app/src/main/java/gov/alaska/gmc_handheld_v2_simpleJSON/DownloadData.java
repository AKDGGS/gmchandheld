package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

public class DownloadData extends AsyncTask<String, Void, String> {
// https://www.youtube.com/watch?v=ARnLydTCRrE

	LinkedList<String> lookupHistory = LookupHistoryHolder.getInstance().lookupHistory;
	ArrayAdapter<String> adapter;
	ListView listView;
	Context context;
	String BARCODE;

	public DownloadData(Context context, String BARCODE) {
		this.context = context;
		this.BARCODE = BARCODE;
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

		// a incorrect barcode returns an array with 3 characters.
		if (s.length() > 3) {
			lookupHistory.add(0, BARCODE);
			LookupBuildTree LookupBuildTreeObj = null;
			LookupBuildTreeObj = new LookupBuildTree();
			try {
				LookupBuildTreeObj.processRawJSON(s);
			} catch (Exception e) {
				e.printStackTrace();
			}

			Bridge.instance().lookupBuildTree = LookupBuildTreeObj;

			Intent intent = new Intent(context, LookupDisplay.class);
			context.startActivity(intent);
		}else{
			lookupHistory.add(0, BARCODE + " Error!");
			GetBarcode.adapter.notifyDataSetChanged();
		}
	}
}

