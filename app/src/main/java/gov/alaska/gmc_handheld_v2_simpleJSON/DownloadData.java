package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.LinkedList;

public class DownloadData extends AsyncTask<String, Void, String> {
	// https://www.youtube.com/watch?v=ARnLydTCRrE

	private Exception exceptionToBeThrown;
	final LinkedList<SpannableString> lookupHistory = LookupHistoryHolder.getInstance().lookupHistory;
	final Context context;
	final String BARCODE;

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
		HttpURLConnection connection;

		try {
			URL myURL = new URL(websiteURL);
			connection = (HttpURLConnection) myURL.openConnection();
			connection.setReadTimeout(10000);
			connection.setConnectTimeout(200000);
			connection.setRequestMethod("GET");
			connection.connect();

			inputStream = connection.getInputStream();
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

			int i;
			try {
				i = inputStream.read();
				while (i != -1) {
					byteArrayOutputStream.write(i);
					i = inputStream.read();
				}
				inputStream.close();
				inputStream.close();

				return byteArrayOutputStream.toString();
			} catch (IOException e) {
				exceptionToBeThrown = e;
				e.printStackTrace();
			}
		} catch (ProtocolException e) {
			exceptionToBeThrown = e;
			e.printStackTrace();
		} catch (MalformedURLException e) {
			exceptionToBeThrown = e;
			e.printStackTrace();
		} catch (IOException e) {
			exceptionToBeThrown = e;
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(String s) {

		if (exceptionToBeThrown != null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			View layout = inflater.inflate(R.layout.lookup_error_display, (ViewGroup) ((Activity) context).findViewById(R.id.lookup_error_root));

			AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
			alertDialog.setTitle("Exception Thrown");
			alertDialog.setMessage(exceptionToBeThrown.toString());

			alertDialog.setView(layout);
			alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});
			AlertDialog alert = alertDialog.create();
			alert.setCanceledOnTouchOutside(false);
			alert.show();
		} else if (s.length() > 2) {
			// an incorrect barcode returns an array with 2 characters.
			SpannableString ss = new SpannableString(BARCODE);
			lookupHistory.add(0, ss);

			Intent intent = new Intent(context, LookupDisplay.class);
			intent.putExtra("barcode", BARCODE);
			intent.putExtra("rawJSON", s);
			context.startActivity(intent);
		} else {
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

