package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;

public class GetBarcode extends BaseActivity {

	EditText barcodeInput;
	Button submit_button;
	ListView listView;
	ArrayList<String> lookupHistory = LookupHistoryHolder.getInstance().lookupHistory;
	ArrayAdapter<String> adapter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.get_barcode);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		toolbar.setBackgroundColor(Color.parseColor("#ff567b95"));
		submit_button = findViewById(R.id.submit_button);
		listView = findViewById(R.id.listView);

		adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lookupHistory);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(GetBarcode.this, Lookup.class);
				intent.putExtra(EXTRA_TEXT, listView.getItemAtPosition(position).toString());
				startActivity(intent);
			}
		});

		onButtonClick();
		addKeyListener();
	}


	public void onButtonClick() {

		submit_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String barcode = barcodeInput.getText().toString();
				lookupHistory.add(0, barcode);
				adapter.notifyDataSetChanged();

				switch (MainActivity.getButton_pushed()) {
					case "Help":
//                            openLookup();
						break;
					case "Summary":
					case "Lookup":
						Runnable runnable = new Runnable() {

							@Override
							public void run() {
								openLookup();
							}
						};
						Thread thread = new Thread(runnable);
						thread.start();

						break;
					case "Move":
						openMove();
						break;
					default:
						System.out.println("Error");
				}
			}
		});
	}

	private void addKeyListener() {
		barcodeInput = findViewById(R.id.editText1);

		// add a keylistener to keep track user input
		barcodeInput.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				// if keydown and "enter" is pressed
				if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {

					String barcode = barcodeInput.getText().toString();
					lookupHistory.add(0, barcode);
					adapter.notifyDataSetChanged();

					switch (MainActivity.getButton_pushed()) {
						case "Help":
//                            openLookup();
							break;
						case "Summary":
						case "Lookup":
							openLookup();
							break;
						case "Move":
							openMove();
							break;
						default:
							System.out.println("Error");
					}
//                    openLookup();
					return true;

				}

				return false;
			}
		});

	}


	private void openSummary() {
		EditText editText1 = findViewById(R.id.editText1);
		String text = editText1.getText().toString();

		Intent intent = new Intent(this, Summary.class);
		intent.putExtra(EXTRA_TEXT, text);


		startActivity(intent);
	}

	private void openLookup() {
//		final LookupBuildTree[] LookupBuildTreeObj = new LookupBuildTree[0];
		JsonPlaceHolderApi jsonPlaceHolderApi;
		EditText editText1 = findViewById(R.id.editText1);
		String BARCODE = editText1.getText().toString();

		String websiteURL = "http://maps.dggs.alaska.gov/gmc/inventory.json?barcode=" + BARCODE;
		DownloadDataBackground downloadClass = new DownloadDataBackground(GetBarcode.this);
		downloadClass.execute(websiteURL);


		System.out.println();


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
//		Call<ResponseBody> call = jsonPlaceHolderApi.getInventory(BARCODE);
//
//		LookupBuildTree LookupBuildTreeObj = null;
//		final Bundle bundle = new Bundle();
//
//		Response<ResponseBody> response = null;
//		try {
//
//			response = call.execute();
//			String rawJSON = response.body().string();
//			LookupBuildTreeObj = new LookupBuildTree();
//			LookupBuildTreeObj.processRawJSON(rawJSON);
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		Intent intent = new Intent(this, Lookup.class);
//		intent.putExtra(EXTRA_TEXT, BARCODE);
//		intent.putStringArrayListExtra("test", (ArrayList<String>) LookupBuildTreeObj.getKeyList());
//
//		HashMap<String, List<SpannableStringBuilder>> displayDict = (HashMap<String, List<SpannableStringBuilder>>) LookupBuildTreeObj.getDisplayDict();
//
////		System.out.println(LookupBuildTreeObj.getDisplayDict());
////		System.out.println(displayDict);
//
//		intent.putExtra("map", displayDict);
//		startActivity(intent);
////
//		startActivity(intent);


//		System.out.println(result);

//		call.enqueue(new Callback<ResponseBody>() {
//			Timer timer;
//
//			LookupBuildTree LookupBuildTreeObj = new LookupBuildTree();
//
//			@RequiresApi(api = Build.VERSION_CODES.KITKAT)
//			@Override
//			public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//
//				try {
//					assert response.body() != null;
//					String rawJSON = response.body().string();
//
//					if (rawJSON.length() == 2) {
////						timer = new Timer();
////						timer.schedule(new TimerTask() {
////							@Override
////							public void run() {
////								Intent intent = new Intent(Lookup.this, MainActivity.class);
////								startActivity(intent);
////								finish();
////							}
////						}, 100);
//
//
//					} else {
//
////						LookupBuildTreeObj = new LookupBuildTree();
//						LookupBuildTreeObj.processRawJSON(rawJSON);
//						System.out.println(LookupBuildTreeObj.getKeyList());
////						System.out.println(LookupBuildTreeObj.getDisplayDict());
//
//
//
//
////						//What appears on the screen
////						//Action Bar
////						if (getContainerBarcode() != null) {
////							Lookup.this.getSupportActionBar().setTitle(getContainerBarcode());
////
////							if (LookupBuildTreeObj.getDisplayDict().size() > 0) {
////								Lookup.this.getSupportActionBar().setSubtitle(LookupBuildTreeObj.getDisplayDict().size() + " Result(s)");
////							}
////						}
////
////						expandableListView = findViewById(R.id.expandableListView);
////						listAdapter = new LookupExpListAdapter(Lookup.this, LookupBuildTreeObj.getKeyList(), LookupBuildTreeObj.getDisplayDict());
////						expandableListView.setAdapter(listAdapter);
////
////
////						if (listAdapter.getGroupCount() >= 1) {
////							//expands only the first element
//////							expandableListView.expandGroup(0);
////							//expands all
////							for (int i = 0; i< listAdapter.getGroupCount(); i++) {
////								expandableListView.expandGroup(i);
////							}
////						}
//					}
//				} catch (IOException |
//						JSONException e) {
//					e.printStackTrace();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//
//
//			@Override
//			public void onFailure(Call<ResponseBody> call, Throwable t) {
//				System.out.println(t.getMessage());
//			}
//		});



	}

	private void openMove() {

		EditText editText1 = findViewById(R.id.editText1);
		String text = editText1.getText().toString();

		Intent intent = new Intent(this, Move.class);
		intent.putExtra(EXTRA_TEXT, text);


		startActivity(intent);
	}
}