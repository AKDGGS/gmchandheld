package gov.alaska.gmchandheld;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

import okhttp3.Request;

public class UpdateBroadcastReceiver extends BroadcastReceiver implements RemoteAPIDownloadCallback {
    Thread t1, t2;
    SharedPreferences sp;

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("time: " + Calendar.getInstance().getTime());
        sp = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);

        if (sp.getString("urlText", "").isEmpty()) {
            Intent getURLIntent = new Intent(context, GetToken.class);
            getURLIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(getURLIntent);
        } else {
            Request request = new Request.Builder()
                    .header("Authorization", "Token " + BaseActivity.apiKeyBase)
                    .url(BaseActivity.sp.getString("urlText", "") + "app/current.apk")
                    .head()
                    .build();
            RemoteAPIDownload updateChecker = new RemoteAPIDownload();
            if (t1 == null) {
                t1 = new Thread(updateChecker, "updateCheck");
                t1.start();
            }

            try {
                updateChecker.setFetchDataObj(BaseActivity.sp.getString("urlText", "") + "app/current.apk",
                        BaseActivity.apiKeyBase,
                        null,
                        this,
                        RemoteAPIDownload.HEAD);
            } catch (Exception e) {
//                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }

            RemoteAPIDownload issuesChecker = new RemoteAPIDownload();
            if (t2 == null) {
                t2 = new Thread(issuesChecker, "updateCheck");
                t2.start();
            }

            Request requestCheckIssues = new Request.Builder()
                    .header("Authorization", "Token " + BaseActivity.apiKeyBase)
                    .url(BaseActivity.sp.getString("urlText", "") + "/qualitylist.json")
                    .build();
            try {
                issuesChecker.setFetchDataObj("https://maps.dggs.alaska.gov/gmcdev/qualitylist.json",
                        BaseActivity.apiKeyBase,
                        null,
                        this,
                        RemoteAPIDownload.GET);
            } catch (Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void displayData(String data, int responseCode, String responseMessage, int requestType) {
        if (responseCode != 403) {
            switch (requestType) {
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
                case RemoteAPIDownload.GET:
                    SharedPreferences.Editor editor;
                    editor = sp.edit();
                    editor.putString("issuesString", data).apply();
                    break;
                default:
                    System.out.println("Error: the requestType didn't match");
            }
        }
    }

    @Override
    public void displayException(Exception e) {
        System.out.println("Broadcast error: " + e.getMessage());
        e.printStackTrace();
    }
}