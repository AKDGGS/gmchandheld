package gov.alaska.gmchandheld;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import java.util.ArrayList;

public class Quality extends BaseActivity implements IssuesFragment.onMultiChoiceListener {
    private EditText addInventoryBarcodeET;
    private TextView showIssuesTV;
    private static int numberOfIssues;
    public static ArrayList<String> selectedItems;
    public static boolean[] checkedItems;
    public static ArrayList<String> selectedItemsDisplayList;
    private StringBuilder sb;

    public Quality() {
        numberOfIssues = 10;
        selectedItems = new ArrayList<>();
        selectedItemsDisplayList = new ArrayList<>();
        checkedItems = new boolean[numberOfIssues];
        selectedItems.add("needs_inventory");
        selectedItemsDisplayList.add("Needs Inventory");
        checkedItems[0] = true;
        sb = new StringBuilder();
    }
    @Override
    public int getLayoutResource() {
        return R.layout.quality;
    }

    @Override
    public void onRestart() {
        super.onRestart();
        this.recreate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkAPIkeyExists(this);
        addInventoryBarcodeET = findViewById(R.id.barcodeET);
        final EditText addInventoryRemarkET = findViewById(R.id.remarkET);
        final Button submit_button = findViewById(R.id.submitBtn);

        showIssuesTV = findViewById(R.id.showIssuesTV);
        if(!selectedItemsDisplayList.isEmpty()){
            showIssuesTV.setText(listToString(selectedItemsDisplayList));
        }
        Button cameraBtn = findViewById(R.id.cameraBtn);
        if (!sp.getBoolean("cameraOn", false)){
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.weight = 7.75f;
            addInventoryBarcodeET.setLayoutParams(params);
            addInventoryRemarkET.setLayoutParams(params);
            cameraBtn.setVisibility(View.GONE);
        }else{
            qrScan = new IntentIntegrator(this);
            qrScan.setBeepEnabled(true);
        }

        cameraBtn.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT <= 24) {
                qrScan.initiateScan();
            } else {
                Intent intent = new Intent(Quality.this, CameraToScanner.class);
                startActivityForResult(intent, 0);
            }
        });

        // KeyListener listens if enter is pressed
        addInventoryBarcodeET.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                addInventoryRemarkET.requestFocus();
                return true;
            }
            return false;
        });
        final RemoteApiUIHandler remoteApiUIHandler = new RemoteApiUIHandler();
        if (remoteApiUIHandler.isDownloading()) {
            // onClickListener listens if the submit button is clicked
            submit_button.setOnClickListener(v -> {
                    if (!(TextUtils.isEmpty(addInventoryBarcodeET.getText()))) {
                        if (!addInventoryBarcodeET.getText().toString().isEmpty()) {
                            RemoteApiUIHandler.setUrlFirstParameter(addInventoryBarcodeET.getText().toString());
                            RemoteApiUIHandler.setAddContainerRemark(addInventoryRemarkET.getText().toString());
                            RemoteApiUIHandler.setContainerList(selectedItems);
                            RemoteApiUIHandler.setDownloading(true);
                            new RemoteApiUIHandler.ProcessDataForDisplay(Quality.this).execute();
                        }
                        addInventoryBarcodeET.setText("");
                        addInventoryRemarkET.setText("");
                        addInventoryBarcodeET.requestFocus();
                        showIssuesTV.setText("");
                        selectedItems.clear();
                        selectedItemsDisplayList.clear();
                        checkedItems = new boolean[numberOfIssues];
                        checkedItems[0] = true;
                        selectedItems.add("needs_inventory");
                        selectedItemsDisplayList.add("Needs Inventory");
                        showIssuesTV.setText(listToString(selectedItemsDisplayList));
                    }
                });
        }
        showIssuesTV = findViewById(R.id.showIssuesTV);
        Button issuesBtn = findViewById(R.id.issuesBtn);
        issuesBtn.setOnClickListener(view -> {
            if (null != selectedItemsDisplayList) {
                showIssuesTV.setText(listToString(selectedItemsDisplayList));
            }else if (selectedItemsDisplayList.isEmpty()){
                showIssuesTV.setText(R.string.needs_inventory);
            }
            DialogFragment issueDialog = new IssuesFragment();
            issueDialog.setCancelable(false);
            issueDialog.show(getSupportFragmentManager(), "Issues Dialog");
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (Build.VERSION.SDK_INT <= 24) {
            addInventoryBarcodeET = findViewById(R.id.barcodeET);
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            addInventoryBarcodeET.setText(result.getContents());
        }else {
            if (resultCode == CommonStatusCodes.SUCCESS && null != data) {
                Barcode barcode = data.getParcelableExtra("barcode");
                if(null != barcode) {
                    EditText edit_text = findViewById(R.id.barcodeET);
                    edit_text.setText(barcode.displayValue);
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void onPostitiveButtonClicked(String[] list, ArrayList<String> selectedItems) {
        sb.setLength(0);
        for(String str:selectedItems){
            sb.append(str).append(", ");
        }
        showIssuesTV.setText(listToString(selectedItemsDisplayList));
    }

    @Override
    public void onNegativebuttonClicked() { }

    public String listToString(ArrayList<String> arrList){
        sb.setLength(0);
        for (String s : arrList) {
            sb.append(s).append("\n");
        }
        return sb.toString();
    }
}