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
    SharedPreferences sp;
    private EditText apiTokenET;
    private IntentIntegrator apiQrScan;
    private Button submitBtn, urlCameraBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_token);
        enableTSL(this);
        EditText urlET = findViewById(R.id.urlET);
        TextView urlTV = findViewById(R.id.urlTV);
        apiTokenET = findViewById(R.id.apiTokenET);
        urlCameraBtn = findViewById(R.id.urlCameraBtn);
        sp = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        if (!sp.getString("urlText", "").equals("")) {
            urlTV.setVisibility(View.GONE);
            urlET.setVisibility(View.GONE);
            urlCameraBtn.setVisibility(View.GONE);
            apiTokenET.requestFocus();
        } else {
            urlET.requestFocus();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("GMC Handheld");

        loadGetToken();
    }

    public void loadGetToken() {
        LookupDisplayObjInstance.getInstance().lookupLogicForDisplayObj = null;
        apiTokenET = findViewById(R.id.apiTokenET);
        Button cameraBtn = findViewById(R.id.cameraBtn);
//        urlCameraBtn = findViewById(R.id.urlCameraBtn);
        if (!BaseActivity.sp.getBoolean("cameraOn", false)) {
            cameraBtn.setVisibility(View.GONE);
            urlCameraBtn.setVisibility(View.GONE);
        } else {
            apiQrScan = new IntentIntegrator(this);
            apiQrScan.setBeepEnabled(true);
        }
        cameraBtn.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT <= 24) {
                apiQrScan.initiateScan();
            } else {
                Intent intent = new Intent(GetToken.this, CameraToScanner.class);
                startActivityForResult(intent, 0);
            }
        });
        submitBtn = findViewById(R.id.submitBtn);
        submitBtn.setOnClickListener(v -> {
            EditText urlET = findViewById(R.id.urlET);
            SharedPreferences sp = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
            TextView urlTV = findViewById(R.id.urlTV);

            if (sp.getString("urlText", "").equals("")) {
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("urlText", urlET.getText().toString());
                editor.apply();
            }

            if (!apiTokenET.getText().toString().isEmpty()) {
                BaseActivity.apiKeyBase = apiTokenET.getText().toString();
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
            if (resultCode == CommonStatusCodes.SUCCESS && data != null) {
                Barcode barcode = data.getParcelableExtra("barcode");
                if (barcode != null) {
                    apiTokenET.setText(barcode.displayValue);
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
}
