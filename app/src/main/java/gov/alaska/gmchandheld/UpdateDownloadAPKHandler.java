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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

// https://vapoyan.medium.com/android-show-allertdialog-before-the-application-starts-80588d6f2dda

public class UpdateDownloadAPKHandler extends AppCompatActivity implements DialogInterface.OnClickListener {
	public static final String SHARED_PREFS = "sharedPrefs";

	// Storage Permissions
	private static final int REQUEST_EXTERNAL_STORAGE = 1;
	private static String[] PERMISSIONS_STORAGE = {
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE,
			Manifest.permission.REQUEST_INSTALL_PACKAGES
	};

	@Override
	protected void onRestart() {
		super.onRestart();
		UpdateDownloadAPKHandler.this.finish();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_translucent);
		alert();
	}

	private void alert() {
		Intent intent = getIntent();
		final Long lastModifiedRefused = intent.getLongExtra("LAST_MODIFIED_DATE", 0);


		final AlertDialog dialog = new AlertDialog.Builder(this)
				.setTitle("Update Available")
				.setMessage("Tap Update to install the app.")
				.setCancelable(false)

				.setNeutralButton("Ignore Update Forever", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						Toast.makeText(UpdateDownloadAPKHandler.this, "Ignore forever.", Toast.LENGTH_LONG).show();

						//If a user refuses an update, the last modified date for that update is saved in shared preferences,
						SharedPreferences sp = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = sp.edit();
						editor.putLong("ignoreUpdateDateSP", lastModifiedRefused).commit();

						Intent intent = new Intent(UpdateDownloadAPKHandler.this, Lookup.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						UpdateDownloadAPKHandler.this.startActivity(intent);
					}
				})
				.setPositiveButton("Update", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						final String urlStr;
						SharedPreferences sharedPreferences = UpdateDownloadAPKHandler.this.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
						urlStr = sharedPreferences.getString("urlText", "") + "app/current.apk";
						new DownloadFileFromURL(UpdateDownloadAPKHandler.this).execute(urlStr);
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


	private static class DownloadFileFromURL extends AsyncTask<String, String, String> {
		int versionJsonResponseCode;
		String filename = "current.apk";

		private WeakReference<UpdateDownloadAPKHandler> mActivity;

		public DownloadFileFromURL(UpdateDownloadAPKHandler context) {
			mActivity = new WeakReference<UpdateDownloadAPKHandler>(context);
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
					con.connect();
					InputStream input = new BufferedInputStream(url.openStream(), 8192);
					input = new BufferedInputStream(url.openStream(), 8192);
					verifyStoragePermissions(mActivity.get());
					OutputStream output = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + filename);
					byte[] data = new byte[1024];
					long total = 0;

					while ((count = input.read(data)) != -1) {
						total += count;
						output.write(data, 0, count);
					}

					output.flush();
					output.close();
					input.close();
				} catch (IOException e) {
					if (null != e.getMessage()){
						Log.e("Error: ", e.getMessage());
					}
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


			Context context = mActivity.get();

			if ((versionJsonResponseCode == 200)) {
				Uri uriFile = Uri.fromFile(apkFile);
				if (context != null) {
					if (Build.VERSION.SDK_INT >= 24) {
						uriFile = FileProvider.getUriForFile(context,  context.getPackageName() + ".provider",
								apkFile);
					}
				}
				intent = new Intent(Intent.ACTION_INSTALL_PACKAGE, uriFile);
				intent.setDataAndType(uriFile, "application/vnd.android.package-archive");
				intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			} else {
				Toast.makeText(context, "No update available.", Toast.LENGTH_LONG).show();
			}
		}
	}

	public static void verifyStoragePermissions(Activity activity) {
		// Check if we have write permission
		int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

		if (permission != PackageManager.PERMISSION_GRANTED) {
			// If we don't have permission so prompt the user
			ActivityCompat.requestPermissions(
					activity,
					PERMISSIONS_STORAGE,
					REQUEST_EXTERNAL_STORAGE
			);
		}
	}
}