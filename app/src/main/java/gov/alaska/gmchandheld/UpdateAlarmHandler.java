package gov.alaska.gmchandheld;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

public class UpdateAlarmHandler {
	private Context context;
	public static final String SHARED_PREFS = "sharedPrefs";
	public static final String HOUR_TEXT = "updateHour";
	public static final String MINUTE_TEXT = "updateMinute";

	public UpdateAlarmHandler(Context context) {
		this.context = context;
	}

	public void setAlarmManager() {

		Intent intent = new Intent(context, UpdateBroadcastReceiver.class);
		PendingIntent sender = PendingIntent.getBroadcast(context, 2, intent, 0);
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		if (am != null) {
			String strTime = "2021-01-20 14:07:00";
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			Date d = null;
			try {
				d = dateFormat.parse(strTime);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		 	String hour = sharedPreferences.getString(HOUR_TEXT, "24");
			String minute = sharedPreferences.getString(MINUTE_TEXT, "0");

			Calendar alarmOffTime = Calendar.getInstance();
			alarmOffTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
			alarmOffTime.set(Calendar.MINUTE, Integer.parseInt(minute));
			alarmOffTime.set(Calendar.SECOND, 0);

//			if (alarmOffTime.before(Calendar.getInstance())) {
//				alarmOffTime.add(Calendar.DATE, 1);
//			}

			long triggerEvery = 1 * 60 * 1000;
			am.setRepeating(AlarmManager.RTC_WAKEUP, alarmOffTime.getTimeInMillis(), triggerEvery, sender);
		}
	}

	public void cancelAlarmManager() {
		Intent intent = new Intent(context, UpdateBroadcastReceiver.class);
		PendingIntent sender = PendingIntent.getBroadcast(context, 2, intent, 0);
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		if (am != null) {
			am.cancel(sender);
		}
	}
}