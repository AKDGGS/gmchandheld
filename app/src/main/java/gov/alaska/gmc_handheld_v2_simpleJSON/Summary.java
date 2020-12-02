package gov.alaska.gmc_handheld_v2_simpleJSON;

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

public class Summary extends BaseActivity {

    private ListView listView;
    private final LinkedList<String> summaryHistory = SummaryHistoryHolder.getInstance().getSummaryHistory();

    @Override
    public void onRestart() {
        super.onRestart();
        EditText barcodeInput = findViewById(R.id.getBarcodeEditText);
        barcodeInput.selectAll();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.summary_get_barcode);
        SummaryDisplayObjInstance.instance().summaryLogicForDisplayObj = null;

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setBackgroundColor(Color.parseColor("#ff567b95"));

        final EditText barcodeInput = findViewById(R.id.getBarcodeEditText);
        final Button submitButton = findViewById(R.id.submitButton);
        final RemoteApiUIHandler remoteApiUIHandler = new RemoteApiUIHandler();

        // populates the history list
        listView = findViewById(R.id.listViewGetSummaryHistory);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        adapter.addAll(summaryHistory);
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);

        // Submit barcode query
        if (remoteApiUIHandler.isDownloading()) {

            // onClickListener listens if the submit button is clicked
            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!getBarcode().isEmpty()) {
                        remoteApiUIHandler.setDownloading(true);
                        RemoteApiUIHandler.setQueryOrDestination(getBarcode());
                        remoteApiUIHandler.processDataForDisplay(Summary.this);
                    }
                }
            });

            // KeyListener listens if enter is pressed
            barcodeInput.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    // if "enter" is pressed
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        submitButton.performClick();
                        return true;
                    }
                    return false;
                }
            });

            // Clicking barcode in history list.
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    barcodeInput.setText(listView.getItemAtPosition(position).toString());
                    submitButton.performClick();
                }
            });
        }
    }

    public String getBarcode() {
        EditText barcodeInput = findViewById(R.id.getBarcodeEditText);
        return barcodeInput.getText().toString();
    }

    @Override
    public void onBackPressed() {
        SummaryDisplayObjInstance.instance().summaryLogicForDisplayObj = null;
        super.onBackPressed();
    }
}
