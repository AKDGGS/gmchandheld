package gov.alaska.gmchandheld;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;

public class AddInventory extends BaseActivity implements IssuesFragment.onMultiChoiceListener {

    private IntentIntegrator qrScan;
    private EditText addinventoryBarcodeET;
    private Button issuesBtn;
    private TextView showIssuesTV;
    private ArrayList<String> issuesList;

    public static final ArrayList<String> selectedItems = new ArrayList<>();
    public static final boolean[] checkedItems = new boolean[9];

    public AddInventory() {
        selectedItems.add("needs_inventory");
        checkedItems[0] = true;
    }

    @Override
    public void onRestart() {
        super.onRestart();
        this.recreate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_inventory);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Color.parseColor("#ff567b95"));
        setSupportActionBar(toolbar);

        addinventoryBarcodeET = findViewById(R.id.barcodeET);
        final EditText addInveotryRemarkET = findViewById(R.id.remarkET);
        final Button submit_button = findViewById(R.id.submitBtn);

        final RemoteApiUIHandler remoteApiUIHandler = new RemoteApiUIHandler();

        SharedPreferences sp = getSharedPreferences(Configuration.SHARED_PREFS, MODE_PRIVATE);
        boolean cameraOn = (sp.getBoolean("cameraOn", false));

        Button cameraBtn = findViewById(R.id.cameraBtn);
        if(!cameraOn){
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.weight = 7.75f;
            params.rightMargin = 15;

            addinventoryBarcodeET.setLayoutParams(params);
            addInveotryRemarkET.setLayoutParams(params);
            cameraBtn.setVisibility(View.GONE);
        }else{
            qrScan = new IntentIntegrator(this);
            qrScan.setBeepEnabled(true);
        }

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT <= 24) {
                    qrScan.initiateScan();
                } else {
                    Intent intent = new Intent(AddInventory.this, CameraToScanner.class);
                    startActivityForResult(intent, 0);
                }
            }
        });

        // KeyListener listens if enter is pressed
        addinventoryBarcodeET.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // if "enter" is pressed
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    addInveotryRemarkET.requestFocus();
                    return true;
                }
                return false;
            }
        });


        if (remoteApiUIHandler.isDownloading()) {
            // onClickListener listens if the submit button is clicked
            submit_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckConfiguration checkConfiguration = new CheckConfiguration();
                    if (checkConfiguration.checkConfiguration(AddInventory.this)) {
                        if (!(TextUtils.isEmpty(addinventoryBarcodeET.getText()))) {

                            String container = addinventoryBarcodeET.getText().toString();
                            if (!container.isEmpty()) {
                                RemoteApiUIHandler remoteApiUIHandler = new RemoteApiUIHandler();
                                RemoteApiUIHandler.setUrlFirstParameter(addinventoryBarcodeET.getText().toString());
                                RemoteApiUIHandler.setAddContainerRemark(addInveotryRemarkET.getText().toString());
                                RemoteApiUIHandler.setContainerList(selectedItems);

                                remoteApiUIHandler.setDownloading(true);
                                new RemoteApiUIHandler.ProcessDataForDisplay(AddInventory.this).execute();
                            }
                            addinventoryBarcodeET.setText("");
                            addInveotryRemarkET.setText("");
                            addinventoryBarcodeET.requestFocus();
                        }
                    }
                }
            });
        }

        showIssuesTV = findViewById(R.id.showIssuesTV);
        issuesBtn = findViewById(R.id.issuesBtn);

        issuesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DialogFragment issueDialog = new IssuesFragment();
                issueDialog.setCancelable(false);
                issueDialog.show(getSupportFragmentManager(), "Issues Dialog");

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (Build.VERSION.SDK_INT <= 24) {
            addinventoryBarcodeET = findViewById(R.id.barcodeET);
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            addinventoryBarcodeET.setText(result.getContents());
        }else {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                Barcode barcode = data.getParcelableExtra("barcode");
                EditText edit_text = findViewById(R.id.barcodeET);
                assert barcode != null;
                edit_text.setText(barcode.displayValue);
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void onPostitiveButtonClicked(String[] list, ArrayList<String> selectedItems) {
        StringBuilder sb = new StringBuilder();

        for(String str:selectedItems){
            sb.append(str + ", ");
        }

        showIssuesTV.setText(selectedItems.toString());
    }

    @Override
    public void onNegativebuttonClicked() {

//        showIssuesTV.setText("Needs Inventory");
    }
}
