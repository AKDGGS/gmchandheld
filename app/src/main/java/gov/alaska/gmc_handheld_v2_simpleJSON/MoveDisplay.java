package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;


public class MoveDisplay extends BaseActivity {

	//	private final Button add_button = null;
	ListView containerListLV;

	ArrayList<String> containerList;
	ArrayAdapter<String> adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.move_display);

		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setBackgroundColor(Color.parseColor("#ff567b95"));
		setSupportActionBar(toolbar);

		final EditText moveContainerET = findViewById(R.id.moveContainerET);
		final EditText moveDestinationET = findViewById(R.id.destinationET);
		final TextView moveCountTV = findViewById(R.id.moveCountTV);
		final Button move_button = findViewById(R.id.move_button);
		final Button add_button = findViewById(R.id.add_container_button);
		containerListLV = findViewById(R.id.listViewGetContainersToMove);

		containerList = new ArrayList<>();

		adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
		containerListLV.setAdapter(adapter);


		add_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String container = moveContainerET.getText().toString();
				if (!container.isEmpty()) {
					containerList.add(container);
					adapter.add(container);
					adapter.notifyDataSetChanged();
					moveContainerET.setText("");
					moveCountTV.setText(String.valueOf(containerList.size()));
				}
				moveContainerET.requestFocus();
			}
		});

		final OpenLookup openLookupObj = new OpenLookup();

		if (!openLookupObj.isDownloading()) {
			containerListLV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
					final int which_item = position;
					adapter.remove(containerList.get(which_item));
					containerList.remove(which_item);
					adapter.notifyDataSetChanged();
					moveCountTV.setText(String.valueOf(containerList.size()));
					return false;
				}
			});

			// KeyListener listens if enter is pressed
			moveContainerET.setOnKeyListener(new View.OnKeyListener() {
				public boolean onKey(View v, int keyCode, KeyEvent event) {

					// if "enter" is pressed
					if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
						add_button.performClick();
						moveContainerET.requestFocus();
						return true;
					}
					return false;
				}
			});


			// KeyListener listens if enter is pressed
			moveDestinationET.setOnKeyListener(new View.OnKeyListener() {
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if (TextUtils.isEmpty(moveContainerET.getText())) {
						moveContainerET.setError("Container is required!");
					} else {
						// if "enter" is pressed
						if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
							add_button.performClick();
							moveContainerET.requestFocus();
							return true;
						}

					}
					return false;
				}
			});


			// onClickListener listens if the submit button is clicked
			move_button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if ((TextUtils.isEmpty(moveContainerET.getText())) && (containerList.size() == 0)) {
						System.out.println(containerList.size());
						moveContainerET.setError("A container is required!");
					} else if (TextUtils.isEmpty(moveDestinationET.getText())) {
						moveDestinationET.setError("A destination is required!");
					} else {
						moveContainer();
						moveContainerET.setText("");
						moveDestinationET.setText("");
						moveCountTV.setText("");
					}
				}
			});
		}
	}

	public String getDestination() {
		EditText destinationInput = findViewById(R.id.destinationET);
		return destinationInput.getText().toString();
	}

	public void moveContainer() {
		OpenLookup openLookup = new OpenLookup();
		openLookup.setDownloading(true);
		openLookup.processDataForDisplay(getDestination(), containerList, this);
	}


}