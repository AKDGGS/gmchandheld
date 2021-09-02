package gov.alaska.gmchandheld;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import com.google.zxing.integration.android.IntentIntegrator;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;

public abstract class BaseActivity extends AppCompatActivity {
	protected static SharedPreferences sp;
	protected static SharedPreferences.Editor editor;
	public static String apiKeyBase = null;
	protected Toolbar toolbar;
	protected IntentIntegrator qrScan;
	protected static Intent intent;
	protected static String baseURL;



	@Override
	protected void onStop() {
		super.onStop();
		PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		if (!pm.isScreenOn()){
			apiKeyBase = "";
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		checkAPIkeyExists(this);
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
		} else if (item.getItemId() == (R.id.photo_test)) {
			Intent intentAddContainer = new Intent(this, TakePhoto_Test.class);
			intentAddContainer.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			this.startActivity(intentAddContainer);
			return true;
		}else {
			return super.onOptionsItemSelected(item);
		}
	}

	public void enableTSL(Context mContext){
		try {
			// enables TSL-1.2 if Google Play is updated on old devices.
			// doesn't work with emulators
			// https://stackoverflow.com/a/29946540
			ProviderInstaller.installIfNeeded(mContext);
		} catch (GooglePlayServicesRepairableException e) {
			e.printStackTrace();
		} catch (GooglePlayServicesNotAvailableException e) {
			e.printStackTrace();
		}
		SSLContext sslContext;
		try {
			sslContext = SSLContext.getInstance("TLSv1.2");
			sslContext.init(null, null, null);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
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
}