package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;


public class RemoteApiTask {

	public RemoteApiTask() {
	}

	private LinkedList<String> lookupHistory = LookupHistoryHolder.getInstance().getLookupHistory();
	private LinkedList<String> summaryHistory = SummaryHistoryHolder.getInstance().getSummaryHistory();
	private String containerListStr;

	public static final String SHARED_PREFS = "sharedPrefs";
//	public static final String LOOKUPHISTORYSP = "lookupHistorySP";

	private boolean downloading = false;

	public boolean isDownloading() {
		return downloading;
	}

	public void setDownloading(boolean downloading) {
		this.downloading = downloading;
	}

	AsyncTask asyncTask = null;

	@SuppressLint("StaticFieldLeak")
	public void processDataForDisplay(final String url, final String query, final String containerListStr, final Context context) {

//		SharedPreferences sp = context.getApplicationContext().getSharedPreferences("", Context.MODE_PRIVATE);
//		String s2 = sp.getString("lookupHistoryString", "");
//		System.out.println(s2);

//		SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
//		String url = sharedPreferences.getString("urlText", "");




		if (!downloading) {
			asyncTask = new AsyncTask<String, Integer, RemoteAPIDownload>() {

				AlertDialog alert;  //onPreExec

				@Override
				protected void onPreExecute() {
					super.onPreExecute();

					AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
					LayoutInflater inflater = ((Activity) context).getLayoutInflater();
					View layout = inflater.inflate(R.layout.downloading_progress_dialog, (ViewGroup) ((Activity) context).findViewById(R.id.downloading_alert_root));
					alertDialog.setView(layout);


					TextView title = new TextView(context);
					title.setText("Processing " + query);
					title.setGravity(Gravity.CENTER);
					title.setTextSize(16);
					alertDialog.setCustomTitle(title);
					alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							asyncTask.cancel(true);
							downloading = false;
						}
					});

					alert = alertDialog.create();

					alert.show();
					alert.setCanceledOnTouchOutside(false);

					if (!query.isEmpty()) {
						switch (context.getClass().getSimpleName()) {
							case "Lookup": {

								lastAddedToHistory(context, query);
								ListView listView = ((Activity) context).findViewById(R.id.listViewGetBarcodeHistory);
								ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1);
								adapter.addAll(lookupHistory);
								adapter.notifyDataSetChanged();
								listView.setAdapter(adapter);
								break;
							}
							case "Summary": {

								lastAddedToHistory(context, query);
								ListView listView = ((Activity) context).findViewById(R.id.listViewGetSummaryHistory);
								ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1);
								adapter.addAll(summaryHistory);
								adapter.notifyDataSetChanged();
								listView.setAdapter(adapter);
								break;
							}
						}
					}
					downloading = false;
				}

				@Override
				protected RemoteAPIDownload doInBackground(String... strings) {

					if (!isCancelled()) {
						RemoteAPIDownload remoteAPIDownload = null;
						switch (context.getClass().getSimpleName()) {
							case "Lookup":
							case "LookupDisplay":
							case "Summary":
							case "SummaryDisplay": {
								remoteAPIDownload = new RemoteAPIDownload(url, context);
								remoteAPIDownload.setQueryOrDestination(query);
								break;
							}
							case "MoveDisplay": {
								remoteAPIDownload = new RemoteAPIDownload(url, context);
								remoteAPIDownload.setQueryOrDestination(query);
								remoteAPIDownload.setContainerListStr(containerListStr);

								break;
							}
						}

						remoteAPIDownload.getDataFromURL();
						return remoteAPIDownload;
					}
					return null;
				}

				@Override
				protected void onPostExecute(RemoteAPIDownload obj) {
					//Dismisses the downloading alert.  This is needed if the download fails.
					alert.dismiss();

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
								downloading = true;
								switch (context.getClass().getSimpleName()) {
									case "Lookup":
									case "Summary":
										EditText getBarcodeEditText = ((Activity) context).findViewById(R.id.getBarcodeEditText);
										getBarcodeEditText.setText("");
										getBarcodeEditText.requestFocus();
										break;
									case "SummaryDisplay":
									case "LookupDisplay":
//										lastAddedToHistory(context, queryOrDestination);
										EditText invisibleEditText = ((Activity) context).findViewById(R.id.invisibleEditText);
										invisibleEditText.setText("");
										invisibleEditText.requestFocus();
										break;
									case "MoveDisplay":
										EditText destinationET = ((Activity) context).findViewById(R.id.destinationET);
//										destinationET.setText(queryOrDestination);
										break;
								}
							}

						});

						AlertDialog alert = alertDialog.create();
						alert.setCanceledOnTouchOutside(false);
						alert.show();
					} else if (obj.getRawJson().length() > 2) {
						switch (context.getClass().getSimpleName()) {
							case "LookupDisplay":
							case "Lookup": {
								LookupLogicForDisplay lookupLogicForDisplayObj;
								lookupLogicForDisplayObj = new LookupLogicForDisplay();
								LookupDisplayObjInstance.instance().lookupLogicForDisplayObj = lookupLogicForDisplayObj;

								lookupLogicForDisplayObj.setBarcodeQuery(query);

								try {
									lookupLogicForDisplayObj.processRawJSON(obj.getRawJson());
								} catch (Exception e) {
									e.printStackTrace();
								}
								Intent intent = new Intent(context, LookupDisplay.class);
								intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
								intent.putExtra("barcode", query);  //this barcode refers to the query barcode.
								context.startActivity(intent);

								lastAddedToHistory(context, query);
								break;
							}
							case "Summary":
							case "SummaryDisplay": {
								SummaryLogicForDisplay summaryLogicForDisplayObj;
								summaryLogicForDisplayObj = new SummaryLogicForDisplay();
								SummaryDisplayObjInstance.instance().summaryLogicForDisplayObj = summaryLogicForDisplayObj;

								summaryLogicForDisplayObj.setBarcodeQuery(query);

								try {
									summaryLogicForDisplayObj.processRawJSON(obj.getRawJson());
								} catch (Exception e) {
									e.printStackTrace();
								}

								Intent intent = new Intent(context, SummaryDisplay.class);
								intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
								intent.putExtra("barcode", query);  //this barcode refers to the query barcode.
								context.startActivity(intent);
								lastAddedToHistory(context, query);
								break;
							}
							case "MoveDisplay": {
//								containerList.clear();
								ListView containerListLV = ((Activity) context).findViewById(R.id.listViewGetContainersToMove);
								ArrayAdapter<String> adapter = (ArrayAdapter<String>) containerListLV.getAdapter();
								adapter.clear();
								adapter.notifyDataSetChanged();
								Toast.makeText(context, "The move was successful.",
										Toast.LENGTH_LONG).show();
								EditText destinationET = ((Activity) context).findViewById(R.id.destinationET);
								destinationET.requestFocus();
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

		}

	}

	private void lastAddedToHistory(Context context, String barcodeQuery) {
		String lastAdded = null;
		switch (context.getClass().getSimpleName()) {
			case "LookupDisplay":
			case "Lookup": {
				if (!lookupHistory.isEmpty()) {
					lastAdded = lookupHistory.get(0);
				}
				if (!barcodeQuery.equals(lastAdded) & !barcodeQuery.isEmpty()) {
					lookupHistory.add(0, barcodeQuery);
				}
				break;
			}
			case "Summary":
			case "SummaryDisplay": {
				if (!summaryHistory.isEmpty()) {
					lastAdded = summaryHistory.get(0);
				}
				if (!barcodeQuery.equals(lastAdded) & !barcodeQuery.isEmpty()) {
					summaryHistory.add(0, barcodeQuery);
				}
				break;
			}
		}
	}

	public String containersToMoveStr(ArrayList<String> list) {
		String delim = "&c=";

		StringBuilder sb = new StringBuilder();

		sb.append(delim);
		int i = 0;
		while (i < list.size() - 1) {
			sb.append(list.get(i));
			sb.append(delim);
			i++;
		}
		sb.append(list.get(i));

		String res = sb.toString();
		return res;
	}

}
