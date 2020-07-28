package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class Lookup extends BaseActivity {

    private JsonPlaceHolderApi jsonPlaceHolderApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lookup);

        // Current okhttp3 doesn't work with Android < 5, so using an old version (https://stackoverflow.com/questions/61245270/glide-okhttp-for-android-api-16-not-working#comment108349740_61245529)
        //Not all Android devices support TSL 1.2 (API >= 16 - API <18 and possibly other versions depending on the device)
        //Works with http, but not https with API 16
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://maps.dggs.alaska.gov/gmc/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);

        processRawJSON();
    }

    private void processRawJSON() {
        String BARCODE;  //retrieved from user input used to call dggs
        BARCODE = get_container_barcode();

        Call<ResponseBody> call = jsonPlaceHolderApi.getInventory(BARCODE);
        call.enqueue(new Callback<ResponseBody>() {
            Timer timer;

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                JSONArray jsonArray;
                JSONObject inputJSONObject;

                ExpandableListView expandableListView;
                ExpandableListAdapter listAdapter;

                try {
                    assert response.body() != null;
                    String rawJSON = response.body().string();

                    if (rawJSON.length() == 2) {
//                        textViewHeader.setText("The URL didn't work.\nPlease enter another barcode.");

                        timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(Lookup.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }, 100);

                    } else {
                        jsonArray = new JSONArray(rawJSON);

                        if (jsonArray.length() <= 1) {

                            inputJSONObject = (JSONObject) jsonArray.get(0);

                            String containerPath = inputJSONObject.get("containerPath").toString();
                            getSupportActionBar().setTitle(containerPath);

                            Map<String, List<SpannableStringBuilder>> displayDict;
                            displayDict = LookupBuildTree.setupDisplay(inputJSONObject);
                            Set<String> keys = displayDict.keySet();

                            List<String> keyList = new ArrayList<>(keys);

                            expandableListView = findViewById(R.id.expandableListView);
                            listAdapter = new MyExpListAdapter(Lookup.this, keyList, displayDict);
                            expandableListView.setAdapter(listAdapter);


                        } else if (jsonArray.length() > 1) {

//                            lookupBuildTree.setArray(true); //Used to display the count in the display header
                            Map<String, List<SpannableStringBuilder>> displayDict = new HashMap<>();
                            List<String> keyList = new ArrayList<>();

                            String containerPath = "";
                            for (int i = 0; i < jsonArray.length(); i++) {
                                containerPath = jsonArray.getJSONObject(i).get("containerPath").toString();
                                String barcode = jsonArray.getJSONObject(i).get("barcode").toString();
                                String IDNumber = jsonArray.getJSONObject(i).get("ID").toString();
                                keyList.add(barcode + "-" + IDNumber);

                                Map<String, List<SpannableStringBuilder>> tmpDisplayDict;

                                tmpDisplayDict = LookupBuildTree.setupDisplay(jsonArray.getJSONObject(i));
                                displayDict.putAll(tmpDisplayDict);
                            }
                            System.out.println(displayDict.size());

                            getSupportActionBar().setTitle(containerPath);
                            getSupportActionBar().setSubtitle("Count: " + (displayDict.size()));


                            expandableListView = findViewById(R.id.expandableListView);
                            listAdapter = new MyExpListAdapter(Lookup.this, keyList, displayDict);
                            expandableListView.setAdapter(listAdapter);
                        }

                    }
                } catch (IOException |
                        JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                System.out.println(t.getMessage());
            }
        });
    }


    private String get_container_barcode() {
        Intent intent = getIntent();
        return intent.getStringExtra(GetBarcode.EXTRA_TEXT);
    }
}