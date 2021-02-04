package gov.alaska.gmchandheld;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

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

// https://vapoyan.medium.com/android-show-allertdialog-before-the-application-starts-80588d6f2dda

public class UpdateTranslucentActivity extends AppCompatActivity implements DialogInterface.OnClickListener {
	public static final String SHARED_PREFS = "sharedPrefs";

	// Storage Permissions
	private static final int REQUEST_EXTERNAL_STORAGE = 1;
	private static String[] PERMISSIONS_STORAGE = {
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE,
			Manifest.permission.REQUEST_INSTALL_PACKAGES
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_translucent);

		final AlertDialog dialog = new AlertDialog.Builder(this)
				.setTitle("Update Available")
				.setMessage("Tap Update to install the app.")
				.setCancelable(false)
//				.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialogInterface, int i) {
//						Toast.makeText(UpdateTranslucentActivity.this, "Ignore forever.", Toast.LENGTH_LONG).show();
//						Intent intent = new Intent(UpdateTranslucentActivity.this, Lookup.class);
//						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//						UpdateTranslucentActivity.this.startActivity(intent);
//					}
//				})
				.setNeutralButton("Ignore Update Forever", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {

//						SharedPreferences sharedPreferences = UpdateTranslucentActivity.this.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
//						long x  = sharedPreferences.getLong("LAST_MODIFIED_DATE", 0);
//						System.out.println("************************* " + x);

						Toast.makeText(UpdateTranslucentActivity.this, "Ignore forever.", Toast.LENGTH_LONG).show();
						Intent intent = new Intent(UpdateTranslucentActivity.this, Lookup.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
						UpdateTranslucentActivity.this.startActivity(intent);
					}
				})
				.setPositiveButton("Update", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
//						Toast.makeText(UpdateTranslucentActivity.this, "Install.", Toast.LENGTH_LONG).show();
						final String fileUrl;
						if (Build.VERSION.SDK_INT <= 17) {
							fileUrl = "http://maps.dggs.alaska.gov/gmcdev/app/current.apk";
						} else {
							fileUrl = "https://maps.dggs.alaska.gov/gmcdev/app/current.apk";
						}
						new DownloadFileFromURL(UpdateTranslucentActivity.this).execute(fileUrl);
					}
				})
				.create();
		dialog.show();
	}

	@Override
	public void onClick(DialogInterface dialogInterface, int i) {
		finish();
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				android.os.Process.killProcess(android.os.Process.myPid());
			}
		}, 100);
	}

	class DownloadFileFromURL extends AsyncTask<String, String, String> {
		DateFormat simple = new SimpleDateFormat("yyyyMMddHHmm");
		int versionJsonResponseCode = 200;
		int appFileResponseCode = 200;
		String filename = "current.apk";

		private WeakReference<Context> contextRef;

		public DownloadFileFromURL(Context context) {
			contextRef = new WeakReference<>(context);
		}

		@Override
		protected String doInBackground(String... f_url) {
			int count;
			try {
				URL url = new URL(f_url[0]);
				HttpURLConnection.setFollowRedirects(false);
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				versionJsonResponseCode = con.getResponseCode();
				try {

					if (con.getResponseCode() == 200) {
						con.connect();
						InputStream input = new BufferedInputStream(url.openStream(), 8192);
						input = new BufferedInputStream(url.openStream(), 8192);
						verifyStoragePermissions(UpdateTranslucentActivity.this);
						OutputStream output = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + filename);
						byte data[] = new byte[1024];
						long total = 0;

						while ((count = input.read(data)) != -1) {
							total += count;
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

			if ((versionJsonResponseCode == 200) && (appFileResponseCode == 200)) {
				Uri uriFile = Uri.fromFile(apkFile);
				if (context != null) {
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
	 * <p>
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