package gov.alaska.gmchandheld;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class UpdateBroadcastReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent intent2 = new Intent(context, UpdateAppActivity.class);
		intent2.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK );
		context.startActivity(intent2);
	}
}