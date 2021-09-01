package gov.alaska.gmchandheld;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class GetToken extends AppCompatActivity {
    private EditText apiTokenET;
    private IntentIntegrator apiQrScan;
    private Button submitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_token);
        apiTokenET = findViewById(R.id.apiTokenET);
        apiTokenET.requestFocus();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("GMC Handheld");
        loadGetToken();
    }

    public void loadGetToken() {
        LookupDisplayObjInstance.getInstance().lookupLogicForDisplayObj = null;
        apiTokenET = findViewById(R.id.apiTokenET);
        Button cameraBtn = findViewById(R.id.cameraBtn);
        if (!BaseActivity.sp.getBoolean("cameraOn", false)) {
            cameraBtn.setVisibility(View.GONE);
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
            if (!apiTokenET.getText().toString().isEmpty()) {
                BaseActivity.apiKeyBase = apiTokenET.getText().toString();
                finish();
            }
        });
        apiTokenET.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
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
            if (resultCode == CommonStatusCodes.SUCCESS && null != data ) {
                Barcode barcode = data.getParcelableExtra("barcode");
                if (null != barcode) {
                    apiTokenET.setText(barcode.displayValue);
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }
}
