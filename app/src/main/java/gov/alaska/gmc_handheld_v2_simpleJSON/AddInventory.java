package gov.alaska.gmc_handheld_v2_simpleJSON;

import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;

public class AddInventory extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_inventory);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
}
