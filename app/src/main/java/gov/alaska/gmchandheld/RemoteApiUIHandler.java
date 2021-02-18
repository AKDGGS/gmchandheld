package gov.alaska.gmchandheld;

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


public class RemoteApiUIHandler {

	public RemoteApiUIHandler() {
	}

	private final LinkedList<String> lookupHistory = LookupHistoryHolder.getInstance().getLookupHistory();
	private final LinkedList<String> summaryHistory = SummaryHistoryHolder.getInstance().getSummaryHistory();

	private boolean downloading = false;
	public boolean isDownloading() {
		return !downloading;
	}
	public void setDownloading(boolean downloading) {
		this.downloading = downloading;
	}

	private static ArrayList<String> containerList;
	public static void setContainerList(ArrayList<String> moveList) {containerList = moveList;}

	private static String urlFirstParameter;
	public static void setUrlFirstParameter(String query) {RemoteApiUIHandler.urlFirstParameter = query;}

	private static String destinationBarcode;
	public static void setDestinationBarcode(String destinationBarcode) {RemoteApiUIHandler.destinationBarcode = destinationBarcode;}

	private static String addContainerName;
	public static void setAddContainerName(String addContainerName) {
		RemoteApiUIHandler.addContainerName = addContainerName;
	}

	private static String addContainerRemark;
	public static void setAddContainerRemark(String addContainerRemark) {
		RemoteApiUIHandler.addContainerRemark = addContainerRemark;
	}

	private static String getNewBarcode;
	public static void setGetNewBarcode(String getNewBarcode) {
		RemoteApiUIHandler.getNewBarcode = getNewBarcode;
	}

	AsyncTask<String, Integer, RemoteApiDownload> asyncTask = null;

	@SuppressLint("StaticFieldLeak")  //Can be ignored because the Asynctask is short lived.  https://stackoverflow.com/a/46166223
	public void processDataForDisplay( final Context context) {

		if (downloading) {
			asyncTask = new AsyncTask<String, Integer, RemoteApiDownload>() {

				AlertDialog alert;

				@Override
				protected void onPreExecute() {
					super.onPreExecute();

					AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
					LayoutInflater inflater = ((Activity) context).getLayoutInflater();
					View layout = inflater.inflate(R.layout.downloading_progress_dialog, (ViewGroup) ((Activity) context).findViewById(R.id.downloading_alert_root));
					alertDialog.setView(layout);

					TextView title = new TextView(context);
					String processingTitle = "Processing " + urlFirstParameter;
					title.setText(processingTitle);
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

					if (!urlFirstParameter.isEmpty()) {
						switch (context.getClass().getSimpleName()) {
							case "Lookup": {
								lastAddedToHistory(context, urlFirstParameter);
								ListView listView = ((Activity) context).findViewById(R.id.listViewGetBarcodeHistory);
								ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1);
								adapter.addAll(lookupHistory);
								adapter.notifyDataSetChanged();
								listView.setAdapter(adapter);
								break;
							}
							case "Summary": {
								lastAddedToHistory(context, urlFirstParameter);
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
				protected RemoteApiDownload doInBackground(String... strings) {
					if (!isCancelled()) {
						RemoteApiDownload remoteAPIDownload = null;
						switch (context.getClass().getSimpleName()) {
							case "Lookup":
							case "LookupDisplay":
							case "Summary":
							case "SummaryDisplay": {

								remoteAPIDownload = new RemoteApiDownload(context);
								remoteAPIDownload.setUrlFirstParameter(urlFirstParameter);
								break;
							}
							case "MoveDisplay": {
								remoteAPIDownload = new RemoteApiDownload(context);
								remoteAPIDownload.setUrlFirstParameter(urlFirstParameter);
								remoteAPIDownload.setContainerList(containerList);
								break;
							}

							case "MoveContents": {
								remoteAPIDownload = new RemoteApiDownload(context);
								remoteAPIDownload.setUrlFirstParameter(urlFirstParameter);
								remoteAPIDownload.setDestinationBarcode(destinationBarcode);
								break;
							}

							case "AddContainer":{
								remoteAPIDownload = new RemoteApiDownload(context);
								remoteAPIDownload.setUrlFirstParameter(urlFirstParameter);
								remoteAPIDownload.setAddedContainerName(addContainerName);
								remoteAPIDownload.setAddedContainerRemark(addContainerRemark);
								break;
							}

							case "AuditDisplay":{
								remoteAPIDownload = new RemoteApiDownload(context);
								remoteAPIDownload.setUrlFirstParameter(urlFirstParameter);
								remoteAPIDownload.setContainerList(containerList);
								break;
							}

							case "Recode":{
								remoteAPIDownload = new RemoteApiDownload(context);
								remoteAPIDownload.setUrlFirstParameter(urlFirstParameter);
								remoteAPIDownload.setNewBarcode(getNewBarcode);
								break;
							}
						}
						assert remoteAPIDownload != null;
						remoteAPIDownload.getDataFromURL();
						return remoteAPIDownload;
					}
					return null;
				}

				@Override
				protected void onPostExecute(RemoteApiDownload obj) {
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
									case "Summary": {
										EditText getBarcodeEditText = ((Activity) context).findViewById(R.id.getBarcodeEditText);
										getBarcodeEditText.setText("");
										getBarcodeEditText.requestFocus();
										break;
									}
									case "SummaryDisplay":
									case "LookupDisplay": {
//										lastAddedToHistory(context, queryOrDestination);
										EditText invisibleEditText = ((Activity) context).findViewById(R.id.invisibleEditText);
										invisibleEditText.setText("");
										invisibleEditText.requestFocus();
										break;
									}
									case "MoveContents": {
										EditText sourceET = ((Activity) context).findViewById(R.id.sourceET);
										sourceET.setText(urlFirstParameter);
										EditText destinationET = ((Activity) context).findViewById(R.id.destinationET);
										destinationET.setText(destinationBarcode);
										destinationET.requestFocus();
										break;
									}
									case "MoveDisplay": {
										EditText destinationET = ((Activity) context).findViewById(R.id.destinationET);
										destinationET.setText(urlFirstParameter);
										EditText moveContainerET = ((Activity) context).findViewById(R.id.moveContainerET);
										moveContainerET.requestFocus();
										break;
									}
									case "Recode": {
										EditText oldBarcodeET = ((Activity) context).findViewById(R.id.getOldBarcodeEditText);
										oldBarcodeET.setText(urlFirstParameter);
										EditText newBarcodeET = ((Activity) context).findViewById(R.id.getNewBarcodeEditText);
										oldBarcodeET.requestFocus();
										break;
									}
								}
							}

						});

						AlertDialog alert = alertDialog.create();
						alert.setCanceledOnTouchOutside(false);
						alert.show();
					} else  {
						switch (context.getClass().getSimpleName()) {
							case "LookupDisplay":
							case "Lookup": {
								LookupLogicForDisplay lookupLogicForDisplayObj;
								lookupLogicForDisplayObj = new LookupLogicForDisplay(context);
								LookupDisplayObjInstance.instance().lookupLogicForDisplayObj = lookupLogicForDisplayObj;

								lookupLogicForDisplayObj.setBarcodeQuery(urlFirstParameter);

								try {
									lookupLogicForDisplayObj.processRawJSON(obj.getRawJson());
								} catch (Exception e) {
									e.printStackTrace();
								}

								Intent intent = new Intent(context, LookupDisplay.class);
								intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
								intent.putExtra("barcode", urlFirstParameter);  //this barcode refers to the query barcode.
								context.startActivity(intent);

								lastAddedToHistory(context, urlFirstParameter);
								break;
							}
							case "Summary":
							case "SummaryDisplay": {
								SummaryLogicForDisplay summaryLogicForDisplayObj;
								summaryLogicForDisplayObj = new SummaryLogicForDisplay();
								SummaryDisplayObjInstance.instance().summaryLogicForDisplayObj = summaryLogicForDisplayObj;

								summaryLogicForDisplayObj.setBarcodeQuery(urlFirstParameter);


								try {
									summaryLogicForDisplayObj.processRawJSON(obj.getRawJson());
								} catch (Exception e) {
									e.printStackTrace();
								}

								Intent intent = new Intent(context, SummaryDisplay.class);
								intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
								intent.putExtra("barcode", urlFirstParameter);  //this barcode refers to the query barcode.
								context.startActivity(intent);
								lastAddedToHistory(context, urlFirstParameter);
								break;
							}
							case "MoveContents": {
								Toast.makeText(context, "The contents were moved.",
										Toast.LENGTH_LONG).show();
								break;
							}
							case "MoveDisplay": {
								containerList.clear();
								ListView containerListLV = ((Activity) context).findViewById(R.id.listViewGetContainersToMove);
								ArrayAdapter<String> adapter = (ArrayAdapter<String>) containerListLV.getAdapter();
								adapter.clear();
								adapter.notifyDataSetChanged();
								Toast.makeText(context, "The move was successful.",
										Toast.LENGTH_LONG).show();
								EditText destinationET = ((Activity) context).findViewById(R.id.destinationET);
								destinationET.requestFocus();
								break;
							}
							case "AddContainer": {
								Toast.makeText(context, "The container was added.",
										Toast.LENGTH_LONG).show();
								break;
							}
							case "AuditDisplay": {
								containerList.clear();
								ListView containerListLV = ((Activity) context).findViewById(R.id.listViewGetContainersToAudit);
								ArrayAdapter<String> adapter = (ArrayAdapter<String>) containerListLV.getAdapter();
								adapter.clear();
								adapter.notifyDataSetChanged();
								Toast.makeText(context, "Items added to the Audit table.",
										Toast.LENGTH_LONG).show();
								EditText remarkET = ((Activity) context).findViewById(R.id.remarkET);
								remarkET.requestFocus();
								break;
							}
							case "Recode": {
								EditText oldBarcodeET = ((Activity) context).findViewById(R.id.getOldBarcodeEditText);
								oldBarcodeET.requestFocus();
								Toast.makeText(context, "Recode successful.",
										Toast.LENGTH_LONG).show();
								break;
							}
						}
					}
				}
			}.execute();
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


}
