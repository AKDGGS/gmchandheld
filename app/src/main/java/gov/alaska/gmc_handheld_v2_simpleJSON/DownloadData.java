package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.LinkedList;

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

		String websiteURL;

		int APILevel = android.os.Build.VERSION.SDK_INT;
		if (APILevel < 18) {
			websiteURL = "http://maps.dggs.alaska.gov/gmc/inventory.json?barcode=" + strings[0];
			;
		} else {
			websiteURL = "https://maps.dggs.alaska.gov/gmc/inventory.json?barcode=" + strings[0];
			;
		}

		InputStream inputStream;

		// Retry code: https://stackoverflow.com/a/37443321
		HttpURLConnection connection = null;
		int tries = 0;
		int maxRetries = 5;

		do {
			try {
				URL myURL = new URL(websiteURL);
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
		if (s.length() > 3) {  //len of 2
			SpannableString ss = new SpannableString(BARCODE);
			lookupHistory.add(0, ss);

			Intent intent = new Intent(context, LookupDisplay.class);
			intent.putExtra("barcode", BARCODE);
			intent.putExtra("rawJSON", s);
			context.startActivity(intent);
		} else {
			int LONG_DELAY = 3500; //controls for how long the Toast error message is displayed
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			View layout = inflater.inflate(R.layout.lookup_toast_layout, (ViewGroup) ((Activity) context).findViewById(R.id.toast_error_root));
			Toast toast = new Toast(context.getApplicationContext());
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.setDuration(Toast.LENGTH_LONG);
			toast.setView(layout);
			toast.show();


		}
	}
}

