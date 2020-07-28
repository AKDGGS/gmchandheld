package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.widget.Toolbar;

public class GetBarcode extends BaseActivity {

    EditText barcodeInput;

    public static final String EXTRA_TEXT = "com.example.user_input_no_button.EXTRA_TEXT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_barcode);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setBackgroundColor(Color.parseColor("#ff567b95"));

        addKeyListener();
    }

    private void addKeyListener() {
        barcodeInput = findViewById(R.id.editText1);

        // add a keylistener to keep track user input
        barcodeInput.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                // if keydown and "enter" is pressed
                if ((event.getAction() == KeyEvent.ACTION_DOWN)
                        && (keyCode == KeyEvent.KEYCODE_ENTER)) {

                    Intent intent = getIntent();
                    String buttonPushed = intent.getStringExtra(MainActivity.EXTRA_TEXT);

                    switch(buttonPushed){
                        case "Help":
//                            openLookup();
                            break;
                        case "Summary":
                        case "Lookup":
                            openLookup();
                            break;
                        case "Move":
                            openMove();
                            break;
                        default:
                            System.out.println("Error");
                    }
//                    openLookup();
                    return true;

                }

                return false;
            }
        });

    }


    private void openSummary() {
        EditText editText1 = findViewById(R.id.editText1);
        String text = editText1.getText().toString();

        Intent intent = new Intent(this, Summary.class);
        intent.putExtra(EXTRA_TEXT, text);


        startActivity(intent);
    }

    private void openLookup() {

        EditText editText1 = findViewById(R.id.editText1);
        String text = editText1.getText().toString();

        Intent intent = new Intent(this, Lookup.class);
        intent.putExtra(EXTRA_TEXT, text);


        startActivity(intent);
    }

    private void openMove() {

        EditText editText1 = findViewById(R.id.editText1);
        String text = editText1.getText().toString();

        Intent intent = new Intent(this, Move.class);
        intent.putExtra(EXTRA_TEXT, text);


        startActivity(intent);
    }
}