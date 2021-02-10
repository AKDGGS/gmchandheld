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


public class AuditDisplay extends BaseActivity {
    public static final String SHARED_PREFS = "sharedPrefs";

    private ListView auditContainerListLV;
    private ArrayList<String> containerList;
    private ArrayAdapter<String> adapter;

    int clicks = 0;  //used to count double clicks for deletion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audit_display);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Color.parseColor("#ff567b95"));
        setSupportActionBar(toolbar);

        final EditText remarkContainerET = findViewById(R.id.remarkContainerET);
        final EditText auditRemarkET = findViewById(R.id.remarkET);
        final TextView auditCountTV = findViewById(R.id.auditCountTV);
        final Button submit_button = findViewById(R.id.submit_button);
        final Button add_button = findViewById(R.id.add_container_button);
        final Button clear_all_button = findViewById(R.id.clear_all_button);
        auditContainerListLV = findViewById(R.id.listViewGetContainersToAudit);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        auditContainerListLV.setAdapter(adapter);

        final SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        if (sharedPreferences.getString(SHARED_PREFS, "savedAuditRemark") != null) {
            auditRemarkET.setText(sharedPreferences.getString("savedAuditRemark", ""));
        }
        if (sharedPreferences.getStringSet("savedAuditContainerList", null) != null) {
            containerList = new ArrayList<>(sharedPreferences.getStringSet("savedAuditContainerList", null));
            adapter.addAll(containerList);
        } else {
            containerList = new ArrayList<>();
        }

        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String container = remarkContainerET.getText().toString();

                if (!container.isEmpty()) {
                    if (!(container.equals(auditRemarkET.getText().toString()) && (!containerList.contains(container)))) {
                        containerList.add(0, container);
                        adapter.insert(container, 0);
                        adapter.notifyDataSetChanged();
                        auditCountTV.setText(String.valueOf(containerList.size()));
                    }
                    remarkContainerET.setText("");
                }
                remarkContainerET.requestFocus();
            }

        });

        clear_all_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String container = remarkContainerET.getText().toString();
                remarkContainerET.setText("");

                remarkContainerET.requestFocus();
                containerList.clear();
                adapter.clear();
                adapter.notifyDataSetChanged();
                auditCountTV.setText(String.valueOf(containerList.size()));

            }
        });

        final RemoteApiUIHandler remoteApiUIHandler = new RemoteApiUIHandler();

        if (remoteApiUIHandler.isDownloading()) {
            //double click to remove elements
            auditContainerListLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
                                auditCountTV.setText(String.valueOf(containerList.size()));
                            }
                            clicks = 0;
                        }
                    }, 500);
                }
            });

            // KeyListener listens if enter is pressed
            remarkContainerET.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    // if "enter" is pressed
                    if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        add_button.performClick();
                        remarkContainerET.requestFocus();
                        return true;
                    }
                    return false;
                }
            });

            // KeyListener listens if enter is pressed
            auditRemarkET.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (!(TextUtils.isEmpty(auditRemarkET.getText()))) {
                        // if "enter" is pressed
                        if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                            remarkContainerET.requestFocus();
                            return true;
                        }
                    }
                    return false;
                }
            });

            // onClickListener listens if the submit button is clicked
            submit_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckConfiguration checkConfiguration = new CheckConfiguration();
                    if (checkConfiguration.checkConfiguration(AuditDisplay.this)) {
                        if ((containerList.size() > 0)) {
                            auditContainerFn(auditRemarkET.getText().toString());
                            remarkContainerET.setText("");
                            auditRemarkET.setText("");
                            auditCountTV.setText("");
                            sharedPreferences.edit().remove("savedAuditContainerList").apply();
                            sharedPreferences.edit().remove("savedAuditRemark").apply();
                        }
                    }
                }
            });
        }
    }

    public void auditContainerFn(String remarkInput) {
        RemoteApiUIHandler remoteApiUIHandler = new RemoteApiUIHandler();
        RemoteApiUIHandler.setUrlFirstParameter(remarkInput);
        RemoteApiUIHandler.setContainerList(containerList);
        remoteApiUIHandler.setDownloading(true);
        remoteApiUIHandler.processDataForDisplay(this);
    }

    @Override
    public void onBackPressed() {
        String[] containerArray = containerList.toArray(new String[0]);
        Set<String> containerSet = new HashSet<>(Arrays.asList(containerArray));
        final EditText remarkET = findViewById(R.id.remarkET);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("savedAuditContainerList", containerSet);
        editor.putString("savedAuditRemark", remarkET.getText().toString());
        editor.apply();

        super.onBackPressed();
    }

}
