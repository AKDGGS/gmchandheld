package gov.alaska.gmchandheld;

import android.content.SharedPreferences;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MoveDisplay extends BaseActivity {
	public static final String SHARED_PREFS = "sharedPrefs";

	private ListView containerListLV;
	private ArrayList<String> containerList;
	private ArrayAdapter<String> adapter;

	int clicks = 0;  //used to count double clicks for deletion

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
		final Button clear_all_button = findViewById(R.id.clear_all_button);
		containerListLV = findViewById(R.id.listViewGetContainersToMove);

		adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
		containerListLV.setAdapter(adapter);

		final SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		if (sharedPreferences.getString(SHARED_PREFS, "savedDestination") != null) {
			moveDestinationET.setText(sharedPreferences.getString("savedDestination", ""));
		}
		if (sharedPreferences.getStringSet("savedContainerList", null) != null) {
			containerList = new ArrayList<>(sharedPreferences.getStringSet("savedContainerList", null));
			adapter.addAll(containerList);
		} else {
			containerList = new ArrayList<>();
		}

		add_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String container = moveContainerET.getText().toString();

					if (!container.isEmpty()) {
						if (!(container.equals(moveDestinationET.getText().toString()) && (!containerList.contains(container)))) {
							containerList.add(0, container);
							adapter.insert(container, 0);
							adapter.notifyDataSetChanged();
							moveCountTV.setText(String.valueOf(containerList.size()));
						}
						moveContainerET.setText("");
					}
					moveContainerET.requestFocus();
				}

		});

		clear_all_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String container = moveContainerET.getText().toString();
				moveContainerET.setText("");

				moveContainerET.requestFocus();
				containerList.clear();
				adapter.clear();
				adapter.notifyDataSetChanged();
				moveCountTV.setText(String.valueOf(containerList.size()));

			}
		});

		final RemoteApiUIHandler remoteApiUIHandler = new RemoteApiUIHandler();

		if (remoteApiUIHandler.isDownloading()) {
			//double click to remove elements
			containerListLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				final long startTime = System.currentTimeMillis();

				@Override
				public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {
					clicks++;

					Handler handler = new Handler();
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							if (clicks == 2) {
								adapter.remove(containerList.get(position));
								containerList.remove(position);
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
					if (!(TextUtils.isEmpty(moveDestinationET.getText()))) {
						// if "enter" is pressed
						if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
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
					CheckConfiguration checkConfiguration = new CheckConfiguration();
					if (checkConfiguration.checkConfiguration(MoveDisplay.this)) {
						if (!(TextUtils.isEmpty(moveDestinationET.getText())) && (containerList.size() > 0)) {
							moveContainer(moveDestinationET.getText().toString());
							moveContainerET.setText("");
							moveDestinationET.setText("");
							moveCountTV.setText("");
							sharedPreferences.edit().remove("savedContainerList").apply();
							sharedPreferences.edit().remove("savedDestination").apply();
						}
					}
				}
			});
		}
	}

	public void moveContainer(String destinationInput) {
		RemoteApiUIHandler remoteApiUIHandler = new RemoteApiUIHandler();
		RemoteApiUIHandler.setUrlFirstParameter(destinationInput);
		RemoteApiUIHandler.setContainerList(containerList);

		remoteApiUIHandler.setDownloading(true);
		remoteApiUIHandler.processDataForDisplay(this);
	}

	@Override
	public void onBackPressed() {
		String[] containerArray = containerList.toArray(new String[0]);
		Set<String> containerSet = new HashSet<>(Arrays.asList(containerArray));
		final EditText moveDestinationET = findViewById(R.id.destinationET);

		SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putStringSet("savedContainerList", containerSet);
		editor.putString("savedDestination", moveDestinationET.getText().toString());
		editor.apply();

		super.onBackPressed();
	}
}