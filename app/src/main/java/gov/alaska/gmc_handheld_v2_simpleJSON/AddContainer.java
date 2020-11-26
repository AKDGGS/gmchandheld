package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.widget.Toolbar;

public class AddContainer extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_container);

		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setBackgroundColor(Color.parseColor("#ff567b95"));
		setSupportActionBar(toolbar);

		final EditText addContainerBarcodeET = findViewById(R.id.getBarcodeEditText);
		final EditText addContainerNameET = findViewById(R.id.getNameEditText);
		final EditText addContainerRemarkET = findViewById(R.id.getRemarkEditText);
		final Button submit_button = findViewById(R.id.submit_button);

		final RemoteApiUIHandler remoteApiUIHandler = new RemoteApiUIHandler();

		if (remoteApiUIHandler.isDownloading()) {
			// onClickListener listens if the submit button is clicked
			submit_button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (!(TextUtils.isEmpty(addContainerBarcodeET.getText()))) {

						String container = addContainerBarcodeET.getText().toString();
						if (!container.isEmpty()) {
							RemoteApiUIHandler remoteApiUIHandler = new RemoteApiUIHandler();
							RemoteApiUIHandler.setQueryOrDestination(addContainerBarcodeET.getText().toString());
							RemoteApiUIHandler.setAddContainerName(addContainerNameET.getText().toString());
							RemoteApiUIHandler.setAddContainerRemark(addContainerRemarkET.getText().toString());

							remoteApiUIHandler.setDownloading(true);
							remoteApiUIHandler.processDataForDisplay(AddContainer.this);
						}
						addContainerBarcodeET.setText("");
						addContainerNameET.setText("");
						addContainerRemarkET.setText("");
						addContainerBarcodeET.requestFocus();
					}
				}
			});
		}
	}
}
