package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Field;

public class BaseActivity extends AppCompatActivity {
//	public static final String EXTRA_TEXT = "com.example.user_input_no_button.EXTRA_TEXT";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.base);
//		checkConnection(this);

		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			menuKeyField.setAccessible(true);
			menuKeyField.setBoolean(config, false);
		} catch (Exception ex) {
			// Ignore
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

		switch (item.getItemId()) {
//			case R.id.help:
//				Intent intent_help = new Intent(this, Help.class);
//				this.startActivity(intent_help);
//				return true;

			case R.id.summary: {
				SummaryLogicForDisplay summaryLogicForDisplayObj;
				summaryLogicForDisplayObj = SummaryDisplayObjInstance.instance().summaryLogicForDisplayObj;

				if (summaryLogicForDisplayObj == null) {
					Intent get_barcode = new Intent(this, Summary.class);
					startActivity(get_barcode);
				} else {
					Intent summary = new Intent(this, SummaryDisplay.class);
					startActivity(summary);
				}
				return true;
			}

			case R.id.lookup:
				LookupLogicForDisplay lookupLogicForDisplayObj;
				lookupLogicForDisplayObj = LookupDisplayObjInstance.instance().lookupLogicForDisplayObj;

				if (lookupLogicForDisplayObj == null) {
					Intent get_barcode = new Intent(this, MainActivity.class);
					startActivity(get_barcode);
				} else {
					Intent lookup = new Intent(this, LookupDisplay.class);
					startActivity(lookup);
				}
				return true;

//			case R.id.move:
//				Intent intent_move = new Intent(this, Move.class);
//				this.startActivity(intent_move);
//				return true;
//
//			case R.id.recode:
//				Intent intent_recode = new Intent(this, Recode.class);
//				this.startActivity(intent_recode);
//				return true;
//
//			case R.id.add_inventory:
//
//				Intent intent_add_inventory = new Intent(this, AddInventory.class);
//
//				this.startActivity(intent_add_inventory);
//				return true;
//
//			case R.id.add_container:
//				Intent intent_add_container = new Intent(this, AddContainer.class);
//				this.startActivity(intent_add_container);
//				return true;
//
//			case R.id.audit:
//				Intent intent_audit = new Intent(this, Audit.class);
//				this.startActivity(intent_audit);
//				return true;

			case R.id.configuration:
				Intent intent_configuration = new Intent(this, Configuration.class);
				this.startActivity(intent_configuration);
				return true;


			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private boolean checkConnection(final Context context) {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (Settings.System.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0) {

			LayoutInflater inflater = this.getLayoutInflater();
			View layout = inflater.inflate(R.layout.airplane_mode_alert, (ViewGroup) ((Activity) context).findViewById(R.id.airplane_mode_root));

			AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
			TextView title = new TextView(context);
			title.setText("It appears that Airplane Mode is turned on.");
			title.setGravity(Gravity.CENTER);
			title.setTextSize(16);
			alertDialog.setCustomTitle(title);

			alertDialog.setView(layout);

			alertDialog.setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which){
				}
			});

			AlertDialog alert = alertDialog.create();
			alert.setCanceledOnTouchOutside(false);
			alert.show();
		} else if (! (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected())) {
			LayoutInflater inflater = this.getLayoutInflater();
			View layout = inflater.inflate(R.layout.airplane_mode_alert, (ViewGroup) ((Activity) context).findViewById(R.id.airplane_mode_root));

			AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
			TextView title = new TextView(context);
			title.setText("It appears the device is not connected to the network.");
			title.setGravity(Gravity.CENTER);
			title.setTextSize(16);
			alertDialog.setCustomTitle(title);

			alertDialog.setView(layout);

			alertDialog.setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});

			AlertDialog alert = alertDialog.create();
			alert.setCanceledOnTouchOutside(false);
			alert.show();
		}
		return true;
	}
}