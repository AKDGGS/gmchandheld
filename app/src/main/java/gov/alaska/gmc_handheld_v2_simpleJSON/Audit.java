package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;

public class Audit extends BaseActivity {

    EditText et;
    ListView lv;
    ArrayList<String> auditingList;
    ArrayAdapter<String> adapter;
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audit);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        et = findViewById(R.id.editText1);

        lv = findViewById(R.id.listview);


        auditingList = new ArrayList<>();
        adapter = new ArrayAdapter<>(Audit.this, android.R.layout.simple_list_item_1, auditingList);
        lv.setAdapter(adapter);

        addKeyListener(); //Allows enter to be used to submit information using return.

    }

    private void addKeyListener() {
        et = findViewById(R.id.editText1);

        // add a keylistener to keep track user input
        et.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                // if keydown and "enter" is pressed
                if ((event.getAction() == KeyEvent.ACTION_DOWN)
                        && (keyCode == KeyEvent.KEYCODE_ENTER)) {

                    String result = et.getText().toString();
                    et.getText().clear();
                    if(auditingList.contains(result)){
                        Toast.makeText(getApplicationContext(),"Already in list",Toast.LENGTH_SHORT).show();
                    }else if(result == null || result.trim().equals("")) {
                        Toast.makeText(getApplicationContext(),"Empty",Toast.LENGTH_SHORT).show();
                    }else{
                        auditingList.add(result);
                        count++;
                        System.out.println(count);
                    }
                    adapter.notifyDataSetChanged();

                    return true;
                }

                return false;
            }
        });
    }
}
