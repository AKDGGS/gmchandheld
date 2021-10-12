package gov.alaska.gmchandheld;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.zxing.integration.android.IntentIntegrator;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public abstract class BaseActivity extends AppCompatActivity {
    public static String apiKeyBase = null;
    protected static SharedPreferences sp;
    protected static SharedPreferences.Editor editor;
    protected static Intent intent;
    protected static String baseURL;
    protected Toolbar toolbar;
    protected IntentIntegrator qrScan;
    protected Thread thread;
    protected volatile AlertDialog alert;
    protected RemoteAPIDownload remoteAPIDownload;


    @Override
    protected void onStop() {
        super.onStop();
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        if (!pm.isScreenOn()) {
            apiKeyBase = "";
        }
        if (alert != null) {
            alert.dismiss();
            alert = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (alert != null) {
            alert.dismiss();
            alert = null;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
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
        checkAPIkeyExists(this);
        baseURL = BaseActivity.sp.getString("urlText", "");

        remoteAPIDownload = new RemoteAPIDownload();
        if (thread == null) {
            thread = new Thread(remoteAPIDownload, "remoteAPIDownloadThread");
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
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == (R.id.summary)) {
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
        } else if (item.getItemId() == (R.id.lookup)) {
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
        if (null == apiKeyBase || apiKeyBase.isEmpty()) {
            Intent intent = new Intent(mContext, GetToken.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mContext.startActivity(intent);
        }
    }

    protected void processingAlert(Context mContext, String barcode) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        View layout = inflater.inflate(R.layout.downloading_progress_dialog,
                ((Activity) mContext).findViewById(R.id.downloading_alert_root));
        alertDialog.setView(layout);
        TextView title = new TextView(mContext);
        String processingTitle = "Processing " + barcode;
        title.setText(processingTitle);
        title.setGravity(Gravity.CENTER);
        title.setTextSize(16);
        alertDialog.setCustomTitle(title);
        alertDialog.setNegativeButton("Cancel", (dialogInterface, i) -> {
            dialogInterface.cancel();
            if (alert != null) {
                alert.dismiss();
                alert = null;
            }
        });
        alert = alertDialog.create();
        alert.show();
        thread.interrupt();
        remoteAPIDownload.setUrl(null);
        alert.setCanceledOnTouchOutside(false);
    }

    public String createListForURL(ArrayList<String> list, String paramKeyword) {
        String delim = "&" + paramKeyword + "=";
        StringBuilder sb = new StringBuilder();
        if (list != null && list.size() > 0) {
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
        }
        return sb.toString();
    }
}