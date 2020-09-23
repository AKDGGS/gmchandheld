package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.widget.Toolbar;

public class MainActivity extends BaseActivity {

    private static String button_pushed;

    public static String getButton_pushed() {
        return button_pushed;
    }

    public static void setButton_pushed(String button_pushed) {
        MainActivity.button_pushed = button_pushed;
    }

    public static final String EXTRA_TEXT = "com.example.user_input_no_button.EXTRA_TEXT";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


////         test for accessing lookupHistory from shared preferences.
//        SharedPreferences sp = getApplicationContext().getSharedPreferences("LookupHistorySP", Context.MODE_PRIVATE);
//        String s2 =  sp.getString("lookupHistoryString", "");
//        System.out.println("TEST " + s2);


        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Color.parseColor("#ff567b95"));
        setSupportActionBar(toolbar);
    }

    public void menu_option(View View) {
        String button_text;
        button_text = ((Button) View).getText().toString();

        switch (button_text) {
            case "Help":
                Intent help = new Intent(this, Help.class);
                startActivity(help);
                break;
            case "Lookup":
                button_pushed = "Lookup";
                open_get_barcode();
                break;
            case "Summary":
                button_pushed = "Summary";
                open_get_barcode();
                break;
            case "Move":
                button_pushed = "Move";
                open_move();
                break;
            case "Recode":
                Intent recode = new Intent(this, Recode.class);
                startActivity(recode);
                break;
            case "Add Inventory":
                Intent add_inventory = new Intent(this, AddInventory.class);
                startActivity(add_inventory);
                break;
            case "Add Container":
                Intent add_container = new Intent(this, AddContainer.class);
                startActivity(add_container);
                break;
            case "Audit":
                Intent audit = new Intent(this, Audit.class);
                startActivity(audit);
                break;
            case "Configuration":
                Intent configuration = new Intent(this, Configuration.class);
                startActivity(configuration);
                break;
        }
    }

    private void open_get_barcode() {

        LookupLogicForDisplay lookupLogicForDisplayObj;
        lookupLogicForDisplayObj = Bridge.instance().lookupLogicForDisplayObj;


        if(lookupLogicForDisplayObj == null) {
            Intent get_barcode = new Intent(this, Lookup.class);
            get_barcode.putExtra(EXTRA_TEXT, button_pushed);
            startActivity(get_barcode);
        }else{
            Intent lookup = new Intent(this, LookupDisplay.class);
            startActivity(lookup);
        }
    }

    private void open_move() {

            Intent get_barcode = new Intent(this, Move.class);
            get_barcode.putExtra(EXTRA_TEXT, button_pushed);
            startActivity(get_barcode);
    }

    @Override
    public void onBackPressed() {
        Bridge.instance().lookupLogicForDisplayObj = null;
        super.onBackPressed();
    }
}
