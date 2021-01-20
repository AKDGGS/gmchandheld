package gov.alaska.gmchandheld;

import androidx.appcompat.widget.Toolbar;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MoveContents extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.move_contents);

		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setBackgroundColor(Color.parseColor("#ff567b95"));
		setSupportActionBar(toolbar);

		final EditText moveContentsSourceET = findViewById(R.id.sourceET);
		final EditText moveContentsDestinationET = findViewById(R.id.destinationET);
		final Button submit_button = findViewById(R.id.submit_button);

		// onClickListener listens if the submit button is clicked
		submit_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CheckConfiguration checkConfiguration = new CheckConfiguration();
				if (checkConfiguration.checkConfiguration(MoveContents.this)) {
					if (!(TextUtils.isEmpty(moveContentsSourceET.getText())) & !(TextUtils.isEmpty(moveContentsDestinationET.getText()))) {
						moveContents(moveContentsSourceET.getText().toString(), moveContentsDestinationET.getText().toString());
						moveContentsSourceET.setText("");
						moveContentsDestinationET.setText("");
					}
				}
			}
		});

		// KeyListener listens if enter is pressed
		moveContentsDestinationET.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// if "enter" is pressed
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
					submit_button.performClick();
					return true;
				}
				return false;
			}
		});
	}

	public void moveContents(String sourceInput, String destinationInput) {
		RemoteApiUIHandler remoteApiUIHandler = new RemoteApiUIHandler();
		RemoteApiUIHandler.setUrlFirstParameter(sourceInput);
		RemoteApiUIHandler.setDestinationBarcode(destinationInput);
		remoteApiUIHandler.setDownloading(true);
		remoteApiUIHandler.processDataForDisplay(this);
	}
}