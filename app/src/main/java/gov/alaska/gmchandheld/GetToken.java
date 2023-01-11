package gov.alaska.gmchandheld;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.security.ProviderInstaller;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

public class GetToken extends AppCompatActivity {
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private EditText apiTokenET, urlET;
    private IntentIntegrator qrScan;
    private Button submitBtn, urlCameraBtn;
    private StringBuilder sb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_token);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("GMC Handheld");
        enableTSL(this);
        sb = new StringBuilder();
        urlET = findViewById(R.id.urlET);
        apiTokenET = findViewById(R.id.apiTokenET);
        TextView urlTV = findViewById(R.id.urlTV);
        urlCameraBtn = findViewById(R.id.urlCameraBtn);
        sp = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        if (sp.getString("urlText", "").isEmpty()) {
            loadGetURL();
        } else {
            urlTV.setVisibility(View.INVISIBLE);
            urlET.setVisibility(View.INVISIBLE);
            urlCameraBtn.setVisibility(View.INVISIBLE);
            apiTokenET.requestFocus();
        }
        loadGetToken();
    }

    public void loadGetURL() {
        urlET.requestFocus();
        if (!BaseActivity.sp.getBoolean("cameraOn", false)) {
            urlCameraBtn.setVisibility(View.GONE);
        } else {
            qrScan = new IntentIntegrator(this);
            qrScan.setBeepEnabled(true);
        }
        urlCameraBtn.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT <= 24) {
                qrScan.initiateScan();
            } else {
                Intent intent = new Intent(GetToken.this, CameraToScanner.class);
                startActivityForResult(intent, 0);
            }
        });
        urlET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    editor = sp.edit();
                    editor.putString("urlText", urlET.getText().toString()).apply();
                    Intent intent = new Intent(GetToken.this, UpdateBroadcastReceiver.class);
                    sendBroadcast(intent);
                }
            }
        });

    }

    public void loadGetToken() {
        apiTokenET = findViewById(R.id.apiTokenET);
        Button cameraBtn = findViewById(R.id.cameraBtn);
        if (!BaseActivity.sp.getBoolean("cameraOn", false)) {
            cameraBtn.setVisibility(View.GONE);
        } else {
            qrScan = new IntentIntegrator(this);
            qrScan.setBeepEnabled(true);
        }
        cameraBtn.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT <= 24) {
                qrScan.initiateScan();
            } else {
                Intent intent = new Intent(GetToken.this, CameraToScanner.class);
                startActivityForResult(intent, 0);
            }
        });
        submitBtn = findViewById(R.id.submitBtn);
        submitBtn.setOnClickListener(v -> {
            if (!apiTokenET.getText().toString().isEmpty()) {
                BaseActivity.setToken(apiTokenET.getText().toString());
                finish();
            }
        });
        apiTokenET.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                submitBtn.performClick();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        urlET = findViewById(R.id.urlET);
        apiTokenET = findViewById(R.id.apiTokenET);
        if (Build.VERSION.SDK_INT <= 24) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (urlET.hasFocus()){
                urlET.setText(result.getContents());
            }
            if ((apiTokenET.hasFocus()) && (!apiTokenET.getText().toString().isEmpty())){
                apiTokenET.setText(result.getContents());
                BaseActivity.setToken(apiTokenET.getText().toString());
                Intent intent = new Intent(GetToken.this, Lookup.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                GetToken.this.startActivity(intent);
            }
        } else {
            if (resultCode == CommonStatusCodes.SUCCESS && data != null) {
                Barcode barcode = data.getParcelableExtra("barcode");
                if (barcode != null) {
                    if (urlET.hasFocus()){
                        urlET.setText(barcode.displayValue);
                    }
                    if (apiTokenET.hasFocus()){
                        apiTokenET.setText(barcode.displayValue);
                    }
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    public void enableTSL(Context mContext) {
        try {
            // enables TSL-1.2 if Google Play is updated on old devices.
            // doesn't work with emulators
            // https://stackoverflow.com/a/29946540
            ProviderInstaller.installIfNeeded(mContext);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, null);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN){
                if (event.isPrintingKey()){
                    sb = sb.append((char)event.getUnicodeChar());
                }
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
                    if (urlET.hasFocus()){
                        if (!sb.toString().isEmpty()) {
                            urlET.setText(sb.toString());
                            BaseActivity.setURL(sb.toString());
                            sb.setLength(0);
                        }
                    }
                    if (apiTokenET.hasFocus()){
                        if (!sb.toString().isEmpty()) {
                            apiTokenET.setText(sb.toString());
                            BaseActivity.setToken(sb.toString());
                            sb.setLength(0);
                            finish();
                        }
                    }
                }

            if (event.getKeyCode() == KeyEvent.KEYCODE_DEL){
                if (urlET.hasFocus()){
                        urlET.setText("");
                        sb.setLength(0);
                }
                if (apiTokenET.hasFocus()){
                        apiTokenET.setText("");
                        sb.setLength(0);
                        finish();
                }
            }

            if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
                if (urlET.hasFocus()){
                    apiTokenET.requestFocus();
                }
            }
        }
        return super.onKeyDown(event.getKeyCode(), event);
    }
}
