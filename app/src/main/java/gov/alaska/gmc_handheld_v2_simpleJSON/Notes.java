package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class Notes extends AppCompatActivity {

	List<String> notesList;
	ArrayAdapter<String> arrayAdapter;
	ListView listView;
	EditText editText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notes_layout);

		notesList = new ArrayList<>();
		arrayAdapter = new ArrayAdapter<>(this, R.layout.notes_list_view_layout, notesList);
		listView = findViewById(R.id.notes_listView);

		listView.setAdapter(arrayAdapter);

		editText = findViewById(R.id.notes_editText);

	}

	public void addItemToList(View view){
		notesList.add(editText.getText().toString());
		arrayAdapter.notifyDataSetChanged();

		editText.setText("");
	}
}