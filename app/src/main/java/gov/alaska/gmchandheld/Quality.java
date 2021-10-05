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
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class Quality extends BaseActivity implements IssuesFragment.onMultiChoiceListener, RemoteAPIDownloadCallback {
    private static ArrayList<String> selectedItems;
    private static boolean[] checkedItems;
    private static ArrayList<String> selectedItemsDisplayList;
    private EditText barcodeET, remarkET;
    private TextView showIssuesTV;
    private String data;

    public Quality() {
        selectedItems = new ArrayList<>();
        selectedItems.add("needs_inventory");
        selectedItemsDisplayList = new ArrayList<>();
        selectedItemsDisplayList.add("Needs Inventory");
        checkedItems = new boolean[sp.getString("issuesString", "needs_inventory")
                .split(",").length];
        checkedItems[0] = true;
    }

    public static ArrayList<String> getSelectedItems() {
        return selectedItems;
    }

    public static boolean[] getCheckedItems() {
        return checkedItems;
    }

    public static ArrayList<String> getSelectedItemsDisplayList() {
        return selectedItemsDisplayList;
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
        barcodeET = findViewById(R.id.barcodeET);
        remarkET = findViewById(R.id.remarkET);
        showIssuesTV = findViewById(R.id.showIssuesTV);
        if (!selectedItemsDisplayList.isEmpty()) {
            showIssuesTV.setText(listToString(selectedItemsDisplayList));
        }
        Button cameraBtn = findViewById(R.id.cameraBtn);
        if (!sp.getBoolean("cameraOn", false)) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.weight = 7.75f;
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
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                remarkET.requestFocus();
                return true;
            }
            return false;
        });

        // onClickListener listens if the submit button is clicked
        findViewById(R.id.submitBtn).setOnClickListener(v -> {
            if (!(TextUtils.isEmpty(barcodeET.getText()))) {
                if (!barcodeET.getText().toString().isEmpty()) {
                    String barcode = null;
                    String remark = null;
                    try {
                        barcode = URLEncoder.encode(barcodeET.getText().toString(), "utf-8");
                        if (remarkET.getText() != null) {
                            System.out.println("Remark before encoding: " + remarkET.getText());
                            remark = URLEncoder.encode(remarkET.getText().toString(), "utf-8");
                        }
                    } catch (UnsupportedEncodingException e) {
                        //                            exception = new Exception(e.getMessage());
                    }
                    StringBuilder sb = new StringBuilder();
                    if (barcode != null && barcode.length() > 0) {
                        sb.append("barcode=").append(barcode);
                    }
                    if (remark != null && remark.length() > 0) {
                        System.out.println("Remark: " + remark.length());
                        sb.append("&remark=").append(remark);
                    }
                    if (selectedItems != null) {
                        sb.append(containersToUrlList(selectedItems, "i"));
                    }

                    System.out.println(sb.toString());

                    try {
                        remoteAPIDownload.setFetchDataObj(baseURL + "addinventoryquality.json?" + sb.toString(),
                                BaseActivity.apiKeyBase,
                                null,
                                this);
                    } catch (Exception e) {
                        System.out.println("Exception: " + e.getMessage());
                    }
                }
            }
        });

        showIssuesTV = findViewById(R.id.showIssuesTV);
        Button issuesBtn = findViewById(R.id.issuesBtn);
        issuesBtn.setOnClickListener(view -> {
            if (selectedItemsDisplayList != null) {
                showIssuesTV.setText(listToString(selectedItemsDisplayList));
            } else {
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
            if (resultCode == CommonStatusCodes.SUCCESS && data != null) {
                Barcode barcode = data.getParcelableExtra("barcode");
                if (barcode != null) {
                    barcodeET.setText(barcode.displayValue);
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

    public String listToString(ArrayList<String> arrList) {
        StringBuilder sb = new StringBuilder();
        //used to display the list in the app
        sb.setLength(0); //clears the display list so unchecked items are removed
        for (String s : arrList) {
            sb.append(s).append("\n");
        }
        return sb.toString();
    }

    public String containersToUrlList(ArrayList<String> list, String paramKeyword) {
        String delim = "&" + paramKeyword + "=";
        StringBuilder sb = new StringBuilder();
        if (list != null && list.size() > 0) {
            sb.append(delim);
            int i = 0;
            while (i < list.size() - 1) {
                try {
                    sb.append(URLEncoder.encode(list.get(i), "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                sb.append(delim);
                i++;
            }
            sb.append(list.get(i));
        }
        return sb.toString();
    }

    @Override
    public void displayData(String data, int responseCode, String responseMessage) {
        runOnUiThread(() -> {
            if (null == data || data.length() <= 2) {
                Toast.makeText(Quality.this,
                        "There was a problem. The inventory was not updated.",
                        Toast.LENGTH_SHORT).show();
                barcodeET.requestFocus();
            } else if (data.contains("success")) {
                Toast.makeText(Quality.this, "The inventory was updated.",
                        Toast.LENGTH_SHORT).show();
                barcodeET.requestFocus();
                barcodeET.setText("");
                remarkET.setText("");
                barcodeET.requestFocus();
                showIssuesTV.setText("");
                selectedItems.clear();
                selectedItemsDisplayList.clear();
                checkedItems = new boolean[10];
                checkedItems[0] = true;
                selectedItems.add("needs_inventory");
                selectedItemsDisplayList.add("Needs Inventory");
                showIssuesTV.setText(listToString(selectedItemsDisplayList));
            }
        });
    }

    @Override
    public void displayException(Exception e) {
        if (e.getMessage() != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (e.getMessage().isEmpty()){
                        Toast.makeText(getApplicationContext(), "There was an error.",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}