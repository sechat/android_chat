package de.zauberstuhl.encoapp.services;

/**
 * Copyright (C) 2012 Lukas Matt <lukas@zauberstuhl.de>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

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
