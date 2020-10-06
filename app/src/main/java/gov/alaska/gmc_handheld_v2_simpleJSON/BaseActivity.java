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
    public static String button_pushed;
    public static final String EXTRA_TEXT = "com.example.user_input_no_button.EXTRA_TEXT";

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

        switch(item.getItemId()){
            case R.id.help:
                button_pushed = "Help";
                Intent intent_help = new Intent(this, Help.class);
                intent_help.putExtra(EXTRA_TEXT, button_pushed);
                this.startActivity(intent_help);
                return true;

            case R.id.summary: {
                SummaryLogicForDisplay summaryLogicForDisplayObj;
                summaryLogicForDisplayObj = SummaryDisplayObjInstance.instance().summaryLogicForDisplay;

                if (summaryLogicForDisplayObj == null) {
                    Intent get_barcode = new Intent(this, Summary.class);
                    get_barcode.putExtra(EXTRA_TEXT, button_pushed);
                    startActivity(get_barcode);
                } else {
                    Intent lookup = new Intent(this, SummaryDisplay.class);
                    startActivity(lookup);
                }
                return true;
            }

            case R.id.lookup:
                LookupLogicForDisplay lookupLogicForDisplayObj;
                lookupLogicForDisplayObj = LookupDisplayObjInstance.instance().lookupLogicForDisplayObj;

                if(lookupLogicForDisplayObj == null) {
                    Intent get_barcode = new Intent(this, MainActivity.class);
                    get_barcode.putExtra(EXTRA_TEXT, button_pushed);
                    startActivity(get_barcode);
                }else{
                    Intent lookup = new Intent(this, LookupDisplay.class);
                    startActivity(lookup);
                }
                return true;

            case R.id.move:
                button_pushed = "Move";
                Intent intent_move = new Intent(this, Move.class);
                intent_move.putExtra(EXTRA_TEXT, button_pushed);
                this.startActivity(intent_move);
                return true;

            case R.id.recode:
                button_pushed = "Recode";
                Intent intent_recode = new Intent(this, Recode.class);
                intent_recode.putExtra(EXTRA_TEXT, button_pushed);
                this.startActivity(intent_recode);
                return true;

            case R.id.add_inventory:
                button_pushed = "Add Inventory";
                Intent intent_add_inventory = new Intent(this, AddInventory.class);
                intent_add_inventory.putExtra(EXTRA_TEXT, button_pushed);
                this.startActivity(intent_add_inventory);
                return true;

            case R.id.add_container:
                button_pushed = "Add Container";
                Intent intent_add_container = new Intent(this, AddContainer.class);
                intent_add_container.putExtra(EXTRA_TEXT, button_pushed);
                this.startActivity(intent_add_container);
                return true;

            case R.id.audit:
                button_pushed = "Audit";
                Intent intent_audit = new Intent(this, Audit.class);
                intent_audit.putExtra(EXTRA_TEXT, button_pushed);
                this.startActivity(intent_audit);
                return true;

            case R.id.configuration:
                button_pushed = "Configuration";
                Intent intent_configuration = new Intent(this, Configuration.class);
                intent_configuration.putExtra(EXTRA_TEXT, button_pushed);
                this.startActivity(intent_configuration);
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }
}