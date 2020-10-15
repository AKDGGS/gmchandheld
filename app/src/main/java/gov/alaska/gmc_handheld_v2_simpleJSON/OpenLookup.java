package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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

import java.util.LinkedList;


public class OpenLookup {

	public OpenLookup() {
	}

	private LinkedList<String> lookupHistory = LookupHistoryHolder.getInstance().getLookupHistory();
	private LinkedList<String> summaryHistory = SummaryHistoryHolder.getInstance().getSummaryHistory();

	public static final String SHARED_PREFS = "sharedPrefs";
//	public static final String LOOKUPHISTORYSP = "lookupHistorySP";

	private boolean downloading = false;
	ProgressDialog p;
	AsyncTask asyncTask = null;

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

		if (url.charAt(url.length() - 1) != ('/')) {
			url = url + '/';
		}

		int APILevel = android.os.Build.VERSION.SDK_INT;
		if (APILevel < 18 && url.substring(0, 5).equals("https")) {
			websiteURL1 = "http" + url.substring(5, url.length());
		} else {
			websiteURL1 = url;
		}

		switch (context.getClass().getSimpleName()) {
			case "MainActivity": {
				final Button submit_button = ((Activity) context).findViewById(R.id.submit_button);
				final EditText barcodeInput = ((Activity) context).findViewById(R.id.editText1);
				submit_button.setEnabled(false);
				submit_button.setClickable(false);
				submit_button.setFocusableInTouchMode(false);

				barcodeInput.setFocusable(false);
				barcodeInput.setEnabled(false);
				barcodeInput.setFocusableInTouchMode(false);
				websiteURL1 = websiteURL1 + "inventory.json?barcode=" + barcodeQuery;
				break;
			}
			case "LookupDisplay": {
				final EditText barcodeInput = ((Activity) context).findViewById(R.id.invisibleEditText);
				barcodeInput.setFocusable(false);
				barcodeInput.setEnabled(false);
				barcodeInput.setFocusableInTouchMode(false);
				websiteURL1 = websiteURL1 + "inventory.json?barcode=" + barcodeQuery;
				break;
			}
			case "Summary": {
				final Button submit_button = ((Activity) context).findViewById(R.id.submit_button);
				final EditText barcodeInput = ((Activity) context).findViewById(R.id.editText1);
				submit_button.setEnabled(false);
				submit_button.setClickable(false);
				submit_button.setFocusableInTouchMode(false);

				barcodeInput.setFocusable(false);
				barcodeInput.setEnabled(false);
				barcodeInput.setFocusableInTouchMode(false);
				websiteURL1 = websiteURL1 + "summary.json?barcode=" + barcodeQuery;
				break;
			}
			case "SummaryDisplay": {
				final EditText barcodeInput = ((Activity) context).findViewById(R.id.invisibleEditText);
				barcodeInput.setFocusable(false);
				barcodeInput.setEnabled(false);
				barcodeInput.setFocusableInTouchMode(false);
				websiteURL1 = websiteURL1 + "summary.json?barcode=" + barcodeQuery;
				break;
			}
		}

		websiteURL = websiteURL1;

		if (downloading == false) {
			downloading = true;

			asyncTask = new AsyncTask<String, Integer, DownloadData>() {
				AlertDialog alert;  //onPreExec

				@Override
				protected void onPreExecute() {
					super.onPreExecute();

					AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
					LayoutInflater inflater = ((Activity) context).getLayoutInflater();
					View layout = inflater.inflate(R.layout.downloading_progress_dialog, (ViewGroup) ((Activity) context).findViewById(R.id.downloading_alert_root));
					alertDialog.setView(layout);

					TextView title = new TextView(context);
					title.setText("Downloading: " + barcodeQuery);
					title.setGravity(Gravity.CENTER);
					title.setTextSize(16);
					alertDialog.setCustomTitle(title);
					alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							asyncTask.cancel(true);
						}
					});
					alert = alertDialog.create();
					alert.show();


//					if (!lookupHistory.contains(barcodeQuery) & !barcodeQuery.isEmpty()) {
						lookupHistory.add(0, barcodeQuery);

//					}
				}

				@Override
				protected DownloadData doInBackground(String... strings) {
					DownloadData downloadData = new DownloadData(websiteURL, barcodeQuery, context);
					downloadData.getDataFromURL();
					return downloadData;
				}

				@Override
				protected void onPostExecute(DownloadData obj) {
					//Dismisses the downloading alert.  This is needed if the download fails.
					alert.dismiss();

					if (obj.isErrored()) {
						String msg1 = obj.getException().toString();
						int responseCode = obj.getResponseCode();

						if ("UnknownHostException".equals(obj.getException().getClass().getSimpleName())) {
							msg1 = "Go to configuration and check if the URL is correct.";
						}

						final String msg = msg1;
						LayoutInflater inflater = ((Activity) context).getLayoutInflater();
						View layout = inflater.inflate(R.layout.lookup_error_display, (ViewGroup) ((Activity) context).findViewById(R.id.lookup_error_root));

						AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

						switch (responseCode) {
							case (200):
								alertDialog.setTitle("Connection succesful, but");
								alertDialog.setMessage(obj.getException().getMessage());
								break;
							case (403):
								alertDialog.setTitle("Authentication Error.");
								alertDialog.setMessage("Go to configuration and check the API key.");
								break;
							case (404):
								alertDialog.setTitle("URL Error.");
								alertDialog.setMessage("Go to configuration and check the URL.");
								break;
							default:
								alertDialog.setTitle("Unknown Error:");
								alertDialog.setMessage(responseCode + "\n" + obj.getResponseMsg() + "\n" + obj.getException());
						}

						alertDialog.setView(layout);

						alertDialog.setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						});

						AlertDialog alert = alertDialog.create();
						alert.setCanceledOnTouchOutside(false);
						alert.show();

						resetLookupSummaryButtons(context);


					} else if (obj.getRawJson().length() > 2) {

						switch (context.getClass().getSimpleName()) {
							case "LookupDisplay":
							case "MainActivity": {
								LookupLogicForDisplay lookupLogicForDisplayObj;
								lookupLogicForDisplayObj = new LookupLogicForDisplay();
								LookupDisplayObjInstance.instance().lookupLogicForDisplayObj = lookupLogicForDisplayObj;

								lookupLogicForDisplayObj.setBarcodeQuery(barcodeQuery);

								try {
									lookupLogicForDisplayObj.processRawJSON(obj.getRawJson());
								} catch (Exception e) {
									e.printStackTrace();
								}
								Intent intent = new Intent(context, LookupDisplay.class);
								intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
								intent.putExtra("barcode", barcodeQuery);  //this barcode refers to the query barcode.
								context.startActivity(intent);


								break;
							}
							case "Summary":
							case "SummaryDisplay": {
								SummaryLogicForDisplay summaryLogicForDisplayObj;
								summaryLogicForDisplayObj = new SummaryLogicForDisplay();
								SummaryDisplayObjInstance.instance().summaryLogicForDisplayObj = summaryLogicForDisplayObj;

								try {
									summaryLogicForDisplayObj.processRawJSON(obj.getRawJson());
								} catch (Exception e) {
									e.printStackTrace();
								}

								Intent intent = new Intent(context, SummaryDisplay.class);
								intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
								intent.putExtra("barcode", barcodeQuery);  //this barcode refers to the query barcode.
								context.startActivity(intent);

								if (!summaryHistory.contains(barcodeQuery) & !barcodeQuery.isEmpty()) {
									summaryHistory.add(0, barcodeQuery);
								}
								break;
							}

						}

					}
				}
			}.execute();

////				Save LookupHistory list-- Test for audit and move.
//					SharedPreferences prefs = context.getSharedPreferences("LookupHistorySP", Context.MODE_PRIVATE);
//					SharedPreferences.Editor editor = prefs.edit();
//					editor.putString("lookupHistoryString", lookupHistory.toString());
//					editor.commit();

			resetLookupSummaryButtons(context);
		}
		downloading = false;
	}


	private void resetLookupSummaryButtons(Context context) {
		switch (context.getClass().getSimpleName()) {
			case "Summary":
			case "MainActivity": {
				final Button submit_button = ((Activity) context).findViewById(R.id.submit_button);
				submit_button.setEnabled(true);
				submit_button.setClickable(true);
				submit_button.setFocusableInTouchMode(true);

				final EditText barcodeInput = ((Activity) context).findViewById(R.id.editText1);
				barcodeInput.requestFocus();
				barcodeInput.getText().clear();
				barcodeInput.setFocusable(true);
				barcodeInput.setEnabled(true);
				barcodeInput.setFocusableInTouchMode(true);
				break;
			}
			case "SummaryDisplay":
			case "LookupDisplay": {
				final EditText barcodeInput = ((Activity) context).findViewById(R.id.invisibleEditText);
				barcodeInput.setFocusable(true);
				barcodeInput.setEnabled(true);
				barcodeInput.setFocusableInTouchMode(true);
				barcodeInput.requestFocus();
				barcodeInput.getText().clear();
				break;
			}
		}
	}
}
