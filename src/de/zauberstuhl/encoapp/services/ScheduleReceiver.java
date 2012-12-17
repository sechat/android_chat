package de.zauberstuhl.encoapp.services;

import java.util.Calendar;

import de.zauberstuhl.encoapp.ThreadHelper;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScheduleReceiver extends BroadcastReceiver {
		
	@Override
	public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, Listener.class);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pending = PendingIntent.getBroadcast(
        		context, 0, service, PendingIntent.FLAG_CANCEL_CURRENT);
        Calendar cal = Calendar.getInstance();
        // Start 10 seconds after boot completed
        cal.add(Calendar.SECOND, 10);
        // InexactRepeating allows Android to optimize the energy consumption
        alarmManager.setInexactRepeating(
        		AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), ThreadHelper.REPEAT_TIME, pending);
	}
}
