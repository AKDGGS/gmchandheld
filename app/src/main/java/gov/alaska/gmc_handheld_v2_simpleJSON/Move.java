package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;


public class Move extends BaseActivity {
    private boolean submitted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.move);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Color.parseColor("#ff567b95"));
        setSupportActionBar(toolbar);


        final EditText moveContainerET = findViewById(R.id.moveContainerET);
        final EditText moveDestinationET = findViewById(R.id.destinationET);
        final Button move_button = findViewById(R.id.move_button);

            // KeyListener listens if enter is pressed
            System.out.println("Not empty");
            moveDestinationET.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if( TextUtils.isEmpty(moveContainerET.getText())){

                        moveContainerET.setError( "Container is required!" );

                    }else if (TextUtils.isEmpty(moveDestinationET.getText())) {
                        moveDestinationET.setError("A destination is required!");
                    }else{
                        // if "enter" is pressed
                        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                            move_button.performClick();
                            return true;
                        }
                    }
                    return false;
                }
            });

        // onClickListener listens if the submit button is clicked
        move_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( TextUtils.isEmpty(moveContainerET.getText())){
                    moveContainerET.setError( "A container is required!" );
                }else if (TextUtils.isEmpty(moveDestinationET.getText())) {
                    moveDestinationET.setError( "A destination is required!" );
                }else{
                    moveContainer();
                    Toast.makeText(getApplicationContext(), moveContainerET.getText().toString() + " was moved to " + moveDestinationET.getText().toString(),
                            Toast.LENGTH_LONG).show();

                    moveContainer();
                    moveContainerET.setText("");
                    moveDestinationET.setText("");
                    submitted = false;
                }
            }
        });
    }

    public String getBarcode() {
        EditText barcodeInput = findViewById(R.id.moveContainerET);
        return barcodeInput.getText().toString();
    }

    public void moveContainer()  {
        System.out.println(getBarcode());
        OpenLookup openLookup = new OpenLookup();
        openLookup.processDataForDisplay(getBarcode(), this);

    }

}