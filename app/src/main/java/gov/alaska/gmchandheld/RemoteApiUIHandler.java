package gov.alaska.gmchandheld;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;

public final class RemoteApiUIHandler  extends AsyncTask<String, String, RemoteApiDownload> {
    private String urlFirstParameter, secondParameter, name,remark;
    private LinkedList<String> lookupHistory, summaryHistory;
    private ArrayList<String> containerList;
    private static boolean downloading;
    private WeakReference<Context> mContext;
    private int apiKeyAttempts;

    //Lookup, LookupDisplay, Summary, SummaryDisplay
    public RemoteApiUIHandler(Context mContext, String urlFirstParameter) {
        this.mContext= new WeakReference<>(mContext);
        this.urlFirstParameter = urlFirstParameter;
        apiKeyAttempts = 0;
        lookupHistory = LookupDisplayObjInstance.getInstance().getLookupHistory();
        summaryHistory = SummaryDisplayObjInstance.getInstance().getSummaryHistory();
//        downloading = true;
    }
    //MoveContents, Recode
    public RemoteApiUIHandler(Context mContext, String urlFirstParameter, String secondParameter) {
        this(mContext, urlFirstParameter);
        this.secondParameter = secondParameter;
    }
    //MoveDisplay, Audit
    public RemoteApiUIHandler(Context mContext, String urlFirstParameter,
                              ArrayList<String> containerList) {
        this(mContext, urlFirstParameter);
        this.containerList = containerList;
    }
    //AddContainer
    public RemoteApiUIHandler(Context mContext, String urlFirstParameter, String name,
                              String remark) {
        this(mContext, urlFirstParameter);
        this.name = name;
        this.remark = remark;
    }

    //AddInventory, Quality
    public RemoteApiUIHandler(Context mContext, String urlFirstParameter, String remark,
                              ArrayList<String> containerList) {
        this(mContext, urlFirstParameter);
        this.remark = remark;
        this.containerList = containerList;
    }

    public static void setDownloading(boolean b) {
        downloading = b;
    }
    public static boolean isDownloading() {
        return downloading;
    }

    private AlertDialog alert;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext.get());
        LayoutInflater inflater = ((Activity) mContext.get()).getLayoutInflater();
        View layout = inflater.inflate(R.layout.downloading_progress_dialog,
                ((Activity) mContext.get()).findViewById(R.id.downloading_alert_root));
        alertDialog.setView(layout);
        TextView title = new TextView(mContext.get());
        String processingTitle = "Processing " + urlFirstParameter;
        title.setText(processingTitle);
        title.setGravity(Gravity.CENTER);
        title.setTextSize(16);
        alertDialog.setCustomTitle(title);
        alertDialog.setNegativeButton("Cancel", (dialogInterface, i) -> {
            downloading = false;
            cancel(true);
        });
        alert = alertDialog.create();
        alert.show();
        alert.setCanceledOnTouchOutside(false);
        if (!urlFirstParameter.isEmpty()) {
            switch (mContext.get().getClass().getSimpleName()) {
                case "Lookup": {
                    lastAddedToHistory(mContext.get(), urlFirstParameter);
                    ListView listView =
                            ((Activity) mContext.get()).findViewById(R.id.listViewBarcodeHistory);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext.get(),
                            android.R.layout.simple_list_item_1);
                    adapter.addAll(lookupHistory);
                    adapter.notifyDataSetChanged();
                    listView.setAdapter(adapter);
                    break;
                }
                case "Summary": {
                    lastAddedToHistory(mContext.get(), urlFirstParameter);
                    ListView listView =
                            ((Activity) mContext.get()).findViewById(R.id.listViewSummaryHistory);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext.get(),
                            android.R.layout.simple_list_item_1);
                    adapter.addAll(summaryHistory);
                    adapter.notifyDataSetChanged();
                    listView.setAdapter(adapter);
                    break;
                }
            }
        }
    }

    @Override
    protected RemoteApiDownload doInBackground(String... strings) {
        while (!isCancelled()){
            RemoteApiDownload remoteAPIDownload;
            remoteAPIDownload = new RemoteApiDownload(mContext.get());
            switch (mContext.get().getClass().getSimpleName()) {
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
                    remoteAPIDownload.setDestinationBarcode(secondParameter);
                    break;
                }
                case "AddContainer": {
                    remoteAPIDownload.setUrlFirstParameter(urlFirstParameter);
                    remoteAPIDownload.setAddedContainerName(name);
                    remoteAPIDownload.setAddedContainerRemark(remark);
                    break;
                }
                case "AddInventory":
                case "Quality": {
                    remoteAPIDownload.setUrlFirstParameter(urlFirstParameter);
                    if (remark.trim().length() > 0) {
                        remoteAPIDownload.setAddedContainerRemark(remark);
                    }
                    if (containerList != null) {
                        remoteAPIDownload.setContainerList(containerList);
                    }
                    break;
                }
                case "AuditDisplay": {
                    remoteAPIDownload.setUrlFirstParameter(urlFirstParameter);
                    remoteAPIDownload.setContainerList(containerList);
                    break;
                }
                case "Recode": {
                    remoteAPIDownload.setUrlFirstParameter(urlFirstParameter);
                    remoteAPIDownload.setNewBarcode(secondParameter);
                    break;
                }
            }
            remoteAPIDownload.getDataFromURL();
            return remoteAPIDownload;
        }
        return null;
    }

    @Override
    protected void onPostExecute(RemoteApiDownload remoteApiDownload) {
        //Dismisses the downloading alert without user intervention
        if (alert != null) {
            alert.dismiss();
        }
        if (remoteApiDownload.isErrored()) {
            int responseCode = remoteApiDownload.getResponseCode();
            System.out.println("Remote Api Download response code: " + responseCode);
            if (responseCode == 403) {
                Intent intentGetBarcode = new Intent(mContext.get().getApplicationContext(),
                        GetToken.class);
                intentGetBarcode.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (apiKeyAttempts == 0){
                    mContext.get().getApplicationContext().startActivity(intentGetBarcode);
                    apiKeyAttempts = apiKeyAttempts + 1;
                } else {
                    apiKeyAttempts = 0;
                }
            } else {
                LayoutInflater inflater = ((Activity) mContext.get()).getLayoutInflater();
                View layout = inflater.inflate(R.layout.lookup_error_display,
                        ((Activity) mContext.get()).findViewById(R.id.lookup_error_root));
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext.get());
                ConnectivityManager cm = (ConnectivityManager)
                        mContext.get().getSystemService(Context.CONNECTIVITY_SERVICE);
                if (responseCode == 404) {
                    alertDialog.setTitle("URL Error");
                    alertDialog.setMessage("In the configuration screen, check the URL.");
                } else if (responseCode >= 500) {
                    alertDialog.setTitle("Internal Server Error");
                    alertDialog.setMessage(remoteApiDownload.getException().getMessage());
                } else if ((Settings.System.getInt(mContext.get().getContentResolver(),
                        Settings.Global.AIRPLANE_MODE_ON, 0) != 0)) {
                    alertDialog.setMessage("Is the device connected to the internet/network?  " +
                            "Check if Air Plane mode is on.");
                } else if (!(cm.getActiveNetworkInfo() != null &&
                        cm.getActiveNetworkInfo().isConnected())) {
                    alertDialog.setMessage("Is the device connected to the internet/network?  " +
                            "Check if the connection has been lost.");
                } else {
                    alertDialog.setMessage(remoteApiDownload.getException().getMessage());
                }
                alertDialog.setView(layout);
                alertDialog.setPositiveButton("Dismiss", (dialog, which) -> {
                    downloading = false;
                    switch (mContext.get().getClass().getSimpleName()) {
                        case "Lookup":
                        case "Summary": {
                            EditText getBarcodeEditText =
                                    ((Activity) mContext.get()).findViewById(R.id.barcodeET);
                            getBarcodeEditText.setText("");
                            getBarcodeEditText.requestFocus();
                            break;
                        }
                        case "SummaryDisplay":
                        case "LookupDisplay": {
                            EditText invisibleET =
                                    ((Activity) mContext.get()).findViewById(R.id.invisibleET);
                            invisibleET.setText("");
                            invisibleET.requestFocus();
                            break;
                        }
                        case "MoveContents": {
                            EditText sourceET =
                                    ((Activity) mContext.get()).findViewById(R.id.fromET);
                            sourceET.setText(urlFirstParameter);
                            EditText destinationET =
                                    ((Activity) mContext.get()).findViewById(R.id.toET);
                            destinationET.setText(secondParameter);
                            destinationET.requestFocus();
                            break;
                        }
                        case "MoveDisplay": {
                            EditText destinationET =
                                    ((Activity) mContext.get()).findViewById(R.id.toET);
                            destinationET.setText(urlFirstParameter);
                            EditText moveContainerET =
                                    ((Activity) mContext.get()).findViewById(R.id.itemET);
                            moveContainerET.requestFocus();
                            break;
                        }
                        case "Recode": {
                            EditText oldBarcodeET =
                                    ((Activity) mContext.get()).findViewById(R.id.oldBarcodeET);
                            oldBarcodeET.setText(urlFirstParameter);
                            oldBarcodeET.requestFocus();
                            break;
                        }
                    }
                });
                AlertDialog alert = alertDialog.create();
                alert.setCanceledOnTouchOutside(false);
                alert.show();
            }
        } else {
            switch (mContext.get().getClass().getSimpleName()) {
                case "LookupDisplay":
                case "Lookup": {
                    LookupLogicForDisplay lookupLogicForDisplayObj;
                    lookupLogicForDisplayObj = new LookupLogicForDisplay(mContext.get());
                    LookupDisplayObjInstance.getInstance().lookupLogicForDisplayObj
                            = lookupLogicForDisplayObj;
                    lookupLogicForDisplayObj.setBarcodeQuery(urlFirstParameter);
                    try {
                        lookupLogicForDisplayObj.processRawJSON(remoteApiDownload.getRawJson());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Intent intent = new Intent(mContext.get(), LookupDisplay.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("barcode", urlFirstParameter);
                    mContext.get().startActivity(intent);
                    lastAddedToHistory(mContext.get(), urlFirstParameter);
                    break;
                }
                case "Summary":
                case "SummaryDisplay": {
                    SummaryLogicForDisplay summaryLogicForDisplayObj;
                    summaryLogicForDisplayObj = new SummaryLogicForDisplay();
                    SummaryDisplayObjInstance.getInstance().summaryLogicForDisplayObj
                            = summaryLogicForDisplayObj;
                    summaryLogicForDisplayObj.setBarcodeQuery(urlFirstParameter);
                    try {
                        summaryLogicForDisplayObj.processRawJSON(remoteApiDownload.getRawJson());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Intent intent = new Intent(mContext.get(), SummaryDisplay.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("barcode", urlFirstParameter);
                    mContext.get().startActivity(intent);
                    lastAddedToHistory(mContext.get(), urlFirstParameter);
                    break;
                }
                case "MoveContents": {
                    Toast.makeText(mContext.get(), "The contents were moved.",
                            Toast.LENGTH_LONG).show();
                    break;
                }
                case "MoveDisplay": {
                    containerList.clear();
                    ListView containerListLV = ((Activity) mContext.get())
                            .findViewById(R.id.listViewContainersToMove);
                    ArrayAdapter<String> adapter =
                            (ArrayAdapter<String>) containerListLV.getAdapter();
                    adapter.clear();
                    adapter.notifyDataSetChanged();
                    Toast.makeText(mContext.get(), "The move was successful.",
                            Toast.LENGTH_LONG).show();
                    EditText destinationET = ((Activity) mContext.get()).findViewById(R.id.toET);
                    destinationET.requestFocus();
                    break;
                }
                case "AddContainer": {
                    Toast.makeText(mContext.get(), "The container was added.",
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                case "AddInventory": {
                    Toast.makeText(mContext.get(), "The inventory was added.",
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                case "Quality": {
                    Toast.makeText(mContext.get(), "The inventory quality was added.",
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                case "AuditDisplay": {
//                    containerList.clear();
                    ListView containerListLV = ((Activity) mContext.get())
                            .findViewById(R.id.listViewGetContainersToAudit);
                    ArrayAdapter<String> adapter =
                            (ArrayAdapter<String>) containerListLV.getAdapter();
                    adapter.clear();
                    adapter.notifyDataSetChanged();
                    Toast.makeText(mContext.get(), "Items added to the Audit table.",
                            Toast.LENGTH_LONG).show();
                    EditText remarkET = ((Activity) mContext.get()).findViewById(R.id.remarkET);
                    remarkET.requestFocus();
                    break;
                }
                case "Recode": {
                    EditText oldBarcodeET =
                            ((Activity) mContext.get()).findViewById(R.id.oldBarcodeET);
                    oldBarcodeET.requestFocus();
                    Toast.makeText(mContext.get(), "Recode successful.",
                            Toast.LENGTH_LONG).show();
                    break;
                }
            }
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

