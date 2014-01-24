package org.sechat.app.services;

/**
 * Copyright (c) 2014 Sechat GbR <support@sechat.org>
 *
 * You should have received a copy of the MIT License
 * along with this program (license.txt).
 * If not, see <http://sechat.github.io/license.txt>.
 */

import java.util.Calendar;

import org.sechat.app.ThreadHelper;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScheduleReceiver extends BroadcastReceiver {
		
	@Override
	public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, Listener.class);
        Calendar cal = Calendar.getInstance();
        PendingIntent pending = PendingIntent.getService(
        		context, 0, service, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
        		ThreadHelper.REPEAT_TIME, pending);
	}
}
