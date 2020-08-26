package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import java.util.ArrayList;


public class Lookup extends BaseActivity {


	ExpandableListView expandableListView;
	ExpandableListAdapter listAdapter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {

//		JsonPlaceHolderApi jsonPlaceHolderApi;

		super.onCreate(savedInstanceState);
		setContentView(R.layout.lookup);



		getSupportActionBar().setDisplayShowHomeEnabled(true);

		Intent intent = getIntent();

		ArrayList<String> keyList= intent.getStringArrayListExtra("test");

		System.out.println(keyList);
//		HashMap<String, List<SpannableStringBuilder>> hashMap = (HashMap<String, List<SpannableStringBuilder>>)intent.getSerializableExtra("map");
//
//		if (getContainerBarcode() != null) {
//			Lookup.this.getSupportActionBar().setTitle(getContainerBarcode());
//
//			if (keyList.size() > 0) {
//				Lookup.this.getSupportActionBar().setSubtitle(keyList.size() + " Result(s)");
//			}
//		}
//
//		expandableListView = findViewById(R.id.expandableListView);
//		listAdapter = new LookupExpListAdapter(Lookup.this, keyList, hashMap);
//		expandableListView.setAdapter(listAdapter);

//		expandableListView = findViewById(R.id.expandableListView);
//		listAdapter = new LookupExpListAdapter(Lookup.this, LookupBuildTreeObj.getKeyList(), LookupBuildTreeObj.getDisplayDict());
//		expandableListView.setAdapter(listAdapter);


//		if (listAdapter.getGroupCount() >= 1) {
//			//expands only the first element
////							expandableListView.expandGroup(0);
//			//expands all
//			for (int i = 0; i< listAdapter.getGroupCount(); i++) {
//				expandableListView.expandGroup(i);
//			}
//		}

		// Gets the API level
//		int APILevel = android.os.Build.VERSION.SDK_INT;
//		if (APILevel < 18) {
//			// Current okhttp3 doesn't work with Android < 5, so using an old version (https://stackoverflow.com/questions/61245270/glide-okhttp-for-android-api-16-not-working#comment108349740_61245529)
//			//Not all Android devices support TSL 1.2 (API >= 16 - API <18 and possibly other versions depending on the device)
//			//Works with http, but not https with API 16
//			Retrofit retrofit = new Retrofit.Builder()
//					.baseUrl("http://maps.dggs.alaska.gov/gmc/")
//					.addConverterFactory(GsonConverterFactory.create())
//					.build();
//			jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);
//		} else {
//			Retrofit retrofit = new Retrofit.Builder()
//					.baseUrl("https://maps.dggs.alaska.gov/gmc/")
//					.addConverterFactory(GsonConverterFactory.create())
//					.build();
//			jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);
//
//		}
//
//		String BARCODE;  //retrieved from user input used to call dggs
//		BARCODE = getContainerBarcode();

//		Call<ResponseBody> call = jsonPlaceHolderApi.getInventory(BARCODE);
//		call.enqueue(new Callback<ResponseBody>() {
//			Timer timer;
//
//			@RequiresApi(api = Build.VERSION_CODES.KITKAT)
//			@Override
//			public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//
////				LookupBuildTree LookupBuildTreeObj;
//				ExpandableListView expandableListView;
//				ExpandableListAdapter listAdapter;
//
//				try {
//					assert response.body() != null;
//					String rawJSON = response.body().string();
//
//					if (rawJSON.length() == 2) {
//						timer = new Timer();
//						timer.schedule(new TimerTask() {
//							@Override
//							public void run() {
//								Intent intent = new Intent(Lookup.this, MainActivity.class);
//								startActivity(intent);
//								finish();
//							}
//						}, 100);
//
//
//					} else {
//
//						LookupBuildTreeObj = new LookupBuildTree();
//						LookupBuildTreeObj.processRawJSON(rawJSON);
//
//						//What appears on the screen
//						//Action Bar
//						if (getContainerBarcode() != null) {
//							Lookup.this.getSupportActionBar().setTitle(getContainerBarcode());
//
//							if (LookupBuildTreeObj.getDisplayDict().size() > 0) {
//								Lookup.this.getSupportActionBar().setSubtitle(LookupBuildTreeObj.getDisplayDict().size() + " Result(s)");
//							}
//						}
//
//						expandableListView = findViewById(R.id.expandableListView);
//						listAdapter = new LookupExpListAdapter(Lookup.this, LookupBuildTreeObj.getKeyList(), LookupBuildTreeObj.getDisplayDict());
//						expandableListView.setAdapter(listAdapter);
//
//
//						if (listAdapter.getGroupCount() >= 1) {
//							//expands only the first element
////							expandableListView.expandGroup(0);
//							//expands all
//							for (int i = 0; i< listAdapter.getGroupCount(); i++) {
//								expandableListView.expandGroup(i);
//							}
//						}
//					}
//				}
//				catch (IOException |
//						JSONException e) {
//					e.printStackTrace();
//				}
//				catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//
//			@Override
//			public void onFailure(Call<ResponseBody> call, Throwable t) {
//				System.out.println(t.getMessage());
//			}
//		});

	}


	private String getContainerBarcode() {
		Intent intent = getIntent();
		return intent.getStringExtra(GetBarcode.EXTRA_TEXT);
	}
}