package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Field;

public class BaseActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.base);


		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			menuKeyField.setAccessible(true);
			menuKeyField.setBoolean(config, false);
		} catch (Exception ex) {
			//ignore
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
			summaryLogicForDisplayObj = SummaryDisplayObjInstance.instance().summaryLogicForDisplayObj;

			if (summaryLogicForDisplayObj == null) {
				Intent get_barcode = new Intent(this, Summary.class);
//				get_barcode.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(get_barcode);
			} else {
				Intent summary = new Intent(this, SummaryDisplay.class);
//				summary.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(summary);
			}
			return true;
		} else if (item.getItemId() == (R.id.lookup)) {
			LookupLogicForDisplay lookupLogicForDisplayObj;
			lookupLogicForDisplayObj = LookupDisplayObjInstance.instance().lookupLogicForDisplayObj;

			if (lookupLogicForDisplayObj == null) {
				Intent get_barcode = new Intent(this, Lookup.class);
//				get_barcode.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(get_barcode);
			} else {
				Intent lookup = new Intent(this, LookupDisplay.class);
//				lookup.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(lookup);
			}
			return true;
		} else if (item.getItemId() == (R.id.configuration)) {
			Intent intent_configuration = new Intent(this, Configuration.class);
			intent_configuration.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			this.startActivity(intent_configuration);
			return true;
		} else if (item.getItemId() == (R.id.move)) {
			Intent intent_move = new Intent(this, MoveDisplay.class);
			intent_move.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//			this.startActivityForResult(intent_move, 1);
			this.startActivity(intent_move);
			return true;
		}else {
			return super.onOptionsItemSelected(item);
		}


//		 else if (item.getItemId() == (R.id.audit)) {
//			Intent intent_audit = new Intent(this, AuditDisplay.class);
//			intent_audit.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
////			this.startActivityForResult(intent_move, 1);
//			this.startActivity(intent_audit);
//			return true;
//		}


		// DELETE THIS SWITCH STATEMENT AFTER ADDITIONAL FUNCTIONS ARE FINISHED.
//		switch (item.getItemId()) {
////			case R.id.help:
////				Intent intent_help = new Intent(this, Help.class);
////				this.startActivity(intent_help);
////				return true;
//
//			case R.id.summary: {
//				SummaryLogicForDisplay summaryLogicForDisplayObj;
//				summaryLogicForDisplayObj = SummaryDisplayObjInstance.instance().summaryLogicForDisplayObj;
//
//				if (summaryLogicForDisplayObj == null) {
//					Intent get_barcode = new Intent(this, Summary.class);
//					startActivity(get_barcode);
//				} else {
//					Intent summary = new Intent(this, SummaryDisplay.class);
//					startActivity(summary);
//				}
//				return true;
//			}
//
//			case R.id.lookup:
//				LookupLogicForDisplay lookupLogicForDisplayObj;
//				lookupLogicForDisplayObj = LookupDisplayObjInstance.instance().lookupLogicForDisplayObj;
//
//				if (lookupLogicForDisplayObj == null) {
//					Intent get_barcode = new Intent(this, MainActivity.class);
//					startActivity(get_barcode);
//				} else {
//					Intent lookup = new Intent(this, LookupDisplay.class);
//					startActivity(lookup);
//				}
//				return true;
//
////			case R.id.move:
////				Intent intent_move = new Intent(this, Move.class);
////				this.startActivity(intent_move);
////				return true;
////
////			case R.id.recode:
////				Intent intent_recode = new Intent(this, Recode.class);
////				this.startActivity(intent_recode);
////				return true;
////
////			case R.id.add_inventory:
////
////				Intent intent_add_inventory = new Intent(this, AddInventory.class);
////
////				this.startActivity(intent_add_inventory);
////				return true;
////
////			case R.id.add_container:
////				Intent intent_add_container = new Intent(this, AddContainer.class);
////				this.startActivity(intent_add_container);
////				return true;
////

//
//			case R.id.configuration:
//				Intent intent_configuration = new Intent(this, Configuration.class);
//				this.startActivity(intent_configuration);
//				return true;

//			default:
//				return super.onOptionsItemSelected(item);
//		}
	}



}