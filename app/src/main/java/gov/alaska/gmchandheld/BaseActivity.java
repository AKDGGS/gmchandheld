package gov.alaska.gmchandheld;

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
		} catch (Exception e) {
			e.printStackTrace();
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
		}else{
			return super.onOptionsItemSelected(item);
		}
	}
}