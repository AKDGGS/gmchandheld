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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AddInventory extends BaseActivity implements IssuesFragment.onMultiChoiceListener {
    private IntentIntegrator qrScan;
    private EditText barcodeET;
    private TextView showIssuesTV;
    private static ArrayList<String> selectedItems;
    private static boolean[] checkedItems;
    private static ArrayList<String> selectedItemsDisplayList;
    private String data;

    public AddInventory() {
        selectedItems = new ArrayList<>();
        selectedItemsDisplayList = new ArrayList<>();
        checkedItems = new boolean[10];
        selectedItems.add("needs_inventory");
        selectedItemsDisplayList.add("Needs Inventory");
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
        if (!RemoteApiUIHandler.isDownloading()) {
            // onClickListener listens if the submit button is clicked
            submit_button.setOnClickListener(v -> {
                    if (!(TextUtils.isEmpty(barcodeET.getText()))) {
                        String container = barcodeET.getText().toString();
                        if (!container.isEmpty()) {
                            String barcode = null;
                            String remark = null;
                            try {
                                barcode = URLEncoder.encode(barcodeET.getText().toString(), "utf-8");
                                if (remarkET.getText() != null) {
                                    remark = URLEncoder.encode(remarkET.getText().toString(), "utf-8");
                                }
                            } catch (UnsupportedEncodingException e) {
//                                exception = new Exception(e.getMessage());
                            }
                            StringBuilder sb = new StringBuilder();
                            if (barcode != null) {
                                sb.append("barcode=").append(barcode);
                            }
                            if (remark != null) {
                                sb.append("&remark=").append(remark);
                            }
                            if (selectedItems != null) {
                                sb.append(containersToUrlList(selectedItems, "i"));
                            }
                            String url = baseURL + "addinventory.json?" + sb.toString();
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    if (thread.isInterrupted()) {
                                        return;
                                    }
                                    final ExecutorService service =
                                            Executors.newFixedThreadPool(1);
                                    final Future<String> task =
                                            service.submit(new NewRemoteAPIDownload(url));
                                    try {
                                        data = task.get();
                                    } catch (ExecutionException e) {
                                        e.printStackTrace();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                        return;
                                    }

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (null == data){
                                                Toast.makeText(AddInventory.this,
                                                        "There was a problem.  " +
                                                                "The inventory was not added.",
                                                        Toast.LENGTH_SHORT).show();
                                                barcodeET.requestFocus();
                                            } else if (data.contains("success")){
                                                Toast.makeText(AddInventory.this,
                                                        "The inventory was added.",
                                                        Toast.LENGTH_SHORT).show();
                                                barcodeET.requestFocus();
                                            }
                                        }
                                    });
                                }
                            };
                            thread = new Thread(runnable);
                            thread.start();
                        }
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
                if (data.getParcelableExtra("barcode") != null) {
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
}
