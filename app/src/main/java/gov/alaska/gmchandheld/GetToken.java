package gov.alaska.gmchandheld;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.Nullable;
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
import javax.net.ssl.SSLEngine;


public class GetToken extends BaseActivity{
    private EditText apiTokenET;
    private IntentIntegrator apiQrScan;
    private Button submitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_token);
        try {
            // enables TSL-1.2 if Google Play is updated on old devices.
            // doesn't work with emulators
            // https://stackoverflow.com/a/29946540
            ProviderInstaller.installIfNeeded(this.getApplicationContext());
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, null);
            SSLEngine engine = sslContext.createSSLEngine();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Color.parseColor("#ff567b95"));
        setSupportActionBar(toolbar);
        loadGetToken();
    }

    public void loadGetToken() {
        LookupDisplayObjInstance.getInstance().lookupLogicForDisplayObj = null;
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Get Personal Access Token");
        toolbar.setBackgroundColor(Color.parseColor("#ff567b95"));
        sp = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        apiTokenET = findViewById(R.id.apiTokenET);
        boolean cameraOn = (sp.getBoolean("cameraOn", false));
        Button cameraBtn = findViewById(R.id.cameraBtn);
        if (!cameraOn) {
            cameraBtn.setVisibility(View.GONE);
        } else {
            apiQrScan = new IntentIntegrator(this);
            apiQrScan.setBeepEnabled(true);
        }
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT <= 24) {
                    apiQrScan.initiateScan();
                } else {
                    Intent intent = new Intent(GetToken.this, CameraToScanner.class);
                    startActivityForResult(intent, 0);
                }
            }
        });
        submitBtn = findViewById(R.id.submitBtn);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!apiTokenET.getText().toString().isEmpty()) {
                    BaseActivity.apiKeyBase = apiTokenET.getText().toString();
                    finish();
                }
            }
        });
        apiTokenET.setOnKeyListener(new EditText.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // if "enter" is pressed
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    submitBtn.performClick();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        apiTokenET = findViewById(R.id.apiTokenET);
        if (Build.VERSION.SDK_INT <= 24) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            apiTokenET.setText(result.getContents());
            if (!apiTokenET.getText().toString().isEmpty()) {
                BaseActivity.apiKeyBase = apiTokenET.getText().toString();
                Intent intent = new Intent(GetToken.this, Lookup.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                GetToken.this.startActivity(intent);
            }
        } else {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                Barcode barcode = data.getParcelableExtra("barcode");
                if (null != barcode.displayValue) {
                    apiTokenET.setText(barcode.displayValue);
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }
}
