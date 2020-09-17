package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.LinkedList;

public class OpenLookup {

	public OpenLookup() {
	}

	private LinkedList<String> lookupHistory = LookupHistoryHolder.getInstance().getLookupHistory();

	@SuppressLint("StaticFieldLeak")
	public void processDataForDisplay(final String barcodeQuery, final Context context) {
		final String websiteURL;


		int APILevel = android.os.Build.VERSION.SDK_INT;
		if (APILevel < 18) {
			websiteURL = "http://maps.dggs.alaska.gov/gmc/inventory.json?barcode=" + barcodeQuery;

			//dev address -- used for testing
//			websiteURL = "http://maps.dggs.alaska.gov/gmcdev/inventory.json?barcode=" + barcode;

		} else {
			websiteURL = "https://maps.dggs.alaska.gov/gmc/inventory.json?barcode=" + barcodeQuery;
		}

		Lookup lookup = new Lookup();
		if(((Activity) context).getApplication() == lookup.getApplication()){
			System.out.println("true");
		}

		//want to find a better test condition...this works and nothing else I have tried has worked....
		if (((Activity) context).findViewById(R.id.submit_button) != null){
			final Button submit_button = ((Activity) context).findViewById(R.id.submit_button);
			final EditText barcodeInput = ((Activity) context).findViewById(R.id.editText1);
			submit_button.setEnabled(false);
			submit_button.setClickable(false);

			barcodeInput.setFocusable(false);
			barcodeInput.setEnabled(false);
		}

		if (((Activity) context).findViewById(R.id.invisibleEditText) != null) {
			final EditText barcodeInput = ((Activity) context).findViewById(R.id.invisibleEditText);
			barcodeInput.setFocusable(false);
			barcodeInput.setEnabled(false);
		}

		new AsyncTask<String, Void, DownloadData>() {

			@Override
			protected DownloadData doInBackground(String... strings) {
				DownloadData downloadData = new DownloadData(websiteURL, context);
				downloadData.getDataFromURL();
				return downloadData;
			}

			@Override
			protected void onPostExecute(DownloadData obj) {

				if (obj.isErrored()) {
					final String msg = obj.getException().toString();
					LayoutInflater inflater = ((Activity) context).getLayoutInflater();
					View layout = inflater.inflate(R.layout.lookup_error_display, (ViewGroup) ((Activity) context).findViewById(R.id.lookup_error_root));

					AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
					alertDialog.setTitle("Exception Thrown");
					alertDialog.setMessage(msg);

					alertDialog.setView(layout);
					alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
					AlertDialog alert = alertDialog.create();
					alert.setCanceledOnTouchOutside(false);
					alert.show();

				} else if (obj.getRawJson().length() > 2) {
					if (!lookupHistory.contains(barcodeQuery)) {
						lookupHistory.add(0, barcodeQuery);
					}
					LookupLogicForDisplay lookupLogicForDisplayObj;
					lookupLogicForDisplayObj = new LookupLogicForDisplay();
					Bridge.instance().lookupLogicForDisplayObj = lookupLogicForDisplayObj;

					try {
						lookupLogicForDisplayObj.processRawJSON(obj.getRawJson());
					} catch (Exception e) {
						e.printStackTrace();
					}
					Intent intent = new Intent(context, LookupDisplay.class);
					intent.putExtra("barcode", barcodeQuery);  //this barcode refers to the query barcode.
					context.startActivity(intent);
				} else {
					LayoutInflater inflater = ((Activity) context).getLayoutInflater();
					View layout = inflater.inflate(R.layout.lookup_toast_layout, (ViewGroup) ((Activity) context).findViewById(R.id.toast_error_root));
					Toast toast = new Toast(context);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.setDuration(Toast.LENGTH_LONG);
					toast.setView(layout);
					toast.show();
				}
			}
		}.execute();
	}
}
