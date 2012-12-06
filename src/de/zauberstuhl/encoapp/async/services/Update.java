package de.zauberstuhl.encoapp.async.services;

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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import de.zauberstuhl.encoapp.R;
import de.zauberstuhl.encoapp.ThreadHelper;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class Update extends Service {

	private static ThreadHelper th = new ThreadHelper();
	private static String TAG = th.appName+"Update";
		
	static int SIMPLE_NOTFICATION_ID = 0;
	NotificationManager mNotificationManager;
	Notification notifyDetails;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		if (th.D) Log.e(TAG, "++ onCreate ++");
		this.mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		this.notifyDetails = new Notification(R.drawable.ic_launcher,
				"New update available!", System.currentTimeMillis());
	}
	
	@Override
	public int onStartCommand(Intent i, int flags , int startId){
		if (th.D) Log.e(TAG, "++ onStartCommand ++");
		Message response = Message.obtain();
		response.arg1 = Activity.RESULT_CANCELED;
		
		try {
			URL u = new URL("http://"+th.HOST+"/version");
			InputStream is = u.openStream();
			DataInputStream dis = new DataInputStream(
					new BufferedInputStream(is));
			PackageInfo pInfo = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			String versionName = pInfo.versionName;
			String newVersion = dis.readLine();
			if (!versionName.equalsIgnoreCase(newVersion))
				sendNotification(newVersion);
		} catch (NameNotFoundException e) {
			if (th.D) Log.e(TAG, e.getMessage());
		} catch (MalformedURLException e) {
			if (th.D) Log.e(TAG, e.getMessage());
		} catch (IOException e) {
			if (th.D) Log.e(TAG, e.getMessage());
		}
		return Service.START_STICKY;
	}
	
    public void sendNotification(String newVersion) {
		Intent notify = new Intent(Intent.ACTION_VIEW,
				Uri.parse("http://"+th.HOST+"/latest.apk"));
		
		PendingIntent intent = PendingIntent.getActivity(getBaseContext(), 0,
				notify, android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
		notifyDetails.setLatestEventInfo(this, "New update available!",
				"Version "+newVersion, intent);
		notifyDetails.flags |= Notification.FLAG_AUTO_CANCEL;
		mNotificationManager.notify(SIMPLE_NOTFICATION_ID, notifyDetails);
    }
}
