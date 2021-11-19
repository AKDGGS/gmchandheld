package gov.alaska.gmchandheld;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.net.HttpURLConnection;
import java.util.Date;
import java.util.HashMap;

public class UpdateBroadcastReceiver extends BroadcastReceiver implements RemoteAPIDownloadCallback {
    private static Thread t1, t2;
    private static RemoteAPIDownload updateChecker, issuesChecker;
    private SharedPreferences sp;
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        sp = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        if (t1 == null) {
            updateChecker = new RemoteAPIDownload();
            t1 = new Thread(updateChecker, "updateCheckerThread");
            t1.start();
        }

        HashMap<String, Object> params = new HashMap<>();

        try {
            updateChecker.setFetchDataObj(BaseActivity.sp.getString("urlText", "") + "app/current.apk",
                    this,
                    RemoteAPIDownload.HEAD,
                    params,
                    null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (t2 == null) {
            issuesChecker = new RemoteAPIDownload();
            t2 = new Thread(issuesChecker, "updateCheckerThread");
            t2.start();
        }

        try {
            issuesChecker.setFetchDataObj(BaseActivity.sp.getString("urlText", "") + "qualitylist.json",
                    this,
                    RemoteAPIDownload.GET,
                    params,
                    null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void displayData(byte[] byteData, Date updateBuildDate, int responseCode, String responseMessage, int requestType) {
        if (responseCode < HttpURLConnection.HTTP_BAD_REQUEST) {
            switch (requestType) {
                case RemoteAPIDownload.GET:
                    String data = new String(byteData);
                    SharedPreferences.Editor editor;
                    editor = sp.edit();
                    editor.putString("issuesString", data).apply();
                    break;
                case RemoteAPIDownload.HEAD:
                    Date buildDate = new Date(BuildConfig.TIMESTAMP);

                    //gets the last refused modified date from shared preferences.
                    // (The last refused modified date comes from UpdateDownloadAPKHandler
                    long lastRefusedUpdate = BaseActivity.sp.getLong("ignoreUpdateDateSP", 0);
                    if (!(updateBuildDate.compareTo(new Date(lastRefusedUpdate)) == 0) &
                            (buildDate.compareTo(updateBuildDate) < 0)) {
                        BaseActivity.updatable = true;
                        BaseActivity.updateAvailableBuildDate = updateBuildDate;
                        System.out.println("Update available");
                    } else {
                        BaseActivity.updatable = false;
                        System.out.println("No Update available");
                    }
                    break;
                default:
                    System.out.println("Update Broadcast Exception: the requestType didn't match GET or HEAD.");
            }
        } else {
            System.out.println("There is a problem with the URL.");
        }
    }

    @Override
    public void displayException(Exception e) {
        if (e.getMessage() != null) {
            System.out.println("Broadcast Exception: " + e.getMessage());
        }
        e.printStackTrace();
    }
}