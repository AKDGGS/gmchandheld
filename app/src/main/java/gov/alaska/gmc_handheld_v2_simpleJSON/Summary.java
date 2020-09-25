package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.widget.Toolbar;

public class Summary extends BaseActivity {

    private boolean submitted = false;

    @Override
    public void onRestart() {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.summary_get_barcode);

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
                openLookupObj.processDataForDisplay(getBarcode(), Summary.this);
                submitted = false;
            }
        });

    }

    public String getBarcode() {
        EditText barcodeInput = findViewById(R.id.editText1);
        return barcodeInput.getText().toString();
    }
}
