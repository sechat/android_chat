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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.SocketTimeoutException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLSocket;

import de.zauberstuhl.encoapp.Main;
import de.zauberstuhl.encoapp.R;
import de.zauberstuhl.encoapp.ThreadHelper;
import de.zauberstuhl.encoapp.adapter.DataBaseAdapter;
import de.zauberstuhl.encoapp.enc.Encryption;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class Listener extends Service {

	private static ThreadHelper th = new ThreadHelper();
	private static Encryption encryption = new Encryption();
	private static String TAG = th.appName+"Listener";
	
	private static boolean cancel = false;
	public void cancel(boolean c) { Listener.cancel = c; }
	public boolean isCancelled() { return Listener.cancel; }
	
	private DataBaseAdapter db;
	private String nickName;
	private SSLSocket socket;
	private PrintWriter out;
	private BufferedReader in;
	
	private Messenger outMessenger;
	public static final String FRIEND = "friend";
	public static final String IAM = "iam";
	
	static int SIMPLE_NOTFICATION_ID = 0;
	NotificationManager mNotificationManager;
	Notification notifyDetails;
	
	@Override
	public IBinder onBind(Intent intent) {
		Bundle extras = intent.getExtras();
		// Get messager from the Activity
		if (extras != null) {
			outMessenger = (Messenger) extras.get("MESSENGER");
		}
		return null;
	}
	
	@Override
	public void onCreate() {
		if (th.D) Log.e(TAG, "++ onCreate ++");
		this.db = new DataBaseAdapter(this);
		this.nickName = th.getMd5Sum(db.getContactName(0));
		
		this.mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		this.notifyDetails = new Notification(R.drawable.ic_launcher,
				"New message!", System.currentTimeMillis());
		
        try {
        	socket = th.getConnection(db);
        	in = new BufferedReader(new InputStreamReader(
        			socket.getInputStream())
        	);
        	out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
        	if (th.D) Log.e(TAG, e.getMessage());
        	cancel(true);
        } catch (KeyManagementException e) {
        	if (th.D) Log.e(TAG, e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			if (th.D) Log.e(TAG, e.getMessage());
		}
	}
	
	@Override
	public int onStartCommand(Intent i, int flags , int startId){
		if (th.D) Log.e(TAG, "++ onStartCommand ++");
		out.println("LISTENER()");
		new Thread(thread).start();
		return Service.START_STICKY;
	}
	
	private Runnable thread = new Runnable() {
		@Override
		public synchronized void run() {
			if (isCancelled()) return;
			try {
				while (!isCancelled()) {
					Bundle bundle = new Bundle();
					Message response = Message.obtain();
					String line = in.readLine();
					if (line == null) return;
					if (th.D) Log.e(TAG, line);
					
					if (line.startsWith("MSG")) {
						String[] data = line.substring(4, line.length()-1).split(",");
						String friend = data[0];
						String message = data[1];
											
						if (friend.equalsIgnoreCase("SEVERE"))
							friend = th.getMd5Sum(th.getActiveChatUser());
						else message = encryption.decrypt(db.getPrivateKey(0), message);
						
						response.arg1 = Activity.RESULT_OK;
						th.addDiscussionEntry(friend, message, false);
						bundle.putString(FRIEND, friend);
						sendNotification(friend, message);
					}
					
					if (line.startsWith("IAM")) {
						String plainFriend = line.substring(4, line.length()-1);
						if (!db.isset(plainFriend)) {
							response.arg1 = Activity.RESULT_OK;
							bundle.putString(IAM, plainFriend);
							sendNotification(plainFriend, "A new user added you!");
						}
					}
					response.setData(bundle);
					if (outMessenger != null) outMessenger.send(response);
				}
			} catch(SocketTimeoutException e) {
				if (th.D) Log.e(TAG, "Socket timeout! "+e.getMessage());
			} catch (IOException e) {
				if (th.D) Log.e(TAG, e.getMessage());
			} catch (NumberFormatException e) {
				if (th.D) Log.e(TAG, e.getMessage());
			} catch (RemoteException e) {
				if (th.D) Log.e(TAG, e.getMessage());
			} finally {
				out.println("LOGOUT("+nickName+")");
				th.close(socket);
				db.close();
			}
		}
	};
	
    public void sendNotification(CharSequence contentTitle, CharSequence contentText) {
    	final String friend = db.getContactName(contentTitle.toString());
    	if (friend == null) return;
		if (ThreadHelper.isActivityVisible()) return;
		
		Intent notify = new Intent(this, Main.class);
		notify.setAction(Intent.ACTION_MAIN);
		notify.addCategory(Intent.CATEGORY_LAUNCHER);
			        
		PendingIntent intent = PendingIntent.getActivity(getBaseContext(), 0,
				notify, android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
		notifyDetails.setLatestEventInfo(this, friend, contentText, intent);
		notifyDetails.flags |= Notification.FLAG_AUTO_CANCEL;
		// vibrate on new notification
		notifyDetails.defaults |= Notification.DEFAULT_VIBRATE;
		notifyDetails.vibrate = new long[]{100, 200, 100, 500};
		// and turn on the status LED
		notifyDetails.flags |= Notification.FLAG_SHOW_LIGHTS;
		notifyDetails.ledARGB = Color.GREEN;
		notifyDetails.ledOffMS = 300;
		notifyDetails.ledOnMS = 300;
				
		mNotificationManager.notify(SIMPLE_NOTFICATION_ID, notifyDetails);
    }
}
