package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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

	ListView containerListLV;

	ArrayList<String> containerList;
	ArrayAdapter<String> adapter;

	int clicks = 0;

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
		long timeLastClick;

		containerList = new ArrayList<>();

		adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
		containerListLV.setAdapter(adapter);


		add_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String container = moveContainerET.getText().toString();
				if (!container.isEmpty()) {
					if (!containerList.contains(container)) {
						containerList.add(container);
						adapter.add(container);
						adapter.notifyDataSetChanged();
					}
					moveContainerET.setText("");
					moveCountTV.setText(String.valueOf(containerList.size()));
				}
				moveContainerET.requestFocus();
			}
		});

		final RemoteAPITask remoteAPITaskObj = new RemoteAPITask();

		if (!remoteAPITaskObj.isDownloading()) {

			//Long click option to remove added elements

//			containerListLV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//				@Override
//				public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
//					final int which_item = position;
//					adapter.remove(containerList.get(which_item));
//					containerList.remove(which_item);
//					adapter.notifyDataSetChanged();
//					moveCountTV.setText(String.valueOf(containerList.size()));
//					return false;
//				}
//			});

			//double click to remove elements
			containerListLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				long startTime = System.currentTimeMillis();

				@Override
				public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {
					clicks++;

					Handler handler = new Handler();
					handler.postDelayed(new Runnable(){
						@Override
						public void run() {
							if(clicks == 2){
								final int which_item = position;
								adapter.remove(containerList.get(which_item));
								containerList.remove(which_item);
								adapter.notifyDataSetChanged();
								moveCountTV.setText(String.valueOf(containerList.size()));
							}
							clicks = 0;
						}
					}, 500);
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
					if (!(TextUtils.isEmpty(moveContainerET.getText()))) {
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
//						moveContainerET.setError("A container is required!");
					} else if (TextUtils.isEmpty(moveDestinationET.getText())) {
//						moveDestinationET.setError("A destination is required!");
					} else {
						moveContainer();
						moveContainerET.setText("");
						moveDestinationET.setText("");
						moveCountTV.setText("");
						System.out.println(containerList);
						containerList.clear();
						System.out.println(containerList);
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
		RemoteAPITask remoteAPITask = new RemoteAPITask();
		remoteAPITask.setDownloading(true);
		remoteAPITask.processDataForDisplay(getDestination(), containerList, this);
	}


}