package gov.alaska.gmchandheld;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AlarmHandler {
	private Context context;

	public AlarmHandler(Context context) {
		this.context = context;
	}

	public void setAlarmManager(){
		System.out.println("Set Alarm called");
		Intent intent = new Intent(context, ExecutableService.class);
		PendingIntent sender = PendingIntent.getBroadcast(context, 2, intent, 0);
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		if(am != null){
			String strTime = "2021-01-20 14:07:00";
			System.out.println(strTime);
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			Date d = null;
			try {
				d = dateFormat.parse(strTime);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			long time= d.getTime();
			long triggerAfter = time + 10 * 1000;
			long triggerEvery = 60 * 60 * 1000;
			am.setRepeating(AlarmManager.RTC_WAKEUP, triggerAfter, triggerEvery, sender);
		}
	}

	public void cancelAlarmManager(){
		Intent intent = new Intent(context, ExecutableService.class);
		PendingIntent sender = PendingIntent.getBroadcast(context, 2, intent, 0);
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		if(am != null) {
			am.cancel(sender);
		}
	}
}