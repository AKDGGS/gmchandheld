package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Move extends BaseActivity {

    private JsonPlaceHolderApi jsonPlaceHolderApi;

    private final MoveParseJSONBase moveParseJSONBase = new MoveParseJSONBase();

    HashMap<String, String> map = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.move);


        // Current okhttp3 doesn't work with Android < 5, so using an old version (https://stackoverflow.com/questions/61245270/glide-okhttp-for-android-api-16-not-working#comment108349740_61245529)
        //Not all Android devices support TSL 1.2 (API >= 16 - API <18 and possibly other versions depending on the device)
        //Works with http, but not https with API 16
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://maps.dggs.alaska.gov/gmc/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);


        getRawJSON();

        System.out.println("Hello");
        String x = (Collections.singletonList(map)).toString();
        x = x.replace("=", ":");
        System.out.println(x);
    }

    private void getRawJSON() {
        String BARCODE;  //retrieved from user input used to call dggs
        BARCODE = get_container_barcode();

        Call<ResponseBody> call = jsonPlaceHolderApi.getInventory(BARCODE);
        call.enqueue(new Callback<ResponseBody>() {
            Timer timer;

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                JSONArray jsonArray;
                JSONObject inputJSONObject;

                try {
                    assert response.body() != null;
                    String rawJSON = response.body().string();

                    if (rawJSON.length() == 2) {
                        timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(Move.this, GetBarcode.class);
                                startActivity(intent);
                                finish();
                            }
                        }, 1000);

                    } else {
                        jsonArray = new JSONArray(rawJSON);

                        if (jsonArray.length() <= 1) {
                            inputJSONObject = (JSONObject) jsonArray.get(0);

                            TextView tv1 = findViewById(R.id.textView1);


                            tv1.setText("Current Location: " + inputJSONObject.get("containerPath"));


                            map = moveParseJSONBase.getBaseNodesFromJSON(inputJSONObject);

                            addKeyListener();


//                            map.put("\"containerPath\"", "\"New location\"");


                        } else if (jsonArray.length() > 1) {

                            System.out.println("JsonArray Size: " + jsonArray.length());

                            for (int i = 0; i < jsonArray.length(); i++) {
                                map = moveParseJSONBase.getBaseNodesFromJSON(jsonArray.getJSONObject(i));
                                map.put("\"containerPath\"", "\"New location\"");
//                                String x = (Arrays.asList(map)).toString();
//                                x = x.replace("=", ":");
//                                System.out.println(x);
                            }

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


    private void addKeyListener() {
        EditText et2 = findViewById(R.id.editText2);

        // add a keylistener to keep track user input
        et2.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // if keydown and "enter" is pressed
                if ((event.getAction() == KeyEvent.ACTION_DOWN)
                        && (keyCode == KeyEvent.KEYCODE_ENTER)) {

                    EditText et2 = findViewById(R.id.editText2);
                    String text = et2.getText().toString();
                    TextView ev2 = findViewById(R.id.textView2);
                    ev2.setText(text);
                    map.put("\"containerPath\"", "\"" + text + "\"");

                    returnJSONString();
                    return true;

                }

                return false;
            }
        });

    }

    public void returnJSONString(){
        String x = (Collections.singletonList(map)).toString();
        x = x.replace("=", ":");
        System.out.println(x);
    }
}