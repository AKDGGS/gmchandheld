package gov.alaska.gmchandheld;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

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
import java.util.Calendar;
import java.util.Date;

public class UpdateExecutableService extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent intent2 = new Intent(context, UpdateAppActivity.class);
		intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent2);
	}


//	Context mContext;
//	private Activity activity;
//
//	// Storage Permissions
//	private static final int REQUEST_EXTERNAL_STORAGE = 1;
//	private static String[] PERMISSIONS_STORAGE = {
//			Manifest.permission.READ_EXTERNAL_STORAGE,
//			Manifest.permission.WRITE_EXTERNAL_STORAGE,
//			Manifest.permission.REQUEST_INSTALL_PACKAGES,
//			Manifest.permission.SYSTEM_ALERT_WINDOW
//	};
//
//	@Override
//	public void onReceive(Context context, Intent intent) {
//
//		System.out.println("On Receive called......");
//		StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
//		StrictMode.setVmPolicy(builder.build());
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
//			builder.detectFileUriExposure();
//		}
//
//		mContext = context;
//
//		Calendar now = Calendar.getInstance();
//		int hour = now.get(Calendar.HOUR_OF_DAY);
//		int minute = now.get(Calendar.MINUTE);
//		int second = now.get(Calendar.SECOND);
//		System.out.println(hour + " " + minute);
//
//		final String fileUrl;
//		if (Build.VERSION.SDK_INT <= 17) {
//			fileUrl = "http://maps.dggs.alaska.gov/gmcdev/app/version.json";
//		}else{
//			fileUrl = "https://maps.dggs.alaska.gov/gmcdev/app/version.json";
//		}
//		new DownloadFileFromURL().execute(fileUrl);
//
//	}
//
//	class DownloadFileFromURL extends AsyncTask<String, String, String> {
//		StringBuilder sb;
//		DateFormat simple = new SimpleDateFormat("yyyyMMddHHmm");
//		String currentBuildTime = simple.format(new Date(BuildConfig.TIMESTAMP));
//		int versionJsonResponseCode = 200;
//		int appFileResponseCode = 200;
//		String rawJSON;
//		String build;
//		String filename;
//
//
//
//		@Override
//		protected String doInBackground(String... f_url) {
//			int count;
//			try {
//				URL url = new URL(f_url[0]);
//				System.out.println(url);
//				URLConnection connection = url.openConnection();
//
////				connection.setConnectTimeout(10000);
////				connection.setReadTimeout(10000);
//				try {
//					connection.connect();
//					HttpURLConnection.setFollowRedirects(false);
//					HttpURLConnection con = (HttpURLConnection) new URL(f_url[0]).openConnection();
//					con.setRequestMethod("HEAD");
//					versionJsonResponseCode = con.getResponseCode();
//
//					if (versionJsonResponseCode == 200) {
//						String version = "";
//						sb = new StringBuilder();
//						InputStream input = new BufferedInputStream(url.openStream(), 8192);
//						BufferedReader reader = new BufferedReader(new InputStreamReader(input));
//						while (true) {
//							try {
//								if ((version = reader.readLine()) == null) break;
//							} catch (IOException e) {
//								e.printStackTrace();
//							}
//							rawJSON = sb.append(version).toString();
//							try {
//								JSONObject inputJson = new JSONObject(rawJSON);
//								build = inputJson.optString("build");
//								filename = inputJson.optString("filename");
//							} catch (JSONException e) {
//								e.printStackTrace();
//							}
//						}
//						input.close();
//
//						if ((versionJsonResponseCode == 200) && (Double.parseDouble(currentBuildTime) != Double.parseDouble(build))) {
//							String fileUrl;
//							if (Build.VERSION.SDK_INT <= 17) {
//								fileUrl = "http://maps.dggs.alaska.gov/gmcdev/app/" + filename;
//							}else{
//								fileUrl = "https://maps.dggs.alaska.gov/gmcdev/app/" + filename;
//							}
//
//							try {
//								con = (HttpURLConnection) new URL(fileUrl).openConnection();
//								con.setRequestMethod("HEAD");
//								if (con.getResponseCode() == 200) {
//									url = new URL(fileUrl);
//									connection = url.openConnection();
////				connection.setConnectTimeout(10000);
////				connection.setReadTimeout(10000);
//									connection.connect();
//									input = new BufferedInputStream(url.openStream(), 8192);
////									verifyStoragePermissions((Activity) mContext);
//									OutputStream output = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + filename);
//									byte data[] = new byte[1024];
//									long total = 0;
//
//									while ((count = input.read(data)) != -1) {
//										total += count;
//										// writing data to file
//										output.write(data, 0, count);
//									}
//
//									output.flush();
//									output.close();
//									input.close();
//								} else {
//									appFileResponseCode = con.getResponseCode();
//								}
//							} catch (IOException e) {
//								Log.e("Error: ", e.getMessage());
//							}
//						}
//					}
//
//				} catch (MalformedURLException e) {
//					e.printStackTrace();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			} catch (MalformedURLException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			return null;
//		}
//
//		@Override
//		protected void onPostExecute(String fileUrl) {
//			Intent intent = new Intent();
//			File apkFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + filename);
//			Uri apkURI = Uri.fromFile(apkFile);
////			Context context = contextRef.get();
//
//			if ((versionJsonResponseCode == 200) && (appFileResponseCode == 200)){
////				&& (Double.parseDouble(currentBuildTime) != Double.parseDouble(build))) {
//				Uri uriFile = Uri.fromFile(apkFile);
////				if (context != null){
////					if (Build.VERSION.SDK_INT >= 24) {
////						uriFile = FileProvider.getUriForFile(context, getApplicationContext().getPackageName() + ".provider",
////								apkFile);
////					}
////				}
//
//				System.out.println(mContext);
////				 Intent to open apk
//				intent = new Intent(Intent.ACTION_INSTALL_PACKAGE, uriFile);
//				intent.setDataAndType(uriFile, "application/vnd.android.package-archive");
//				intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//				mContext.startActivity(intent);
//			} else {
////				Toast.makeText(getBaseContext(), "No update available.", Toast.LENGTH_LONG).show();
//			}
//		}
//	}
//
//	/**
//	 * Checks if the app has permission to write to device storage
//	 *
//	 * If the app does not has permission then the user will be prompted to grant permissions
//	 *
//	 * @param activity
//	 */
//	public static void verifyStoragePermissions(Activity activity) {
//		// Check if we have write permission
//		int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//
//		if (permission != PackageManager.PERMISSION_GRANTED) {
//			// We don't have permission so prompt the user
//			ActivityCompat.requestPermissions(
//					activity,
//					PERMISSIONS_STORAGE,
//					REQUEST_EXTERNAL_STORAGE
//			);
//		}
//	}
}