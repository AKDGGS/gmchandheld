package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;

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
    LookupBuildTree obj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        obj = new LookupBuildTree(this);

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
                        //Needs a better name than obj
                        obj.buildLookupLayout(rawJSON);

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