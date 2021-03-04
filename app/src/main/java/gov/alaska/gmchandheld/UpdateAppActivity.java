package gov.alaska.gmchandheld;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.lang.ref.WeakReference;

public class UpdateAppActivity extends AppCompatActivity {

    @Override
    protected void onRestart() {
        super.onRestart();
        Intent intent = new Intent(this, Lookup.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        UpdateAppActivity.this.finish();
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_app);
        new UpdateCheckLastModifiedDate(this).execute();
    }
}