package gov.alaska.gmchandheld;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MoveDisplay extends BaseActivity implements HTTPRequestCallback {
    private ArrayList<String> containerList;
    private ArrayAdapter<String> adapter;
    private EditText itemET, destinationET;
    private TextView moveCountTV;
    private int clicks;  //used to count double clicks for deletion
    private ProgressDialog downloadingAlert;

    public MoveDisplay() {
        clicks = 0;
    }

    @Override
    public int getLayoutResource() {
        return R.layout.move_display;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        this.recreate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = findViewById(R.id.toolbar);
        //users requested color to differentiate Move from other activities
        toolbar.setBackgroundColor(Color.parseColor("#e66101")); //Orange
        destinationET = findViewById(R.id.toET);
        itemET = findViewById(R.id.itemET);
        moveCountTV = findViewById(R.id.moveCountTV);
        Button addBtn = findViewById(R.id.addContainerBtn);
        ListView containerListLV = findViewById(R.id.listViewContainersToMove);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        containerListLV.setAdapter(adapter);
        containerList = MoveDisplayObjInstance.getInstance().getMoveList();
        adapter.addAll(containerList);
        moveCountTV.setText(String.valueOf(containerList.size()));
        Button cameraBtn = findViewById(R.id.cameraBtn);
        Button itemCameraBtn = findViewById(R.id.itemCameraBtn);
        if (!sp.getBoolean("cameraOn", false)) {
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.weight = 6.75f;
            LinearLayout.LayoutParams params2 =
                    new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
            params2.weight = 3f;
            destinationET.setLayoutParams(params);
            itemET.setLayoutParams(params);
            moveCountTV.setLayoutParams(params2);
            cameraBtn.setVisibility(View.GONE);
            itemCameraBtn.setVisibility(View.GONE);
        } else {
            qrScan = new IntentIntegrator(this);
        }
        cameraBtn.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT <= 24) {
                intent = qrScan.createScanIntent();
            } else {
                intent = new Intent(MoveDisplay.this, CameraToScanner.class);
            }
            startActivityForResult(intent, 1);
        });

        itemCameraBtn.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT <= 24) {
                intent = qrScan.createScanIntent();
            } else {
                intent = new Intent(MoveDisplay.this, CameraToScanner.class);
            }
            startActivityForResult(intent, 2);

        });
        addBtn.setOnClickListener(v -> {
            String container = itemET.getText().toString();
            if (!containerList.contains(container) && !container.isEmpty() &&
                    !container.equals(destinationET.getText().toString())) {
                containerList.add(0, container);
                adapter.insert(container, 0);
                adapter.notifyDataSetChanged();
                moveCountTV.setText(String.valueOf(containerList.size()));
            }
            itemET.setText("");
            itemET.requestFocus();
        });
        findViewById(R.id.clearAllBtn).setOnClickListener(v -> {
            itemET.setText("");
            itemET.requestFocus();
            containerList.clear();
            adapter.clear();
            adapter.notifyDataSetChanged();
            moveCountTV.setText(String.valueOf(containerList.size()));
        });
        //double click to remove elements
        containerListLV.setOnItemClickListener((adapterView, view, position, l) -> {
            clicks++;
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                if (clicks == 2) {
                    adapter.remove(containerList.get(position));
                    containerList.remove(position);
                    adapter.notifyDataSetChanged();
                    moveCountTV.setText(String.valueOf(containerList.size()));
                }
                clicks = 0;
            }, 500);
        });
        // KeyListener listens if enter is pressed
        itemET.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode ==
                    KeyEvent.KEYCODE_ENTER)) {
                addBtn.performClick();
                itemET.requestFocus();
                return true;
            }
            return false;
        });
        // KeyListener listens if enter is pressed
        destinationET.setOnKeyListener((v, keyCode, event) -> {
            if (!(TextUtils.isEmpty(destinationET.getText()))) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode ==
                        KeyEvent.KEYCODE_ENTER)) {
                    itemET.requestFocus();
                    return true;
                }
            }
            return false;
        });
        // onClickListener listens if the submit button is clicked
        findViewById(R.id.submitBtn).setOnClickListener(v -> {
            if (!(TextUtils.isEmpty(destinationET.getText())) && (containerList.size() > 0)) {
                HashMap<String, Object> params = new HashMap<>();
                params.put("d", destinationET.getText().toString());
                params.put("c", containerList);

                try {
                    downloadingAlert = new ProgressDialog(this);
                    downloadingAlert.setMessage("Moving the inventory.");
                    downloadingAlert.setCancelable(false);
                    downloadingAlert.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            thread.interrupt();
                            downloadingAlert.dismiss();//dismiss dialog
                        }
                    });
                    downloadingAlert.show();
                    getHTTPRequest().setFetchDataObj(baseURL + "move.json?",
                            this,
                            0,
                            params,
                            null);
                } catch (Exception e) {
                    System.out.println("Move Display Exception: " + e.getMessage());
                    Toast.makeText(MoveDisplay.this,
                            "The there is a problem. " + e.getMessage(), Toast.LENGTH_LONG).show();
                    thread.interrupt();
                    if (downloadingAlert != null) {
                        downloadingAlert.dismiss();
                    }
                }
            }
        });
        if (BaseActivity.getUpdatable()) { //Set in UpdateBroadcastReceiver and Configuration
            downloadingAlert();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (Build.VERSION.SDK_INT <= 24) {
            switch (requestCode) {
                case 1: {
                    destinationET = findViewById(R.id.toET);
                    IntentResult result = IntentIntegrator.parseActivityResult(
                            IntentIntegrator.REQUEST_CODE, resultCode, data);
                    destinationET.setText(result.getContents());
                }
                break;
                case 2: {
                    itemET = findViewById(R.id.itemET);
                    IntentResult result = IntentIntegrator.parseActivityResult(
                            IntentIntegrator.REQUEST_CODE, resultCode, data);
                    itemET.setText(result.getContents());
                }
                break;
            }
        } else {
            switch (requestCode) {
                case 1: {
                    if (resultCode == CommonStatusCodes.SUCCESS && data != null) {
                        Barcode barcode = data.getParcelableExtra("barcode");
                        EditText destinationEt = findViewById(R.id.toET);
                        if (barcode != null) {
                            destinationEt.setText(barcode.displayValue);
                        }
                    }
                    break;
                }
                case 2: {
                    if (resultCode == CommonStatusCodes.SUCCESS && data != null) {
                        Barcode barcode = data.getParcelableExtra("barcode");
                        EditText itemEt = findViewById(R.id.itemET);
                        if (barcode != null) {
                            itemEt.setText(barcode.displayValue);
                        }
                    }
                    break;
                }
                default:
                    super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (itemET.hasFocus() & event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void displayData(byte[] byteData, Date date, int responseCode, String responseMessage, int requestType) {
        if (downloadingAlert != null) {
            downloadingAlert.dismiss();
        }
        String data = new String(byteData);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!(responseCode < HttpURLConnection.HTTP_BAD_REQUEST) || data == null) {
                    switch (responseCode) {
                        case 403:
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MoveDisplay.this,
                                            "The token is not correct.", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(MoveDisplay.this, GetToken.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    MoveDisplay.this.startActivity(intent);
                                }
                            });
                        case 404:
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MoveDisplay.this,
                                            "The URL is not correct.", Toast.LENGTH_LONG).show();
                                    BaseActivity.editor.putString("urlText", "").apply();
                                    Intent intent = new Intent(MoveDisplay.this, GetToken.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    MoveDisplay.this.startActivity(intent);
                                }
                            });
                        default:
                            Toast.makeText(MoveDisplay.this, "There was a problem. Nothing was moved.",
                                    Toast.LENGTH_LONG).show();
                            destinationET.requestFocus();
                    }
                } else if (data.contains("success")) {
                    Toast.makeText(MoveDisplay.this, "The contents were moved.",
                            Toast.LENGTH_LONG).show();
                    destinationET.requestFocus();
                    itemET.setText("");
                    destinationET.setText("");
                    containerList.clear();
                    adapter.clear();
                    adapter.notifyDataSetChanged();
                    moveCountTV.setText("");
                }
            }
        });
    }

    @Override
    public void displayException(Exception e) {
        if (downloadingAlert != null) {
            downloadingAlert.dismiss();
        }
        if (e.getMessage() != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    System.out.println("Move Display exception: " + e.getMessage());
                }
            });
        }
    }
}