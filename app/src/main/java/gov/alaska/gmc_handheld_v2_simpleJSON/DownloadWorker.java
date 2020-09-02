package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class DownloadWorker extends Worker {
	private Exception exceptionToBeThrown;
	private String barcode;

	public DownloadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
		super(context, workerParams);
	}

	@NonNull
	@Override
	public Result doWork() {
//		Data inputData = getInputData();
//		String json = inputData.getString("json");
//		String websiteURL;
//
//		int APILevel = android.os.Build.VERSION.SDK_INT;
//		if (APILevel < 18) {
//			websiteURL = "http://maps.dggs.alaska.gov/gmc/inventory.json?barcode=" + barcode;
//		} else {
//			websiteURL = "https://maps.dggs.alaska.gov/gmc/inventory.json?barcode=" + barcode;
//		}
//
//		InputStream inputStream;
//
//		// Retry code: https://stackoverflow.com/a/37443321
//		HttpURLConnection connection;
//
//		try {
//			URL myURL = new URL(websiteURL);
//			connection = (HttpURLConnection) myURL.openConnection();
//			connection.setReadTimeout(10000);
//			connection.setConnectTimeout(200000);
//			connection.setRequestMethod("GET");
//			connection.connect();
//
//			inputStream = connection.getInputStream();
//			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//
//			int i;
//			try {
//				i = inputStream.read();
//				while (i != -1) {
//					byteArrayOutputStream.write(i);
//					i = inputStream.read();
//				}
//				inputStream.close();
//				connection.disconnect();
//				return byteArrayOutputStream.toString();
//			} catch (IOException e) {
//				exceptionToBeThrown = e;
//				e.printStackTrace();
//			}
//		} catch (ProtocolException e) {
//			exceptionToBeThrown = e;
//			e.printStackTrace();
//		} catch (MalformedURLException e) {
//			exceptionToBeThrown = e;
//			e.printStackTrace();
//		} catch (IOException e) {
//			exceptionToBeThrown = e;
//			e.printStackTrace();
//		}
		return null;
	}
}
