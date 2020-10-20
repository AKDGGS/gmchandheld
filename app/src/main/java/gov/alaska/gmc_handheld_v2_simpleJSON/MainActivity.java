package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.widget.Toolbar;

import java.util.LinkedList;


public class MainActivity extends BaseActivity {

    private ListView listView;
    private LinkedList<String> lookupHistory = LookupHistoryHolder.getInstance().getLookupHistory();
    private boolean submitted = false;
    public static final String SHARED_PREFS = "sharedPrefs";

    @Override
    public void onRestart() {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LookupDisplayObjInstance.instance().lookupLogicForDisplayObj = null;

////         test for accessing lookupHistory from shared preferences.
//        SharedPreferences sp = getApplicationContext().getSharedPreferences("LookupHistorySP", Context.MODE_PRIVATE);
//        String s2 =  sp.getString("lookupHistoryString", "");
//        System.out.println("TEST " + s2);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setBackgroundColor(Color.parseColor("#ff567b95"));

        final EditText barcodeInput = findViewById(R.id.editText1);


        final Button submit_button = findViewById(R.id.submit_button);

        // KeyListener listens if enter is pressed
        barcodeInput.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // if "enter" is pressed

                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    submit_button.performClick();
                    return true;
                }
                return false;
            }
        });


        final OpenLookup openLookupObj = new OpenLookup();

        // onClickListener listens if the submit button is clicked
        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLookupObj.processDataForDisplay(getBarcode(), MainActivity.this);
                submitted = false;
            }
        });

        // populates the history list
        listView = findViewById(R.id.listViewGetBarcodeHistory);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        adapter.addAll(lookupHistory);
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (submitted == false) {
                    System.out.println(position);
                    barcodeInput.setText(listView.getItemAtPosition(position).toString());
                    submit_button.performClick();
                    submitted = true;

                }
            }
        });

        resetLookupSummaryButtons(this);
    }

    public String getBarcode() {
        EditText barcodeInput = findViewById(R.id.editText1);
        return barcodeInput.getText().toString();
    }

    @Override
    public void onBackPressed() {
        LookupDisplayObjInstance.instance().lookupLogicForDisplayObj = null;
        super.onBackPressed();
    }

    private void resetLookupSummaryButtons(Context context) {
        switch (context.getClass().getSimpleName()) {
            case "Summary":
            case "MainActivity": {
                final Button submit_button = ((Activity) context).findViewById(R.id.submit_button);
                submit_button.setEnabled(true);
                submit_button.setClickable(true);
                submit_button.setFocusableInTouchMode(true);

                final EditText barcodeInput = ((Activity) context).findViewById(R.id.editText1);
                barcodeInput.requestFocus();
                barcodeInput.getText().clear();
                barcodeInput.setFocusable(true);
                barcodeInput.setEnabled(true);
                barcodeInput.setFocusableInTouchMode(true);
                break;
            }
            case "SummaryDisplay":
            case "LookupDisplay": {
                final EditText barcodeInput = ((Activity) context).findViewById(R.id.invisibleEditText);
                barcodeInput.setFocusable(true);
                barcodeInput.setEnabled(true);
                barcodeInput.setFocusableInTouchMode(true);
                barcodeInput.requestFocus();
                barcodeInput.getText().clear();
                break;
            }
        }
    }
}
