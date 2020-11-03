package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.LinkedList;


public class Move {

	public Move() {
	}

	private LinkedList<String> moveList = LookupHistoryHolder.getInstance().getLookupHistory();

	public static final String SHARED_PREFS = "sharedPrefs";
//	public static final String LOOKUPHISTORYSP = "lookupHistorySP";

	private boolean moving = false;
//
//	public boolean isDownloading() {
//		return downloading;
//	}
//
//	public void setDownloading(boolean downloading) {
//		this.downloading = downloading;
//	}

	AsyncTask asyncTask = null;

	@SuppressLint("StaticFieldLeak")
	public void processDataForDisplay(final String moveDestination, final String containersToMove, final Context context) {
		final String websiteURL;
		String websiteURL1;

//		SharedPreferences sp = context.getApplicationContext().getSharedPreferences("", Context.MODE_PRIVATE);
//		String s2 = sp.getString("lookupHistoryString", "");
//		System.out.println(s2);

		SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
		String url = sharedPreferences.getString("urlText", "");

		if (url.charAt(url.length() - 1) != ('/')) {
			url = url + '/';
		}

		int APILevel = android.os.Build.VERSION.SDK_INT;
		if (APILevel < 18 && url.startsWith("https")) {
			websiteURL1 = "http" + url.substring(5);
		} else {
			websiteURL1 = url;
		}

		switch (context.getClass().getSimpleName()) {
			case "MoveDisplay": {
				websiteURL1 = websiteURL1 + "move.json?d=" + moveDestination + containersToMove;
				break;
			}
		}

		websiteURL = websiteURL1;

		if (!moving) {

			asyncTask = new AsyncTask<String, Integer, MoveData>() {

				AlertDialog alert;  //onPreExec

				@Override
				protected void onPreExecute() {
					super.onPreExecute();

//					AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
//					LayoutInflater inflater = ((Activity) context).getLayoutInflater();
//					View layout = inflater.inflate(R.layout.downloading_progress_dialog, (ViewGroup) ((Activity) context).findViewById(R.id.downloading_alert_root));
//					alertDialog.setView(layout);
//
//
//					TextView title = new TextView(context);
//					title.setText("Downloading: " + barcodeQuery);
//					title.setGravity(Gravity.CENTER);
//					title.setTextSize(16);
//					alertDialog.setCustomTitle(title);
//					alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//
//						@Override
//						public void onClick(DialogInterface dialogInterface, int i) {
//							asyncTask.cancel(true);
//							downloading = false;
//						}
//					});
//
//					alert = alertDialog.create();
//
//					alert.show();
//					alert.setCanceledOnTouchOutside(false);
//
//					if (!barcodeQuery.isEmpty()) {
//						switch (context.getClass().getSimpleName()) {
//							case "MainActivity": {
//
//								lastAddedToHistory(context, barcodeQuery);
//								ListView listView = ((Activity) context).findViewById(R.id.listViewGetBarcodeHistory);
//								ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1);
//								adapter.addAll(lookupHistory);
//								adapter.notifyDataSetChanged();
//								listView.setAdapter(adapter);
//								break;
//							}
//							case "Summary": {
//
//								lastAddedToHistory(context, barcodeQuery);
//								ListView listView = ((Activity) context).findViewById(R.id.listViewGetSummaryHistory);
//								ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1);
//								adapter.addAll(summaryHistory);
//								adapter.notifyDataSetChanged();
//								listView.setAdapter(adapter);
//								break;
//							}
//						}
//					}
//					downloading = false;
				}

				@Override
				protected MoveData doInBackground(String... strings) {
					if (!isCancelled()) {
						MoveData moveData = new MoveData(websiteURL, moveDestination, containersToMove, context);
						moveData.getDataFromURL();
						System.out.println(moveData.getRawJson());
						return moveData;
					}
					return null;
				}

				@Override
				protected void onPostExecute(MoveData obj) {
					//Dismisses the downloading alert.  This is needed if the download fails.
//					alert.dismiss();

					if (obj.isErrored()) {

						LayoutInflater inflater = ((Activity) context).getLayoutInflater();
						View layout = inflater.inflate(R.layout.lookup_error_display, (ViewGroup) ((Activity) context).findViewById(R.id.lookup_error_root));

						AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

						int responseCode = obj.getResponseCode();
						ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

						if (responseCode == 403) {
							alertDialog.setTitle(obj.getException().getMessage());
							alertDialog.setMessage("In the configuration screen, check the API key.");
						} else if (responseCode == 404) {
							alertDialog.setTitle("URL Error");
							alertDialog.setMessage("In the configuration screen, check the URL.");
						} else if (responseCode >= 500) {
							alertDialog.setTitle("Internal Server Error");
							alertDialog.setMessage(obj.getException().getMessage());
						} else if ((Settings.System.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0)) {
							alertDialog.setMessage("Is the device connected to the internet/network?  " +
									"Check if Air Plane mode is on.");
						} else if (!(cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected())) {
							alertDialog.setMessage("Is the device connected to the internet/network?  " +
									"Check if the connection has been lost.");
						} else {
							if (obj.getException().getMessage() == null) {
								alertDialog.setMessage("Unknown Error");
							} else {
								alertDialog.setMessage(obj.getException().getMessage());
							}
						}


						alertDialog.setView(layout);
						alertDialog.setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								moving = true;
//								switch (context.getClass().getSimpleName()) {
//									case "MainActivity":
//									case "Summary":
//										EditText getBarcodeEditText = ((Activity) context).findViewById(R.id.getBarcodeEditText);
//										getBarcodeEditText.setText("");
//										getBarcodeEditText.requestFocus();
//										break;
//									case "SummaryDisplay":
//									case "LookupDisplay":
//										lastAddedToHistory(context, barcodeQuery);
//										EditText invisibleEditText = ((Activity) context).findViewById(R.id.invisibleEditText);
//										invisibleEditText.setText("");
//										invisibleEditText.requestFocus();
//										break;
//								}
							}
						});

						AlertDialog alert = alertDialog.create();
						alert.setCanceledOnTouchOutside(false);
						alert.show();

					}
//					else if (obj.getRawJson().length() > 2) {

//						switch (context.getClass().getSimpleName()) {
//							case "LookupDisplay":
//							case "MainActivity": {
//								LookupLogicForDisplay lookupLogicForDisplayObj;
//								lookupLogicForDisplayObj = new LookupLogicForDisplay();
//								LookupDisplayObjInstance.instance().lookupLogicForDisplayObj = lookupLogicForDisplayObj;
//
//								lookupLogicForDisplayObj.setBarcodeQuery(barcodeQuery);
//
//								try {
//									lookupLogicForDisplayObj.processRawJSON(obj.getRawJson());
//								} catch (Exception e) {
//									e.printStackTrace();
//								}
//								Intent intent = new Intent(context, LookupDisplay.class);
//								intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//								intent.putExtra("barcode", barcodeQuery);  //this barcode refers to the query barcode.
//								context.startActivity(intent);
//
//								lastAddedToHistory(context, barcodeQuery);
//								break;
//							}
//							case "Summary":
//							case "SummaryDisplay": {
//								SummaryLogicForDisplay summaryLogicForDisplayObj;
//								summaryLogicForDisplayObj = new SummaryLogicForDisplay();
//								SummaryDisplayObjInstance.instance().summaryLogicForDisplayObj = summaryLogicForDisplayObj;
//
//								summaryLogicForDisplayObj.setBarcodeQuery(barcodeQuery);
//
//								try {
//									summaryLogicForDisplayObj.processRawJSON(obj.getRawJson());
//								} catch (Exception e) {
//									e.printStackTrace();
//								}
//
//								Intent intent = new Intent(context, SummaryDisplay.class);
//								intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//								intent.putExtra("barcode", barcodeQuery);  //this barcode refers to the query barcode.
//								context.startActivity(intent);
//								lastAddedToHistory(context, barcodeQuery);
//								break;
//							}
//
//						}
//					}
				}
			}.execute();

////				Save LookupHistory list-- Test for audit and move.
//					SharedPreferences prefs = context.getSharedPreferences("LookupHistorySP", Context.MODE_PRIVATE);
//					SharedPreferences.Editor editor = prefs.edit();
//					editor.putString("lookupHistoryString", lookupHistory.toString());
//					editor.commit();

		}
	}

	private void lastAddedToHistory(Context context, String barcodeQuery) {
		String lastAdded = null;
		switch (context.getClass().getSimpleName()) {
			case "Move": {
				if (!moveList.isEmpty()) {
					lastAdded = moveList.get(0);
				}
				if (!barcodeQuery.equals(lastAdded) & !barcodeQuery.isEmpty()) {
					moveList.add(0, barcodeQuery);
				}
				break;
			}
		}
	}
}
