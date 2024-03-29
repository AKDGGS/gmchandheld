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

import org.conscrypt.Conscrypt;

import java.security.Security;
import java.util.Date;

public abstract class BaseActivity extends AppCompatActivity implements HTTPRequestCallback {
    protected static SharedPreferences sp;
    protected static SharedPreferences.Editor editor;
    protected static Intent intent;
    protected static String baseURL;
    private static volatile boolean updatable; //Set in UpdateBroadcastReceiver and Configuration
    private static volatile boolean updating; //Indicates that the user has already agreed to update
    protected static Date updateAvailableBuildDate;
    private static String token = null;
    protected static Thread thread;
    private static HTTPRequest HTTPRequest;
    protected IntentIntegrator qrScan;
    protected AlertDialog alert;
    protected Toolbar toolbar;
    protected abstract int getLayoutResource();

    public static boolean getUpdatable() {
        return updatable;
    }

    public static boolean getUpdating() {
        return updating;
    }
    public static void setUpdating(boolean value) {
        updating = value;
    }

    public static void setUpdatable(boolean value) {
        updatable = value;
    }

    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        BaseActivity.token = token;
    }
    public static void setURL(String url) {
        BaseActivity.baseURL = url;
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
        //enables using HTTPS in the emulator for API 16
        if (android.os.Build.VERSION.SDK_INT < 17) {
            Security.insertProviderAt(Conscrypt.newProvider(), 1);
        }
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
        switch (item.getItemId()){
            case R.id.lookup:
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
            case R.id.summary:
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
            case R.id.configuration:
                Intent intentConfiguration = new Intent(this, Configuration.class);
                intentConfiguration.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                this.startActivity(intentConfiguration);
                return true;
            case R.id.move: {
                Intent intentMove = new Intent(this, MoveDisplay.class);
                intentMove.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                this.startActivity(intentMove);
                return true;
            }
            case R.id.moveContent: {
                Intent intentMove = new Intent(this, MoveContents.class);
                intentMove.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                this.startActivity(intentMove);
                return true;
            }
            case R.id.add_container: {
                Intent intentAddContainer = new Intent(this, AddContainer.class);
                intentAddContainer.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                this.startActivity(intentAddContainer);
                return true;
            }
            case R.id.add_inventory: {
                Intent intentAddInventory = new Intent(this, AddInventory.class);
                intentAddInventory.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                this.startActivity(intentAddInventory);
                return true;
            }
            case R.id.quality: {
                Intent intentQuality = new Intent(this, Quality.class);
                intentQuality.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                this.startActivity(intentQuality);
                return true;
            }
            case R.id.audit: {
                Intent intentAddContainer = new Intent(this, AuditDisplay.class);
                intentAddContainer.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                this.startActivity(intentAddContainer);
                return true;
            }
            case R.id.recode: {
                Intent intentAddContainer = new Intent(this, Recode.class);
                intentAddContainer.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                this.startActivity(intentAddContainer);
                return true;
            }
            case R.id.photo: {
                Intent intentAddContainer = new Intent(this, TakePhoto.class);
                intentAddContainer.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                this.startActivity(intentAddContainer);
                return true;
            }
            default:
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
                        updating = true;
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