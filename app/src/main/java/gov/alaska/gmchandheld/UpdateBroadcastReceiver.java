package gov.alaska.gmchandheld;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.util.Calendar;
import java.util.Date;

public class UpdateBroadcastReceiver extends BroadcastReceiver implements RemoteAPIDownloadCallback {
    private static Thread t1, t2;
    private static RemoteAPIDownload updateChecker, issuesChecker;
    private SharedPreferences sp;
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        sp = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);

        if (t1 == null) {
            updateChecker = new RemoteAPIDownload();
            t1 = new Thread(updateChecker, "updateCheckerThread");
            t1.start();
        }

        try {
            updateChecker.setFetchDataObj(BaseActivity.sp.getString("urlText", "") + "app/current.apk",
                    BaseActivity.getToken(),
                    null,
                    this,
                    RemoteAPIDownload.HEAD);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (t2 == null) {
            issuesChecker = new RemoteAPIDownload();
            t2 = new Thread(issuesChecker, "updateCheckerThread");
            t2.start();
        }

        try {
            issuesChecker.setFetchDataObj("https://maps.dggs.alaska.gov/gmcdev/qualitylist.json",
                    BaseActivity.getToken(),
                    null,
                    this,
                    RemoteAPIDownload.GET);
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void displayData(String data, int responseCode, String responseMessage, int requestType) {
        if (responseCode < HttpURLConnection.HTTP_BAD_REQUEST) {
            switch (requestType) {
                case RemoteAPIDownload.GET:
                    SharedPreferences.Editor editor;
                    editor = sp.edit();
                    editor.putString("issuesString", data).apply();
                    break;
                case RemoteAPIDownload.HEAD:
                    Date updateBuildDate = new Date(data);
                    Date buildDate = new Date(BuildConfig.TIMESTAMP);

                    //gets the last refused modified date from shared preferences.
                    // (The last refused modified date comes from UpdateDownloadAPKHandler
                    long lastRefusedUpdate = BaseActivity.sp.getLong("ignoreUpdateDateSP", 0);
                    if (!(updateBuildDate.compareTo(new Date(lastRefusedUpdate)) == 0) &
                            (buildDate.compareTo(updateBuildDate) < 0)) {
                        BaseActivity.updateAvailable = true;
                        BaseActivity.updateAvailableBuildDate = updateBuildDate;
                    } else {
                        BaseActivity.updateAvailable = false;
                    }
                    break;
                default:
                    System.out.println("Update Broadcast Exception: the requestType didn't match GET or HEAD.");
            }
        } else if (responseCode == 403) {
            Toast.makeText(mContext,
                    "The token is not correct.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(mContext, GetToken.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    mContext.startActivity(intent);
        } else if (responseCode == 404) {
                    Toast.makeText(mContext,
                            "The URL is not correct.", Toast.LENGTH_LONG).show();
                    BaseActivity.editor.putString("urlText", "").apply();
                    Intent intent = new Intent(mContext, GetToken.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    mContext.startActivity(intent);

        }  else {
            Toast.makeText(mContext,
                    "Something went wrong.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void displayException(Exception e) {
        System.out.println("Broadcast Exception: " + e.getMessage());
        e.printStackTrace();
    }
}