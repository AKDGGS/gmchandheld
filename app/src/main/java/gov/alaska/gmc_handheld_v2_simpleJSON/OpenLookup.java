package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;

public class OpenLookup {

	public OpenLookup() {
	}

	private LinkedList<String> lookupHistory = LookupHistoryHolder.getInstance().getLookupHistory();
	public static final String SHARED_PREFS = "sharedPrefs";
//	public static final String LOOKUPHISTORYSP = "lookupHistorySP";

	@SuppressLint("StaticFieldLeak")
	public void processDataForDisplay(final String barcodeQuery, final Context context) {
		final String websiteURL;
		String websiteURL1;

//		SharedPreferences sp = context.getApplicationContext().getSharedPreferences("", Context.MODE_PRIVATE);
//		String s2 = sp.getString("lookupHistoryString", "");
//		System.out.println(s2);

		SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
		String url = sharedPreferences.getString("urlText", "");

//			websiteURL = "http://maps.dggs.alaska.gov/gmc/inventory.json?barcode=" + barcodeQuery;
//			websiteURL = "http://maps.dggs.alaska.gov/gmcdev/inventory.json?barcode=" + barcode

		int APILevel = android.os.Build.VERSION.SDK_INT;
		if (APILevel < 18) {
			websiteURL1 = "http://" + url;
		} else {
			websiteURL1 = "https://" + url;
		}

		switch (context.getClass().getSimpleName()) {
			case "Lookup": {
				final Button submit_button = ((Activity) context).findViewById(R.id.submit_button);
				final EditText barcodeInput = ((Activity) context).findViewById(R.id.editText1);
				submit_button.setEnabled(false);
				submit_button.setClickable(false);
				submit_button.setFocusableInTouchMode(false);

				barcodeInput.setFocusable(false);
				barcodeInput.setEnabled(false);
				barcodeInput.setFocusableInTouchMode(false);
				websiteURL1 = websiteURL1 + "/inventory.json?barcode=" + barcodeQuery;
				break;
			}
			case "LookupDisplay": {
				final EditText barcodeInput = ((Activity) context).findViewById(R.id.invisibleEditText);
				barcodeInput.setFocusable(false);
				barcodeInput.setEnabled(false);
				barcodeInput.setFocusableInTouchMode(false);
				websiteURL1 = websiteURL1 + "/inventory.json?barcode=" + barcodeQuery;
				break;
			}
			case "Summary":{
				final Button submit_button = ((Activity) context).findViewById(R.id.submit_button);
				final EditText barcodeInput = ((Activity) context).findViewById(R.id.editText1);
				submit_button.setEnabled(false);
				submit_button.setClickable(false);
				submit_button.setFocusableInTouchMode(false);

				barcodeInput.setFocusable(false);
				barcodeInput.setEnabled(false);
				barcodeInput.setFocusableInTouchMode(false);
				websiteURL1 = websiteURL1 + "/summary.json?barcode=" + barcodeQuery;
				break;
			}
			case "SummaryDisplay": {
				final EditText barcodeInput = ((Activity) context).findViewById(R.id.invisibleEditText);
				barcodeInput.setFocusable(false);
				barcodeInput.setEnabled(false);
				barcodeInput.setFocusableInTouchMode(false);
				websiteURL1 = websiteURL1 + "/inventory.json?barcode=" + barcodeQuery;
				break;
			}
		}

		websiteURL = websiteURL1;

		new AsyncTask<String, Integer, DownloadData>() {
			AlertDialog alert;  //onPreExec
			@Override
			protected void onPreExecute() {
				super.onPreExecute();

				AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
				LayoutInflater inflater = ((Activity) context).getLayoutInflater();
				View layout = inflater.inflate(R.layout.downloading_progress_dialog, (ViewGroup) ((Activity) context).findViewById(R.id.downloading_alert_root));
				alertDialog.setView(layout);

				TextView title = new TextView(context);
				title.setText("Downloading");
				title.setGravity(Gravity.CENTER);
				title.setTextSize(16);
				alertDialog.setCustomTitle(title);
				alert = alertDialog.create();
				alert.show();
			}

			@Override
			protected DownloadData doInBackground(String... strings) {
				DownloadData downloadData = new DownloadData(websiteURL, context);
				downloadData.getDataFromURL();
				return downloadData;
			}

			@Override
			protected void onPostExecute(DownloadData obj) {
				//Dismisses the downloading alert.  This is needed if the download fails.
				alert.dismiss();

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

					switch(context.getClass().getSimpleName()){
						case "Lookup":
						case "LookupDisplay": {
							LookupLogicForDisplay lookupLogicForDisplayObj;
							lookupLogicForDisplayObj = new LookupLogicForDisplay();
							LookupDisplayObjInstance.instance().lookupLogicForDisplayObj = lookupLogicForDisplayObj;

							try {
								lookupLogicForDisplayObj.processRawJSON(obj.getRawJson());
							} catch (Exception e) {
								e.printStackTrace();
							}
							Intent intent = new Intent(context, LookupDisplay.class);
							intent.putExtra("barcode", barcodeQuery);  //this barcode refers to the query barcode.
							context.startActivity(intent);
							break;
						}
						case "Summary":
						case "SummaryDisplay":{
							SummaryLogicForDisplay summaryLogicForDisplayObj;
							summaryLogicForDisplayObj = new SummaryLogicForDisplay();
							SummaryDisplayObjInstance.instance().summaryLogicForDisplay = summaryLogicForDisplayObj;

							try {
								summaryLogicForDisplayObj.processRawJSON(obj.getRawJson());
							} catch (Exception e) {
								e.printStackTrace();
							}

							System.out.println(summaryLogicForDisplayObj.getDisplayDict());
							System.out.println(summaryLogicForDisplayObj.getKeyList());

							Intent intent = new Intent(context, SummaryDisplay.class);
							intent.putExtra("barcode", barcodeQuery);  //this barcode refers to the query barcode.
							System.out.println(context.getClass().getSimpleName());
							context.startActivity(intent);
							break;
						}

					}

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

		if(("Lookup".equals(context.getClass().getSimpleName())) || ("LookupDisplay".equals(context.getClass().getSimpleName()))){
			if (!lookupHistory.contains(barcodeQuery) & !barcodeQuery.isEmpty()) {
				lookupHistory.add(0, barcodeQuery);
			}
		}


////				Save LookupHistory list-- Test for audit and move.
//					SharedPreferences prefs = context.getSharedPreferences("LookupHistorySP", Context.MODE_PRIVATE);
//					SharedPreferences.Editor editor = prefs.edit();
//					editor.putString("lookupHistoryString", lookupHistory.toString());
//					editor.commit();


		switch (context.getClass().getSimpleName()) {
			case "Lookup": {
				final Button submit_button = ((Activity) context).findViewById(R.id.submit_button);
				submit_button.setEnabled(true);
				submit_button.setClickable(true);
				submit_button.setFocusableInTouchMode(true);

				final EditText barcodeInput = ((Activity) context).findViewById(R.id.editText1);
				barcodeInput.setFocusable(true);
				barcodeInput.setEnabled(true);
				barcodeInput.setFocusableInTouchMode(true);
				break;
			}
			case "LookupDisplay": {
				final EditText barcodeInput = ((Activity) context).findViewById(R.id.invisibleEditText);
				barcodeInput.setFocusable(true);
				barcodeInput.setEnabled(true);
				barcodeInput.setFocusableInTouchMode(true);
				barcodeInput.requestFocus();
				barcodeInput.getText().clear();
				break;
			}
			case "Summary": {
				final Button submit_button = ((Activity) context).findViewById(R.id.submit_button);
				submit_button.setEnabled(true);
				submit_button.setClickable(true);
				submit_button.setFocusableInTouchMode(true);

				final EditText barcodeInput = ((Activity) context).findViewById(R.id.editText1);
				barcodeInput.setFocusable(true);
				barcodeInput.setEnabled(true);
				barcodeInput.setFocusableInTouchMode(true);
				break;
			}
		}
	}
}
