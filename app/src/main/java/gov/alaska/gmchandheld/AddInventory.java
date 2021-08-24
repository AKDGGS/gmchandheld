package gov.alaska.gmchandheld;

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
import androidx.annotation.Nullable;
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
    private static int numberOfIssues;
    public static ArrayList<String> selectedItems;
    public static boolean[] checkedItems;
    public static ArrayList<String> selectedItemsDisplayList;

    public AddInventory() {
        numberOfIssues = 10;
        selectedItems = new ArrayList<>();
        selectedItemsDisplayList = new ArrayList<>();
        checkedItems = new boolean[numberOfIssues];
        selectedItems.add("needs_inventory");
        selectedItemsDisplayList.add("Needs Inventory");
        checkedItems[0] = true;
    }

    @Override
    public int getLayoutResource() {
        return R.layout.add_inventory;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        this.recreate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addinventoryBarcodeET = findViewById(R.id.barcodeET);
        EditText addInventoryRemarkET = findViewById(R.id.remarkET);
        Button submit_button = findViewById(R.id.submitBtn);
        showIssuesTV = findViewById(R.id.showIssuesTV);
        if (!selectedItemsDisplayList.isEmpty()) {
            showIssuesTV.setText(listToString(selectedItemsDisplayList));
        }
        final RemoteApiUIHandler remoteApiUIHandler = new RemoteApiUIHandler();
        boolean cameraOn = (sp.getBoolean("cameraOn", false));
        Button cameraBtn = findViewById(R.id.cameraBtn);
        if (!cameraOn) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.weight = 7.75f;
            params.rightMargin = 15;
            addinventoryBarcodeET.setLayoutParams(params);
            addInventoryRemarkET.setLayoutParams(params);
            cameraBtn.setVisibility(View.GONE);
        } else {
            qrScan = new IntentIntegrator(this);
            qrScan.setBeepEnabled(true);
        }
        cameraBtn.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT <= 24) {
                qrScan.initiateScan();
            } else {
                Intent intent = new Intent(AddInventory.this, CameraToScanner.class);
                startActivityForResult(intent, 0);
            }
        });
        // KeyListener listens if enter is pressed
        addinventoryBarcodeET.setOnKeyListener((v, keyCode, event) -> {
            // if "enter" is pressed
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                addInventoryRemarkET.requestFocus();
                return true;
            }
            return false;
        });
        if (remoteApiUIHandler.isDownloading()) {
            // onClickListener listens if the submit button is clicked
            submit_button.setOnClickListener(v -> {
                CheckConfiguration checkConfiguration = new CheckConfiguration();
                if (checkConfiguration.checkConfiguration(AddInventory.this)) {
                    if (!(TextUtils.isEmpty(addinventoryBarcodeET.getText()))) {
                        String container = addinventoryBarcodeET.getText().toString();
                        if (!container.isEmpty()) {
                            RemoteApiUIHandler remoteApiUIHandler1 = new RemoteApiUIHandler();
                            RemoteApiUIHandler.setUrlFirstParameter(
                                    addinventoryBarcodeET.getText().toString());
                            RemoteApiUIHandler.setAddContainerRemark(
                                    addInventoryRemarkET.getText().toString());
                            RemoteApiUIHandler.setContainerList(selectedItems);
                            remoteApiUIHandler1.setDownloading(true);
                            new RemoteApiUIHandler.ProcessDataForDisplay(
                                    AddInventory.this).execute();
                        }
                        addinventoryBarcodeET.setText("");
                        addInventoryRemarkET.setText("");
                        addinventoryBarcodeET.requestFocus();
                        showIssuesTV.setText("");
                        selectedItems.clear();
                        selectedItemsDisplayList.clear();
                        checkedItems = new boolean[numberOfIssues];
                        checkedItems[0] = true;
                        selectedItems.add("needs_inventory");
                        selectedItemsDisplayList.add("Needs Inventory");
                        showIssuesTV.setText(listToString(selectedItemsDisplayList));
                    }
                }
            });
        }
        showIssuesTV = findViewById(R.id.showIssuesTV);
        issuesBtn = findViewById(R.id.issuesBtn);
        issuesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != selectedItemsDisplayList) {
                    showIssuesTV.setText(listToString(selectedItemsDisplayList));
                } else if (selectedItemsDisplayList.isEmpty()) {
                    showIssuesTV.setText("Needs Inventory");
                }
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
        } else {
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
        for (String str : selectedItems) {
            sb.append(str + ", ");
        }
        showIssuesTV.setText(listToString(selectedItemsDisplayList));
    }

    @Override
    public void onNegativebuttonClicked() {
    }

    public String listToString(ArrayList<String> arrList) {
        StringBuilder sb = new StringBuilder();
        for (String s : arrList) {
            sb.append(s + "\n");
        }
        return sb.toString();
    }
}
