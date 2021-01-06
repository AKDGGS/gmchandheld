package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

public class Recode extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recode);

		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setBackgroundColor(Color.parseColor("#ff567b95"));
		setSupportActionBar(toolbar);
	}
}