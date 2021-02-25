package gov.alaska.gmchandheld;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.LinkedList;

public class Summary extends BaseActivity {

    private ListView listView;
    private final LinkedList<String> summaryHistory = SummaryHistoryHolder.getInstance().getSummaryHistory();
    public static final String SHARED_PREFS = "sharedPrefs";
    private EditText barcodeET;
    private ToneGenerator toneGen1;
    private IntentIntegrator qrScan;


    @Override
    public void onRestart() {
        this.recreate();
        super.onRestart();
        EditText barcodeInput = findViewById(R.id.barcodeET);
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

        barcodeET = findViewById(R.id.barcodeET);
        final Button submitButton = findViewById(R.id.submitBtn);
        final RemoteApiUIHandler remoteApiUIHandler = new RemoteApiUIHandler();

        // populates the history list
        listView = findViewById(R.id.listViewSummaryHistory);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        adapter.addAll(summaryHistory);
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);

        SharedPreferences sp = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        Boolean cameraOn = (sp.getBoolean("cameraOn", false));

        Button cameraBtn = findViewById(R.id.cameraBtn);
        if(!cameraOn){
            cameraBtn.setVisibility(View.GONE);
        }else{
            qrScan = new IntentIntegrator(this);
            toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        }



        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT <= 24) {
                    qrScan.initiateScan();
                } else {
                Intent intent = new Intent(Summary.this, CameraToScanner.class);
                startActivityForResult(intent, 0);
                }
            }
        });

//        cameraBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(Summary.this, CameraToScanner.class);
//                startActivityForResult(intent, 0);
//            }
//        });

        // Submit barcode query
        if (remoteApiUIHandler.isDownloading()) {
                // onClickListener listens if the submit button is clicked
                submitButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CheckConfiguration checkConfiguration = new CheckConfiguration();
                        if (checkConfiguration.checkConfiguration(Summary.this)) {
                            if (!getBarcode().isEmpty()) {
                                remoteApiUIHandler.setDownloading(true);
                                RemoteApiUIHandler.setUrlFirstParameter(getBarcode());
                                remoteApiUIHandler.processDataForDisplay(Summary.this);
                            }
                        }
                    }
                });


            // KeyListener listens if enter is pressed
            barcodeET.setOnKeyListener(new View.OnKeyListener() {
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
                    barcodeET.setText(listView.getItemAtPosition(position).toString());
                    submitButton.performClick();
                }
            });
        }
    }

    public String getBarcode() {
        EditText barcodeInput = findViewById(R.id.barcodeET);
        return barcodeInput.getText().toString();
    }

    @Override
    public void onBackPressed() {
        SummaryDisplayObjInstance.instance().summaryLogicForDisplayObj = null;
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (Build.VERSION.SDK_INT <= 24) {
            barcodeET = findViewById(R.id.barcodeET);
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            barcodeET.setText(result.getContents());
        }else {
            if (requestCode == 0) {
                if (resultCode == CommonStatusCodes.SUCCESS) {
                    Barcode barcode = data.getParcelableExtra("barcode");
                    EditText edit_text = findViewById(R.id.barcodeET);
                    edit_text.setText(barcode.displayValue);
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }
}
