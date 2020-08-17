package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;

import androidx.annotation.RequiresApi;

import org.json.JSONException;

import java.io.IOException;
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
    public LookupBuildTree LookupBuildTreeObj;
    private Context context = this;
    private ExpandableListView expandableListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Gets the API level
        int APILevel = android.os.Build.VERSION.SDK_INT;
        if( APILevel < 18) {
            // Current okhttp3 doesn't work with Android < 5, so using an old version (https://stackoverflow.com/questions/61245270/glide-okhttp-for-android-api-16-not-working#comment108349740_61245529)
            //Not all Android devices support TSL 1.2 (API >= 16 - API <18 and possibly other versions depending on the device)
            //Works with http, but not https with API 16
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://maps.dggs.alaska.gov/gmc/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);
        }else{
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://maps.dggs.alaska.gov/gmc/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);

        }
        getJSON();
    }

    private void getJSON() {
        String BARCODE;  //retrieved from user input used to call dggs
        BARCODE = get_container_barcode();

        Call<ResponseBody> call = jsonPlaceHolderApi.getInventory(BARCODE);
        call.enqueue(new Callback<ResponseBody>() {
            Timer timer;

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {


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

                        // Constructs the layout for lookupBuildTree
                        LinearLayout linearLayout = new LinearLayout(Lookup.this);
                        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout
                                .LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                        linearLayout.setLayoutParams(linearLayoutParams);
                        linearLayout.setOrientation(LinearLayout.VERTICAL);

                        // Constructs the expandableList
                        LinearLayout.LayoutParams expListParams = new LinearLayout
                                .LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                        ExpandableListView expandableListView = new ExpandableListView(Lookup.this.getApplicationContext());
                        expandableListView.setLayoutParams(expListParams);
                        // removes the down arrow indicator for the expandable list
                        expandableListView.setGroupIndicator(null);
                        linearLayout.addView(expandableListView);

                        Activity activity = Lookup.this;
                        activity.setContentView(linearLayout);

                        LookupBuildTreeObj = new LookupBuildTree();
                        LookupBuildTreeObj.processRawJSON(rawJSON);

                        //What appears on the screen
                        //Action Bar
                         if (LookupBuildTreeObj.getContainerPath() != null) {
                            Lookup.this.getSupportActionBar().setTitle(LookupBuildTreeObj.getContainerPath());

                            if (LookupBuildTreeObj.getDisplayDict().size() > 1) {
                                Lookup.this.getSupportActionBar().setSubtitle("Count: " + LookupBuildTreeObj.getDisplayDict().size());
                            }
                        }

                         //Expandable list
                        ExpandableListAdapter listAdapter = new LookupExpListAdapter(Lookup.this, LookupBuildTreeObj.getKeyList(), LookupBuildTreeObj.getDisplayDict());
                        expandableListView.setAdapter(listAdapter);
                    }
                } catch (IOException |
                        JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
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