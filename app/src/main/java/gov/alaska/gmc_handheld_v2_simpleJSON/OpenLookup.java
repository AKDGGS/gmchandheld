package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;


public class OpenLookup {

	public OpenLookup() {
	}

	private LinkedList<String> lookupHistory = LookupHistoryHolder.getInstance().getLookupHistory();
	private LinkedList<String> summaryHistory = SummaryHistoryHolder.getInstance().getSummaryHistory();
	private String containerListStr;
	private String query = null;

	public static final String SHARED_PREFS = "sharedPrefs";
//	public static final String LOOKUPHISTORYSP = "lookupHistorySP";

	private boolean downloading = false;

	public boolean isDownloading() {
		return downloading;
	}

	public void setDownloading(boolean downloading) {
		this.downloading = downloading;
	}


	@SuppressLint("StaticFieldLeak")
	public void processDataForDisplay(final String queryOrDestination, final ArrayList<String> containerList, final Context context) {

//		SharedPreferences sp = context.getApplicationContext().getSharedPreferences("", Context.MODE_PRIVATE);
//		String s2 = sp.getString("lookupHistoryString", "");
//		System.out.println(s2);

		SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
		String url = sharedPreferences.getString("urlText", "");

		switch (context.getClass().getSimpleName()) {
			case "Lookup":
			case "LookupDisplay": {
				try {
					query = URLEncoder.encode(queryOrDestination, "utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

				url = url + "inventory.json?barcode=" + queryOrDestination;
				break;
			}
			case "Summary":
			case "SummaryDisplay": {
				url = url + "summary.json?barcode=" + queryOrDestination;
				break;
			}
			case "MoveDisplay": {
				if(containerList.size() > 0) {
					containerListStr = containersToMoveStr(containerList);
					try {
						query = URLEncoder.encode(queryOrDestination, "utf-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					query = "d=" + query + containerListStr;
				}
				url = url +"move.json?" +  query;
				break;
			}
		}

		RemoteApiTask remoteApiTask = new RemoteApiTask();
		remoteApiTask.processDataForDisplay(url, queryOrDestination, containerListStr, context);
	}


	public String containersToMoveStr(ArrayList<String> list) {
		String delim = "&c=";

		StringBuilder sb = new StringBuilder();

		sb.append(delim);
		int i = 0;
		while (i < list.size() - 1) {

			try {
				sb.append(URLEncoder.encode(list.get(i), "utf-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			sb.append(delim);
			i++;
		}
		sb.append(list.get(i));

		return sb.toString();
	}
}
