package gov.alaska.gmchandheld;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.zxing.integration.android.IntentIntegrator;

import java.util.Date;

public abstract class BaseActivity extends AppCompatActivity implements HTTPRequestCallback {
    protected static SharedPreferences sp;
    protected static SharedPreferences.Editor editor;
    protected static Intent intent;
    protected static String baseURL;
    protected static volatile boolean updatable;
    protected static Date updateAvailableBuildDate;
    private static String token = null;
    protected static Thread thread;
    private static HTTPRequest HTTPRequest;
    protected IntentIntegrator qrScan;
    protected AlertDialog alert;
    protected Toolbar toolbar;

    public static boolean getUpdatable() {
        return updatable;
    }

    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        BaseActivity.token = token;
    }

    public static HTTPRequest getHTTPRequest() {
        return HTTPRequest;
    }

    @Override
    protected void onStop() {
        super.onStop();
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        if (!pm.isScreenOn()) {
            token = "";
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (null != alert){
            alert.dismiss();
        }
        checkAPIkeyExists(this);
        baseURL = BaseActivity.sp.getString("urlText", "");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());
        configureToolbar();
        sp = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        editor = sp.edit();

        if (sp.getString("apkSavePath", "").isEmpty()) {
            String filename = "current.apk";
            editor.putString("apkSavePath", BaseActivity.this.getExternalCacheDir() + "/" + filename).apply();
        }

        checkAPIkeyExists(this);

        baseURL = BaseActivity.sp.getString("urlText", "");

        if (thread == null) {
            HTTPRequest = new HTTPRequest();
            thread = new Thread(HTTPRequest, "HTTPRequestThread");
            thread.start();
        }
    }

    protected abstract int getLayoutResource();

    private void configureToolbar() {
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        PackageManager pm = this.getPackageManager();
        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.no_camera_menu, menu);
        } else {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.main_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == (R.id.lookup)) {
            LookupLogicForDisplay lookupLogicForDisplayObj;
            lookupLogicForDisplayObj = LookupDisplayObjInstance.getInstance()
                    .lookupLogicForDisplayObj;
            if (lookupLogicForDisplayObj == null) {
                Intent intentGetBarcode = new Intent(this, Lookup.class);
                intentGetBarcode.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intentGetBarcode);
            } else {
                Intent intentLookup = new Intent(this, LookupDisplay.class);
                intentLookup.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intentLookup);
            }
            return true;
        } else if (item.getItemId() == (R.id.summary)) {
            SummaryLogicForDisplay summaryLogicForDisplayObj;
            summaryLogicForDisplayObj = SummaryDisplayObjInstance.getInstance()
                    .summaryLogicForDisplayObj;
            if (summaryLogicForDisplayObj == null) {
                Intent intentGetBarcode = new Intent(this, Summary.class);
                intentGetBarcode.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intentGetBarcode);
            } else {
                Intent intentSummary = new Intent(this, SummaryDisplay.class);
                intentSummary.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intentSummary);
            }
            return true;
        } else if (item.getItemId() == (R.id.configuration)) {
            Intent intentConfiguration = new Intent(this, Configuration.class);
            intentConfiguration.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            this.startActivity(intentConfiguration);
            return true;
        } else if (item.getItemId() == (R.id.move)) {
            Intent intentMove = new Intent(this, MoveDisplay.class);
            intentMove.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            this.startActivity(intentMove);
            return true;
        } else if (item.getItemId() == (R.id.moveContent)) {
            Intent intentMove = new Intent(this, MoveContents.class);
            intentMove.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            this.startActivity(intentMove);
            return true;
        } else if (item.getItemId() == (R.id.add_container)) {
            Intent intentAddContainer = new Intent(this, AddContainer.class);
            intentAddContainer.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            this.startActivity(intentAddContainer);
            return true;
        } else if (item.getItemId() == (R.id.add_inventory)) {
            Intent intentAddInventory = new Intent(this, AddInventory.class);
            intentAddInventory.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            this.startActivity(intentAddInventory);
            return true;
        } else if (item.getItemId() == (R.id.quality)) {
            Intent intentQuality = new Intent(this, Quality.class);
            intentQuality.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            this.startActivity(intentQuality);
            return true;
        } else if (item.getItemId() == (R.id.audit)) {
            Intent intentAddContainer = new Intent(this, AuditDisplay.class);
            intentAddContainer.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            this.startActivity(intentAddContainer);
            return true;
        } else if (item.getItemId() == (R.id.recode)) {
            Intent intentAddContainer = new Intent(this, Recode.class);
            intentAddContainer.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            this.startActivity(intentAddContainer);
            return true;
        } else if (item.getItemId() == (R.id.photo)) {
            Intent intentAddContainer = new Intent(this, TakePhoto.class);
            intentAddContainer.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            this.startActivity(intentAddContainer);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    protected void checkUrlUsesHttps(Context mContext) {
        sp = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        String url = sp.getString("urlText", "");
        if (!url.startsWith("https")) {
            Intent intent = new Intent(mContext, Configuration.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mContext.startActivity(intent);
        }
    }

    protected void checkAPIkeyExists(Context mContext) {
        if (null == token || token.isEmpty()) {
            Intent intent = new Intent(mContext, GetToken.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mContext.startActivity(intent);
        }
    }

    public void setAlarm() {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, UpdateBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 101, intent, 0);

        am.setRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 5000, // five second delay
                Integer.parseInt(sp.getString("interval", "60")) * 60 * 1000L,
                pendingIntent);
    }

    public void cancelAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, UpdateBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 101, intent, 0);
        alarmManager.cancel(pendingIntent);
    }

    public void downloadingAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Update Available.");
        builder.setCancelable(true);
        builder.setPositiveButton(
                "Update",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Configuration.editor.putLong("ignoreUpdateDateSP", BaseActivity.updateAvailableBuildDate.getTime())
                                .apply();
                        Intent intentConfiguration = new Intent(BaseActivity.this, Configuration.class);
                        intentConfiguration.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        BaseActivity.this.startActivity(intentConfiguration);
                    }
                });

        builder.setNegativeButton(
                "Ignore the Update",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //If a user refuses an update, the last modified date for that update
                        // is saved in shared preferences,
                        Configuration.editor.putLong("ignoreUpdateDateSP", BaseActivity.updateAvailableBuildDate.getTime())
                                .apply();
                        BaseActivity.updatable = false;
                    }
                });
        if (alert == null) {
            alert = builder.create();
            alert.show();
        }
    }
}