package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.LinkedList;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;

public class DownloadData extends AsyncTask<String, Void, String> {
// https://www.youtube.com/watch?v=ARnLydTCRrE

	LinkedList<SpannableString> lookupHistory = LookupHistoryHolder.getInstance().lookupHistory;
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

		// Retry code: https://stackoverflow.com/a/37443321
		HttpURLConnection connection = null;
		int tries = 0;
		int maxRetries = 5;

		do {
			try {
				URL myURL = new URL(url);
				connection = (HttpURLConnection) myURL.openConnection();
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
			} catch (SocketTimeoutException e) {
				++tries;
				if (maxRetries < tries) {
					return null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} while (connection == null);
		return null;
	}

	@Override
	protected void onPostExecute(String s) {

		// an incorrect barcode returns an array with 3 characters.
		if (s.length() > 3) {
			lookupHistory.add(0, new SpannableString(BARCODE));
			LookupBuildTree LookupBuildTreeObj = null;
			LookupBuildTreeObj = new LookupBuildTree();
			try {
				LookupBuildTreeObj.processRawJSON(s);
			} catch (Exception e) {
				e.printStackTrace();
			}

			Bridge.instance().lookupBuildTree = LookupBuildTreeObj;

			Intent intent = new Intent(context, LookupDisplay.class);
			intent.putExtra("barcode", BARCODE);
			context.startActivity(intent);
		} else {
			SpannableString ss = new SpannableString(BARCODE + " Error!");
			ForegroundColorSpan foregroundSpan = new ForegroundColorSpan(Color.RED);
			ss.setSpan(foregroundSpan, 0,
					ss.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
			lookupHistory.add(0, ss);
			GetBarcode.adapter.notifyDataSetChanged();
		}
	}
}

