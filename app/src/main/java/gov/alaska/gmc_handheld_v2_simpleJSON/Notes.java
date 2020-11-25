package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Notes extends BaseActivity  {
	public static final String SHARED_PREFS = "sharedPrefs";
	List<String> notesList;
	ArrayAdapter<String> adapter;
	ListView listView;
	EditText notesEditText;

	int clicks = 0;  //used to count double clicks for deletion

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notes_layout);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		toolbar.setBackgroundColor(Color.parseColor("#ff567b95"));

		notesEditText = findViewById(R.id.notes_editText);
		listView = findViewById(R.id.notes_listView);

		adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
		listView.setAdapter(adapter);

		final SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

		if(sharedPreferences.getStringSet("savedNotesList", null) != null) {
			notesList = new ArrayList<>(sharedPreferences.getStringSet("savedNotesList", null));
			adapter.addAll(notesList);
		}else {
			System.out.println("savedNotesList is null");
			notesList = new ArrayList<>();
		}

		//double click to remove elements
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			long startTime = System.currentTimeMillis();

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {
				clicks++;

				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						if (clicks == 2) {
							adapter.remove(notesList.get(position));
							notesList.remove(position);
							adapter.notifyDataSetChanged();
						}
						clicks = 0;
					}
				}, 500);
			}
		});
	}

	public void addItemToList(View view){
		if(notesList.size() < 5) {
			notesList.add(0, notesEditText.getText().toString());
			adapter.insert(notesEditText.getText().toString(), 0);
			adapter.notifyDataSetChanged();
			notesEditText.setText("");
		}else{
			Toast.makeText(this, "Notes can only hold 5 items.", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onBackPressed() {
		String[] containerArray = notesList.toArray(new String[0]);
		Set<String> containerSet = new HashSet<>(Arrays.asList(containerArray));

		SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putStringSet("savedNotesList", containerSet);
		editor.apply();

		super.onBackPressed();
	}
}