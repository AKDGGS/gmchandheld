package gov.alaska.gmchandheld;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
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

import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;


public class RemoteApiUIHandler extends AppCompatActivity {

    public RemoteApiUIHandler() {

    }

    public static final LinkedList<String> lookupHistory = LookupDisplayObjInstance.getInstance().getLookupHistory();
    private static final LinkedList<String> summaryHistory = SummaryDisplayObjInstance.getInstance().getSummaryHistory();

    private static boolean downloading = false;

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

    @Override
    protected void onRestart() {
        super.onRestart();
        RemoteApiUIHandler.this.finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translucent);

    }

    public static class ProcessDataForDisplay extends AsyncTask<String, String, RemoteApiDownload> {

        private WeakReference<Context> mActivity;

        public ProcessDataForDisplay(Context context) {
            mActivity = new WeakReference<>(context);
        }

        AlertDialog alert;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(mActivity.get());
            LayoutInflater inflater = ((Activity) mActivity.get()).getLayoutInflater();

            View layout = inflater.inflate(R.layout.downloading_progress_dialog, (ViewGroup) ((Activity) mActivity.get()).findViewById(R.id.downloading_alert_root));
            alertDialog.setView(layout);

            TextView title = new TextView(mActivity.get());
            String processingTitle = "Processing " + urlFirstParameter;
            title.setText(processingTitle);
            title.setGravity(Gravity.CENTER);
            title.setTextSize(16);
            alertDialog.setCustomTitle(title);
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    cancel(true);
                    downloading = false;
                }
            });

            alert = alertDialog.create();
            alert.show();
            alert.setCanceledOnTouchOutside(false);

            if (!urlFirstParameter.isEmpty()) {
                switch (mActivity.get().getClass().getSimpleName()) {
                    case "Lookup": {
                        lastAddedToHistory(mActivity.get(), urlFirstParameter);
                        ListView listView = ((Activity) mActivity.get()).findViewById(R.id.listViewBarcodeHistory);
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(mActivity.get(), android.R.layout.simple_list_item_1);
                        adapter.addAll(lookupHistory);
                        adapter.notifyDataSetChanged();
                        listView.setAdapter(adapter);
                        break;
                    }
                    case "Summary": {
                        lastAddedToHistory(mActivity.get(), urlFirstParameter);
                        ListView listView = ((Activity) mActivity.get()).findViewById(R.id.listViewSummaryHistory);
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(mActivity.get(), android.R.layout.simple_list_item_1);
                        adapter.addAll(summaryHistory);
                        adapter.notifyDataSetChanged();
                        listView.setAdapter(adapter);
                        break;
                    }
                }
            }
            downloading = false;
        }

        protected RemoteApiDownload doInBackground(String... strings) {
            if (!isCancelled()) {
                RemoteApiDownload remoteAPIDownload;
                remoteAPIDownload = new RemoteApiDownload(mActivity.get());
                switch (mActivity.get().getClass().getSimpleName()) {
                    case "Lookup":
                    case "LookupDisplay":
                    case "Summary":
                    case "SummaryDisplay": {
                        remoteAPIDownload.setUrlFirstParameter(urlFirstParameter);
                        break;
                    }
                    case "MoveDisplay": {
                        remoteAPIDownload.setUrlFirstParameter(urlFirstParameter);
                        remoteAPIDownload.setContainerList(containerList);
                        break;
                    }

                    case "MoveContents": {

                        remoteAPIDownload.setUrlFirstParameter(urlFirstParameter);
                        remoteAPIDownload.setDestinationBarcode(destinationBarcode);
                        break;
                    }

                    case "AddContainer": {
                        remoteAPIDownload.setUrlFirstParameter(urlFirstParameter);
                        remoteAPIDownload.setAddedContainerName(addContainerName);
                        remoteAPIDownload.setAddedContainerRemark(addContainerRemark);
                        break;
                    }

                    case "AddInventory": {
                        remoteAPIDownload.setUrlFirstParameter(urlFirstParameter);
                        remoteAPIDownload.setAddedContainerRemark(addContainerRemark);
                        remoteAPIDownload.setContainerList(containerList);
                        break;
                    }

                    case "AuditDisplay": {
                        remoteAPIDownload.setUrlFirstParameter(urlFirstParameter);
                        remoteAPIDownload.setContainerList(containerList);
                        break;
                    }

                    case "Recode": {
                        remoteAPIDownload.setUrlFirstParameter(urlFirstParameter);
                        remoteAPIDownload.setNewBarcode(getNewBarcode);
                        break;
                    }
                }
                remoteAPIDownload.getDataFromURL();

                return remoteAPIDownload;
            }
            return null;
        }

        @Override
        protected void onPostExecute(RemoteApiDownload obj) {
            //Dismisses the downloading alert.  This is needed if the download fails.
            if (alert != null) {
                alert.dismiss();
            }

            if (obj.isErrored()) {

                LayoutInflater inflater = ((Activity) mActivity.get()).getLayoutInflater();
                View layout = inflater.inflate(R.layout.lookup_error_display, (ViewGroup) ((Activity) mActivity.get()).findViewById(R.id.lookup_error_root));

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(mActivity.get());

                int responseCode = obj.getResponseCode();
                ConnectivityManager cm = (ConnectivityManager) mActivity.get().getSystemService(Context.CONNECTIVITY_SERVICE);

                if (responseCode == 403) {
                    alertDialog.setTitle(obj.getException().getMessage());
                    alertDialog.setMessage("In the configuration screen, check the API key.");
                } else if (responseCode == 404) {
                    alertDialog.setTitle("URL Error");
                    alertDialog.setMessage("In the configuration screen, check the URL.");
                } else if (responseCode >= 500) {
                    alertDialog.setTitle("Internal Server Error");
                    alertDialog.setMessage(obj.getException().getMessage());
                } else if ((Settings.System.getInt(mActivity.get().getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0)) {
                    alertDialog.setMessage("Is the device connected to the internet/network?  " +
                                           "Check if Air Plane mode is on.");
                } else if (!(cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected())) {
                    alertDialog.setMessage("Is the device connected to the internet/network?  " +
                                           "Check if the connection has been lost.");
                } else {
                    alertDialog.setMessage(obj.getException().getMessage());
                }

                alertDialog.setView(layout);
                alertDialog.setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        downloading = true;
                        switch (mActivity.get().getClass().getSimpleName()) {
                            case "Lookup":
                            case "Summary": {
                                EditText getBarcodeEditText = ((Activity) mActivity.get()).findViewById(R.id.barcodeET);
                                getBarcodeEditText.setText("");
                                getBarcodeEditText.requestFocus();
                                break;
                            }
                            case "SummaryDisplay":
                            case "LookupDisplay": {
//										lastAddedToHistory(context, queryOrDestination);
                                EditText invisibleEditText = ((Activity) mActivity.get()).findViewById(R.id.invisibleEditText);
                                invisibleEditText.setText("");
                                invisibleEditText.requestFocus();
                                break;
                            }
                            case "MoveContents": {
                                EditText sourceET = ((Activity) mActivity.get()).findViewById(R.id.fromET);
                                sourceET.setText(urlFirstParameter);
                                EditText destinationET = ((Activity) mActivity.get()).findViewById(R.id.toET);
                                destinationET.setText(destinationBarcode);
                                destinationET.requestFocus();
                                break;
                            }
                            case "MoveDisplay": {
                                EditText destinationET = ((Activity) mActivity.get()).findViewById(R.id.toET);
                                destinationET.setText(urlFirstParameter);
                                EditText moveContainerET = ((Activity) mActivity.get()).findViewById(R.id.itemET);
                                moveContainerET.requestFocus();
                                break;
                            }
                            case "Recode": {
                                EditText oldBarcodeET = ((Activity) mActivity.get()).findViewById(R.id.oldBarcodeET);
                                oldBarcodeET.setText(urlFirstParameter);
                                EditText newBarcodeET = ((Activity) mActivity.get()).findViewById(R.id.newBarcodeET);
                                oldBarcodeET.requestFocus();
                                break;
                            }
                        }
                    }

                });

                AlertDialog alert = alertDialog.create();
                alert.setCanceledOnTouchOutside(false);
                alert.show();
            } else {
                switch (mActivity.get().getClass().getSimpleName()) {
                    case "LookupDisplay":
                    case "Lookup": {
                        LookupLogicForDisplay lookupLogicForDisplayObj;
                        lookupLogicForDisplayObj = new LookupLogicForDisplay(mActivity.get());
                        LookupDisplayObjInstance.getInstance().lookupLogicForDisplayObj = lookupLogicForDisplayObj;

                        lookupLogicForDisplayObj.setBarcodeQuery(urlFirstParameter);

                        try {
                            lookupLogicForDisplayObj.processRawJSON(obj.getRawJson());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Intent intent = new Intent(mActivity.get(), LookupDisplay.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("barcode", urlFirstParameter);  //this barcode refers to the query barcode.
                        mActivity.get().startActivity(intent);

                        lastAddedToHistory(mActivity.get(), urlFirstParameter);
                        break;
                    }
                    case "Summary":
                    case "SummaryDisplay": {
                        SummaryLogicForDisplay summaryLogicForDisplayObj;
                        summaryLogicForDisplayObj = new SummaryLogicForDisplay();
                        SummaryDisplayObjInstance.getInstance().summaryLogicForDisplayObj = summaryLogicForDisplayObj;
                        summaryLogicForDisplayObj.setBarcodeQuery(urlFirstParameter);

                        try {
                            summaryLogicForDisplayObj.processRawJSON(obj.getRawJson());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Intent intent = new Intent(mActivity.get(), SummaryDisplay.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("barcode", urlFirstParameter);  //this barcode refers to the query barcode.
                        mActivity.get().startActivity(intent);
                        lastAddedToHistory(mActivity.get(), urlFirstParameter);
                        break;
                    }
                    case "MoveContents": {
                        Toast.makeText(mActivity.get(), "The contents were moved.",
                                       Toast.LENGTH_LONG).show();
                        break;
                    }
                    case "MoveDisplay": {
                        containerList.clear();
                        ListView containerListLV = ((Activity) mActivity.get()).findViewById(R.id.listViewContainersToMove);
                        ArrayAdapter<String> adapter = (ArrayAdapter<String>) containerListLV.getAdapter();

                        adapter.clear();
                        adapter.notifyDataSetChanged();
                        Toast.makeText(mActivity.get(), "The move was successful.",
                                       Toast.LENGTH_LONG).show();
                        EditText destinationET = ((Activity) mActivity.get()).findViewById(R.id.toET);
                        destinationET.requestFocus();
                        break;
                    }
                    case "AddContainer": {
                        Toast.makeText(mActivity.get(), "The container was added.",
                                       Toast.LENGTH_LONG).show();
                        break;
                    }

                    case "AddInventory": {
                        Toast.makeText(mActivity.get(), "The inventory was added.",
                                       Toast.LENGTH_LONG).show();
                        break;
                    }

                    case "AuditDisplay": {
                        containerList.clear();
                        ListView containerListLV = ((Activity) mActivity.get()).findViewById(R.id.listViewGetContainersToAudit);
                        ArrayAdapter<String> adapter = (ArrayAdapter<String>) containerListLV.getAdapter();
                        adapter.clear();
                        adapter.notifyDataSetChanged();
                        Toast.makeText(mActivity.get(), "Items added to the Audit table.",
                                       Toast.LENGTH_LONG).show();
                        EditText remarkET = ((Activity) mActivity.get()).findViewById(R.id.remarkET);
                        remarkET.requestFocus();
                        break;
                    }
                    case "Recode": {
                        EditText oldBarcodeET = ((Activity) mActivity.get()).findViewById(R.id.oldBarcodeET);
                        oldBarcodeET.requestFocus();
                        Toast.makeText(mActivity.get(), "Recode successful.",
                                       Toast.LENGTH_LONG).show();
                        break;
                    }
                }
            }
        }
    }


    private static void lastAddedToHistory(Context context, String barcodeQuery) {

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