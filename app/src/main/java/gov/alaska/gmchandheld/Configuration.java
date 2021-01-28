package gov.alaska.gmchandheld;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Configuration extends BaseActivity {

	public static final String SHARED_PREFS = "sharedPrefs";
	public static final String URL_TEXT = "urlText";
	public static final String API_TEXT = "apiText";
	public static final String UPDATE_SWITCH_TEXT = "updateSwitchOnOff";
	private SwitchCompat autoUpdateSwitch;
	private boolean autoUpdateOnOff;

	private EditText urlInput;
	private EditText apiInput;
	private String url;
	private String apiKey;

	// Storage Permissions
	private static final int REQUEST_EXTERNAL_STORAGE = 1;
	private static String[] PERMISSIONS_STORAGE = {
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE,
			Manifest.permission.REQUEST_INSTALL_PACKAGES
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
		StrictMode.setVmPolicy(builder.build());
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			builder.detectFileUriExposure();
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.configuration);

		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setBackgroundColor(Color.parseColor("#ff567b95"));
		setSupportActionBar(toolbar);

		Date buildDate = new Date(BuildConfig.TIMESTAMP);
		TextView buildDateTV = findViewById(R.id.buildDateTV);
		buildDateTV.setText(DateFormat.getDateTimeInstance().format(buildDate));

		urlInput = findViewById(R.id.url_editText);
		apiInput = findViewById(R.id.api_editText);
		autoUpdateSwitch = findViewById(R.id.autoUpdateSwitch);



		final Button updateButton = findViewById(R.id.updateBtn);
		updateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateAPK();
			}
		});

		final Button saveButton = findViewById(R.id.saveBtn);
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveData();
			}
		});

		loadData();
		updateViews();

	}

	public String getUrl() {
		urlInput = findViewById(R.id.url_editText);

		url = urlInput.getText().toString();
		if (url.length() > 1 && url.charAt(url.length() - 1) != ('/')) {
			url = url + '/';
		}
		return url;
	}

	public String getApiKey() {
		apiInput = findViewById(R.id.api_editText);
		return apiInput.getText().toString();
	}

	public void saveData() {
		SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();

//		if ("".equals(getUrl())) {
//			Toast.makeText(this, "You did not enter an URL.", Toast.LENGTH_LONG).show();
//		} else {
//			getUrl();
//			if ("".equals(getApiKey())) {
//				Toast.makeText(this, "You did not enter an API key.", Toast.LENGTH_LONG).show();
//			} else {
				getApiKey();
				editor.putString(URL_TEXT, getUrl());
				editor.putString(API_TEXT, getApiKey());
				editor.putBoolean(UPDATE_SWITCH_TEXT, autoUpdateSwitch.isChecked());

				editor.apply();
				Toast.makeText(this, "Changes saved.", Toast.LENGTH_LONG).show();

				Intent intent = new Intent(this, Lookup.class);
				startActivity(intent);
//			}
//		}
	}

	public void loadData() {
		SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		url = sharedPreferences.getString(URL_TEXT, "");
		apiKey = sharedPreferences.getString(API_TEXT, "");
		autoUpdateOnOff = sharedPreferences.getBoolean(UPDATE_SWITCH_TEXT , false);

	}

	public void updateViews() {
		urlInput.setText(url);
		apiInput.setText(apiKey);
	}

	public void updateAPK() {
		new UpdateCheckLastModifiedDate(this).execute();
//		final String fileUrl;
//		if (Build.VERSION.SDK_INT <= 17) {
//			fileUrl = "http://maps.dggs.alaska.gov/gmcdev/app/version.json";
//		}else{
//			fileUrl = "https://maps.dggs.alaska.gov/gmcdev/app/version.json";
//		}
//
//		final Context mContext = this;
//		final AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
//
//		ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
//		if (!(cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected())) {
//			alertDialog.setMessage("Is the device connected to the internet/network?  " +
//					"Check if the connection has been lost.");
//			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
//			View layout = inflater.inflate(R.layout.lookup_error_display, (ViewGroup) ((Activity) mContext).findViewById(R.id.lookup_error_root));
//			alertDialog.setView(layout);
//			alertDialog.setPositiveButton("Dimiss", null);
//			AlertDialog alert = alertDialog.create();
//			alert.setCanceledOnTouchOutside(false);
//			alert.show();
//		} else {
//			new DownloadFileFromURL(this).execute(fileUrl);
//		}
	}

	class DownloadFileFromURL extends AsyncTask<String, String, String> {
		StringBuilder sb;
		DateFormat simple = new SimpleDateFormat("yyyyMMddHHmm");
		String currentBuildTime = simple.format(new Date(BuildConfig.TIMESTAMP));
		int versionJsonResponseCode = 200;
		int appFileResponseCode = 200;
		String rawJSON;
		String build;
		String filename;

		private WeakReference<Context> contextRef;

		public DownloadFileFromURL(Context context) {
			contextRef = new WeakReference<>(context);
		}
		@Override
		protected String doInBackground(String... f_url) {
			int count;
			try {
				URL url = new URL(f_url[0]);
				System.out.println(url);
				URLConnection connection = url.openConnection();


//				connection.setConnectTimeout(10000);
//				connection.setReadTimeout(10000);
				try {
					connection.connect();
					HttpURLConnection.setFollowRedirects(false);
					HttpURLConnection con = (HttpURLConnection) new URL(f_url[0]).openConnection();
					versionJsonResponseCode = con.getResponseCode();

					if (versionJsonResponseCode == 200) {
						String version = "";
						sb = new StringBuilder();
						InputStream input = new BufferedInputStream(url.openStream(), 8192);
						BufferedReader reader = new BufferedReader(new InputStreamReader(input));
						while (true) {
							try {
								if ((version = reader.readLine()) == null) break;
							} catch (IOException e) {
								e.printStackTrace();
							}
							rawJSON = sb.append(version).toString();
							try {
								JSONObject inputJson = new JSONObject(rawJSON);
								build = inputJson.optString("build");
								filename = inputJson.optString("filename");
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
						input.close();

						if ((versionJsonResponseCode == 200) && (Double.parseDouble(currentBuildTime) != Double.parseDouble(build))) {
							String fileUrl;
							if (Build.VERSION.SDK_INT <= 17) {
								fileUrl = "http://maps.dggs.alaska.gov/gmcdev/app/" + filename;
							}else{
								fileUrl = "https://maps.dggs.alaska.gov/gmcdev/app/" + filename;
							}

							try {
								con = (HttpURLConnection) new URL(fileUrl).openConnection();
								con.setRequestMethod("HEAD");
								if (con.getResponseCode() == 200) {
									url = new URL(fileUrl);
									connection = url.openConnection();
//				connection.setConnectTimeout(10000);
//				connection.setReadTimeout(10000);
									connection.connect();
									input = new BufferedInputStream(url.openStream(), 8192);
									verifyStoragePermissions(Configuration.this);
									OutputStream output = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + filename);
									byte data[] = new byte[1024];
									long total = 0;

									while ((count = input.read(data)) != -1) {
										total += count;
										// writing data to file
										output.write(data, 0, count);
									}

									output.flush();
									output.close();
									input.close();
								} else {
									appFileResponseCode = con.getResponseCode();
								}
							} catch (IOException e) {
								Log.e("Error: ", e.getMessage());
							}
						}
					}

				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String fileUrl) {
			Intent intent = new Intent();
			File apkFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + filename);
			Uri apkURI = Uri.fromFile(apkFile);
			Context context = contextRef.get();

			if ((versionJsonResponseCode == 200) && (appFileResponseCode == 200)){
//				&& (Double.parseDouble(currentBuildTime) != Double.parseDouble(build))) {
				Uri uriFile = Uri.fromFile(apkFile);
				if (context != null){
					if (Build.VERSION.SDK_INT >= 24) {
					uriFile = FileProvider.getUriForFile(context, getApplicationContext().getPackageName() + ".provider",
							apkFile);

					}
				}
//				 Intent to open apk
				intent = new Intent(Intent.ACTION_INSTALL_PACKAGE, uriFile);
				intent.setDataAndType(uriFile, "application/vnd.android.package-archive");
				intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			} else {
				Toast.makeText(getBaseContext(), "No update available.", Toast.LENGTH_LONG).show();
			}
		}
	}

	/**
	 * Checks if the app has permission to write to device storage
	 *
	 * If the app does not has permission then the user will be prompted to grant permissions
	 *
	 * @param activity
	 */
	public static void verifyStoragePermissions(Activity activity) {
		// Check if we have write permission
		int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

		if (permission != PackageManager.PERMISSION_GRANTED) {
			// We don't have permission so prompt the user
			ActivityCompat.requestPermissions(
					activity,
					PERMISSIONS_STORAGE,
					REQUEST_EXTERNAL_STORAGE
			);
		}
	}
}