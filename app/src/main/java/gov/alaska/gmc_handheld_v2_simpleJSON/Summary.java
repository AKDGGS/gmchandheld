package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

public class Summary extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.summary);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        OpenLookup openLookup = new OpenLookup();
        openLookup.processDataForDisplay("GMC-000076260", this);

    }
}
