package gov.alaska.gmchandheld;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.HashMap;

public class UpdateBroadcastReceiver extends BroadcastReceiver implements HTTPRequestCallback {
    private static Thread t1, t2;
    private static HTTPRequest updateChecker, issuesChecker;
    private SharedPreferences sp;

    @Override
    public void onReceive(Context context, Intent intent) {
        sp = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        if (t1 == null) {
            updateChecker = new HTTPRequest();
            t1 = new Thread(updateChecker, "updateCheckerThread");
            t1.start();
        }
        HashMap<String, Object> params = new HashMap<>();
        try {
            updateChecker.setFetchDataObj(BaseActivity.sp.getString("urlText", "") + "app/current.apk",
                    this,
                    HTTPRequest.HEAD,
                    params,
                    null);
        } catch (MalformedURLException e) {
            Toast.makeText(context.getApplicationContext(),
                    "The URL is not correct.", Toast.LENGTH_LONG).show();
            BaseActivity.editor.putString("urlText", "").apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (t2 == null) {
            issuesChecker = new HTTPRequest();
            t2 = new Thread(issuesChecker, "updateCheckerThread");
            t2.start();
        }
        try {
            issuesChecker.setFetchDataObj(BaseActivity.sp.getString("urlText", "") + "qualitylist.json",
                    this,
                    HTTPRequest.GET,
                    params,
                    null);
        } catch (Exception e) {
            Toast.makeText(context.getApplicationContext(),
                    "The URL is not correct.", Toast.LENGTH_LONG).show();
            BaseActivity.editor.putString("urlText", "").apply();
            e.printStackTrace();
        }
    }

    @Override
    public void displayData(byte[] byteData, Date updateBuildDate, int responseCode, String responseMessage, int requestType) {
        if (responseCode < HttpURLConnection.HTTP_BAD_REQUEST) {
            switch (requestType) {
                case HTTPRequest.GET:
                    String data = new String(byteData);
                    SharedPreferences.Editor editor;
                    editor = sp.edit();
                    editor.putString("issuesString", data).apply();
                    break;
                case HTTPRequest.HEAD:
                    Date buildDate = new Date(BuildConfig.TIMESTAMP);
                    //gets the last refused modified date from shared preferences.
                    // (The last refused modified date comes from UpdateDownloadAPKHandler
                    long lastRefusedUpdate = BaseActivity.sp.getLong("ignoreUpdateDateSP", 0);
                    if (!(updateBuildDate.compareTo(new Date(lastRefusedUpdate)) == 0) &
                            (buildDate.compareTo(updateBuildDate) < 0)) {
                        BaseActivity.updateAvailableBuildDate = updateBuildDate;
                        BaseActivity.setUpdatable(true);
                    } else {
                        //no update available
                        BaseActivity.setUpdatable(false);
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