package gov.alaska.gmchandheld;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import java.util.LinkedList;

public class DisplayData {
    private static LinkedList<String> lookupHistory, summaryHistory;

    public DisplayData() {
        lookupHistory = LookupDisplayObjInstance.getInstance().getLookupHistory();
        summaryHistory = SummaryDisplayObjInstance.getInstance().getSummaryHistory();
    }

    public void displayData(Context mContext, String barcode, String data){
        switch (mContext.getClass().getSimpleName()) {
            case "LookupDisplay":
            case "Lookup": {
                LookupLogicForDisplay lookupLogicForDisplayObj;
                lookupLogicForDisplayObj = new LookupLogicForDisplay(mContext);
                LookupDisplayObjInstance.getInstance().lookupLogicForDisplayObj
                        = lookupLogicForDisplayObj;
                lookupLogicForDisplayObj.setBarcodeQuery(barcode);
                try {
                    lookupLogicForDisplayObj.processRawJSON(data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(mContext, LookupDisplay.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("barcode", barcode);
                mContext.startActivity(intent);
                lastAddedToHistory(mContext, barcode);
                break;
            }
            case "Summary":
            case "SummaryDisplay": {
                SummaryLogicForDisplay summaryLogicForDisplayObj;
                summaryLogicForDisplayObj = new SummaryLogicForDisplay();
                SummaryDisplayObjInstance.getInstance().summaryLogicForDisplayObj
                        = summaryLogicForDisplayObj;
                summaryLogicForDisplayObj.setBarcodeQuery(barcode);
                try {
                    summaryLogicForDisplayObj.processRawJSON(data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(mContext, SummaryDisplay.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("barcode", barcode);
                mContext.startActivity(intent);
                lastAddedToHistory(mContext, barcode);
                break;
            }
            case "MoveContents": {
                Toast.makeText(mContext, "The contents were moved.",
                        Toast.LENGTH_LONG).show();
                break;
            }
            case "MoveDisplay": {
//                    containerList.clear();
                ListView containerListLV = ((Activity) mContext)
                        .findViewById(R.id.listViewContainersToMove);
                ArrayAdapter<String> adapter =
                        (ArrayAdapter<String>) containerListLV.getAdapter();
                adapter.clear();
                adapter.notifyDataSetChanged();
                Toast.makeText(mContext, "The move was successful.",
                        Toast.LENGTH_LONG).show();
                EditText destinationET = ((Activity) mContext).findViewById(R.id.toET);
                destinationET.requestFocus();
                break;
            }
            case "AddContainer": {
                Toast.makeText(mContext, "The container was added.",
                        Toast.LENGTH_SHORT).show();
                break;
            }
            case "AddInventory": {
                Toast.makeText(mContext, "The inventory was added.",
                        Toast.LENGTH_SHORT).show();
                break;
            }
            case "Quality": {
                Toast.makeText(mContext, "The inventory quality was added.",
                        Toast.LENGTH_SHORT).show();
                break;
            }
            case "AuditDisplay": {
//                    containerList.clear();
                ListView containerListLV = ((Activity) mContext)
                        .findViewById(R.id.listViewGetContainersToAudit);
                ArrayAdapter<String> adapter =
                        (ArrayAdapter<String>) containerListLV.getAdapter();
                adapter.clear();
                adapter.notifyDataSetChanged();
                Toast.makeText(mContext, "Items added to the Audit table.",
                        Toast.LENGTH_LONG).show();
                EditText remarkET = ((Activity) mContext).findViewById(R.id.remarkET);
                remarkET.requestFocus();
                break;
            }
            case "Recode": {
                EditText oldBarcodeET =
                        ((Activity) mContext).findViewById(R.id.oldBarcodeET);
                oldBarcodeET.requestFocus();
                Toast.makeText(mContext, "Recode successful.",
                        Toast.LENGTH_LONG).show();
                break;
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
