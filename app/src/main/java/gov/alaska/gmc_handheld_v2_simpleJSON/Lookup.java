package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import androidx.annotation.RequiresApi;

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

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                ExpandableListView expandableListView;
                ExpandableListAdapter listAdapter;

                try {
                    assert response.body() != null;
                    String rawJSON = response.body().string();

                    if (rawJSON.length() == 2) {
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
                        JSONArray inputJson = new JSONArray((rawJSON));


                        if (inputJson.length() <= 1) {

                            JSONObject inputJSONObject = (JSONObject) inputJson.get(0);

                            if(((JSONObject)inputJson.get(0)).has("containerPath")) {
                                String containerPath = ((JSONObject) inputJson.get(0)).get("containerPath").toString();
                                getSupportActionBar().setTitle(containerPath);
                            }

                            Map<String, List<SpannableStringBuilder>> displayDict;
                            displayDict = LookupBuildTree.setupDisplay(inputJSONObject);
                            Set<String> keys = displayDict.keySet();

                            List<String> keyList = new ArrayList<>(keys);


                            expandableListView = findViewById(R.id.expandableListView);

                            listAdapter = new LookupExpListAdapter(Lookup.this, keyList, displayDict);
                            expandableListView.setAdapter(listAdapter);
                            if (listAdapter.getGroupCount() == 1){
                                expandableListView.expandGroup(0);
                            }

                        }
                        else if (inputJson.length() > 1) {

                            Map<String, List<SpannableStringBuilder>> displayDict = new HashMap<>();
                            List<String> keyList = new ArrayList<>();

                            String containerPath = "";

                            for (int i = 0; i < inputJson.length(); i++) {

                                JSONObject inputJSONObject = (JSONObject) inputJson.get(i);

                                containerPath = inputJson.getJSONObject(i).get("containerPath").toString();
                                String barcode = inputJson.getJSONObject(i).get("barcode").toString();
                                String IDNumber = inputJson.getJSONObject(i).get("ID").toString();
                                keyList.add(barcode + "-" + IDNumber);

                                displayDict.putAll(LookupBuildTree.setupDisplay(inputJSONObject));
                            }

                            getSupportActionBar().setTitle(containerPath);
                            getSupportActionBar().setSubtitle("Count: " + (displayDict.size()));

                            expandableListView = findViewById(R.id.expandableListView);
                            listAdapter = new LookupExpListAdapter(Lookup.this, keyList, displayDict);
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