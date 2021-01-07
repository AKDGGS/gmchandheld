package gov.alaska.gmchandheld;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

public class AddInventory extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_inventory);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Color.parseColor("#ff567b95"));
        setSupportActionBar(toolbar);
    }
}
