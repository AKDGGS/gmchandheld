package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

public class Help extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
}
