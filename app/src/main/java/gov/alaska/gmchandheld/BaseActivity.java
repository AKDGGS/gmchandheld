package gov.alaska.gmchandheld;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.util.SharedPreferencesUtils;

import java.lang.reflect.Field;

public class BaseActivity extends AppCompatActivity {

	protected SharedPreferences sp;
	protected final String SHARED_PREFS = "sharedPrefs";
	protected static SharedPreferences.Editor editor;

//	@Override
//	protected void onRestart() {
//		super.onRestart();
//		// works for api 24, but doesn't work in api 16 or 20
//		// works with swipe
//		KeyguardManager myKM = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
//		System.out.println("Base act: " + myKM.isKeyguardLocked());
//		if (!myKM.isKeyguardLocked()) {
//			System.out.println("SCREEN was TURNED OFF 2");
//			SharedPreferences sp = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
//			Configuration.editor = sp.edit();
//			Configuration.editor.putString("apiText", "Screen was turned off 2").apply();
//		}
//	}

	@Override
	protected void onStop() {
		super.onStop();
		SharedPreferences sp = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
		PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		if (!pm.isScreenOn()){
			Configuration.editor = sp.edit();
			Configuration.editor.putString("apiText", "").apply();
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		SharedPreferences sp = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
		String apiTextValue = sp.getString("apiText", "");
		if (apiTextValue.equals("")){
			Intent intentGetBarcode = new Intent(this.getApplicationContext(), GetToken.class);
			intentGetBarcode.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			this.getApplicationContext().startActivity(intentGetBarcode);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.base);
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
			summaryLogicForDisplayObj = SummaryDisplayObjInstance.getInstance().summaryLogicForDisplayObj;

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
			lookupLogicForDisplayObj = LookupDisplayObjInstance.getInstance().lookupLogicForDisplayObj;

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
		}else if (item.getItemId() == (R.id.photo)) {
			Intent intentAddContainer = new Intent(this, TakePhoto.class);
			intentAddContainer.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			this.startActivity(intentAddContainer);
			return true;
		}

		else{
			return super.onOptionsItemSelected(item);
		}
	}
}