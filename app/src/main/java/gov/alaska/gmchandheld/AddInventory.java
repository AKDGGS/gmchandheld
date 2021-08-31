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
    private EditText barcodeET;
    private TextView showIssuesTV;
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
        checkAPIkeyExists(this);
        barcodeET = findViewById(R.id.barcodeET);
        EditText remarkET = findViewById(R.id.remarkET);
        Button submit_button = findViewById(R.id.submitBtn);
        showIssuesTV = findViewById(R.id.showIssuesTV);
        if (!selectedItemsDisplayList.isEmpty()) {
            showIssuesTV.setText(listToString(selectedItemsDisplayList));
        }
        final RemoteApiUIHandler remoteApiUIHandler = new RemoteApiUIHandler();
        Button cameraBtn = findViewById(R.id.cameraBtn);
        if (!sp.getBoolean("cameraOn", false)) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.weight = 7.75f;
            params.rightMargin = 15;
            barcodeET.setLayoutParams(params);
            remarkET.setLayoutParams(params);
            cameraBtn.setVisibility(View.GONE);
        } else {
            qrScan = new IntentIntegrator(this);
            qrScan.setBeepEnabled(true);
        }
        cameraBtn.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT <= 24) {
                qrScan.initiateScan();
            } else {
                Intent intent = new Intent(this, CameraToScanner.class);
                startActivityForResult(intent, 0);
            }
        });
        // KeyListener listens if enter is pressed
        barcodeET.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                remarkET.requestFocus();
                return true;
            }
            return false;
        });
        if (remoteApiUIHandler.isDownloading()) {
            // onClickListener listens if the submit button is clicked
            submit_button.setOnClickListener(v -> {
                    if (!(TextUtils.isEmpty(barcodeET.getText()))) {
                        String container = barcodeET.getText().toString();
                        if (!container.isEmpty()) {
                            RemoteApiUIHandler.setUrlFirstParameter(barcodeET.getText().toString());
                            RemoteApiUIHandler.setAddContainerRemark(remarkET.getText().toString());
                            RemoteApiUIHandler.setContainerList(selectedItems);
                            RemoteApiUIHandler.setDownloading(true);
                            new RemoteApiUIHandler.ProcessDataForDisplay(this).execute();
                        }
                        barcodeET.setText("");
                        remarkET.setText("");
                        barcodeET.requestFocus();
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
            } else if (selectedItemsDisplayList.isEmpty()) {
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
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            barcodeET.setText(result.getContents());
        } else {
            if (resultCode == CommonStatusCodes.SUCCESS && null != data) {
                if(null != data.getParcelableExtra("barcode")) {
                    Barcode barcode = data.getParcelableExtra("barcode");
                    barcodeET.setText(barcode != null ? barcode.displayValue : null);
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void onPostitiveButtonClicked(String[] list, ArrayList<String> selectedItems) {
        showIssuesTV.setText(listToString(selectedItemsDisplayList));
    }

    @Override
    public void onNegativebuttonClicked() {
    }

    public String listToString(ArrayList<String> arrList){
        //used to display the list in the app
        BaseActivity.sb.setLength(0); //clears the display list so unchecked items are removed
        for (String s : arrList) {
            BaseActivity.sb.append(s).append("\n");
        }
        return BaseActivity.sb.toString();
    }
}
