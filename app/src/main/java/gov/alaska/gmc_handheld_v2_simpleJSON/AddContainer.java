package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.widget.Toolbar;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class AddContainer extends BaseActivity {
	public static final String SHARED_PREFS = "sharedPrefs";
	private ListView addedContainerListLV;
	private ArrayAdapter<String> adapter;
	private LinkedList<String> addedContainerList  = AddContainerHistoryHolder.getInstance().getAddContainerHistory();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_container);


		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setBackgroundColor(Color.parseColor("#ff567b95"));
		setSupportActionBar(toolbar);

		final EditText addContainerBarcodeET = findViewById(R.id.getBarcodeEditText);
		final EditText addContainerNameET = findViewById(R.id.getNameEditText);
		final EditText addContainerRemark = findViewById(R.id.getRemarkEditText);
		final Button submit_button = findViewById(R.id.submit_button);

		addedContainerListLV = findViewById(R.id.listViewAddedContainerHistory);

		adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
		adapter.addAll(addedContainerList);
		addedContainerListLV.setAdapter(adapter);

		final RemoteApiUIHandler remoteApiUIHandler = new RemoteApiUIHandler();

//		final SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
//		if(sharedPreferences.getString(SHARED_PREFS, "savedDestination") != null){
//			moveDestinationET.setText(sharedPreferences.getString("savedDestination", ""));
//		}
//		if(sharedPreferences.getStringSet("savedContainerList", null) != null) {
//			containerList = new ArrayList<>(sharedPreferences.getStringSet("savedContainerList", null));
//			adapter.addAll(containerList);
//		}else {
//			containerList = new ArrayList<>();
//		}

		if (remoteApiUIHandler.isDownloading()) {
			// onClickListener listens if the submit button is clicked
			submit_button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (!(TextUtils.isEmpty(addContainerBarcodeET.getText()))) {

						String container = addContainerBarcodeET.getText().toString();
						if (!container.isEmpty()) {
							addContainer();
							addedContainerList.add(0, container);
							adapter.insert(container, 0);
							adapter.notifyDataSetChanged();
						}
						addContainerBarcodeET.setText("");
						addContainerNameET.setText("");
						addContainerRemark.setText("");
						addContainerBarcodeET.requestFocus();
					}
				}
			});
		}
	}

	public String getBarcode() {
		EditText barcodeInput = findViewById(R.id.getBarcodeEditText);
		return barcodeInput.getText().toString();
	}

	public String getName() {
		EditText barcodeInput = findViewById(R.id.getNameEditText);
		return barcodeInput.getText().toString();
	}

	public String getRemark() {
		EditText barcodeInput = findViewById(R.id.getRemarkEditText);
		return barcodeInput.getText().toString();
	}

	public void addContainer() {
		RemoteApiUIHandler remoteApiUIHandler = new RemoteApiUIHandler();
		RemoteApiUIHandler.setQueryOrDestination(getBarcode());
		RemoteApiUIHandler.setAddContainerName(getName());
		RemoteApiUIHandler.setAddContainerRemark(getRemark());

		remoteApiUIHandler.setDownloading(true);
		remoteApiUIHandler.processDataForDisplay(this);
	}

	@Override
	public void onBackPressed() {
		String[] containerArray = addedContainerList.toArray(new String[0]);
		Set<String> containerSet = new HashSet<>(Arrays.asList(containerArray));
		final EditText addContainerBarcodeET = findViewById(R.id.getBarcodeEditText);

		SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putStringSet("savedAddedContainerList", containerSet);
		editor.putString("savedAddedContainerBarcode", addContainerBarcodeET.getText().toString());
		editor.apply();

		super.onBackPressed();
	}

}
