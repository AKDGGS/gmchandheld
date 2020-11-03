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
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;


public class MoveDisplay extends BaseActivity {

	Button add_button;
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
		final Button move_button = findViewById(R.id.move_button);
		Button add_button = findViewById(R.id.add_container_button);
		containerListLV = findViewById(R.id.listViewGetContainersToMove);

		containerList = new ArrayList<>();
		adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
		containerListLV.setAdapter(adapter);

		add_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String container = moveContainerET.getText().toString();
				containerList.add(container);
				adapter.add(container);
				adapter.notifyDataSetChanged();
				moveContainerET.setText("");
				moveContainerET.requestFocus();

			}
		});

		containerListLV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
				final int which_item = position;
				adapter.remove(containerList.get(which_item));
				containerList.remove(which_item);
//				System.out.println(containerList.get(which_item));
//				System.out.println(containerList.toString());
				adapter.notifyDataSetChanged();
				return false;
			}
		});


		// KeyListener listens if enter is pressed
		moveDestinationET.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (TextUtils.isEmpty(moveDestinationET.getText())) {
					moveDestinationET.setError("A destination is required!");
				} else if (TextUtils.isEmpty(moveContainerET.getText())) {
					moveContainerET.setError("Container is required!");

				} else {
					// if "enter" is pressed
					if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
						move_button.performClick();
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
					Toast.makeText(getApplicationContext(), moveContainerET.getText().toString() + " was moved to " + moveDestinationET.getText().toString(),
							Toast.LENGTH_LONG).show();

					moveContainerET.setText("");
					moveDestinationET.setText("");
				}
			}
		});
	}

	public String getDestination() {
		EditText destinationInput = findViewById(R.id.destinationET);
		return destinationInput.getText().toString();
	}

	public String containersToMoveStr(ArrayList<String> list) {
		String delim = "&c=";

		StringBuilder sb = new StringBuilder();

		sb.append(delim);
		int i = 0;
		while (i < list.size() - 1) {
			sb.append(list.get(i));
			sb.append(delim);
			i++;
		}
		sb.append(list.get(i));

		String res = sb.toString();
		return res;
	}

	public void moveContainer() {
		containersToMoveStr(containerList);
		Move move = new Move();
		move.processDataForDisplay(getDestination(), containersToMoveStr(containerList), this);

	}


}