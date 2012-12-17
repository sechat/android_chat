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

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.provider.ProviderManager;
import de.zauberstuhl.encoapp.Main;
import de.zauberstuhl.encoapp.R;
import de.zauberstuhl.encoapp.ThreadHelper;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class Listener extends Service {
	
	private static ThreadHelper th = new ThreadHelper();
	private String TAG = th.appName+getClass().getName();
	
	private Messenger outMessenger;
	private ConnectionConfiguration config;
	private static boolean userListRunning = false;
	
	public static final int PUBKEY = 666;
	public static final int ROSTER = 667;
	public static final String ID = "id";
	public static final String MESSAGE = "message";
	
	static int SIMPLE_NOTFICATION_ID = 0;
	NotificationManager mNotificationManager;
	Notification notifyDetails;
	
	public Listener() {
		super.onCreate();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		if (th.D) Log.e(TAG, "++ ListenerOnBind ++");
		Bundle extras = intent.getExtras();
		if (extras != null) outMessenger = (Messenger) extras.get("MESSENGER");
		return null;
	}
	
	@Override
	public void onCreate() {
		if (th.D) Log.e(TAG, "++ ListenerOnCreate ++");
		this.mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		this.notifyDetails = new Notification(R.drawable.ic_launcher,
				"New message!", System.currentTimeMillis());
		/**
		 * Set configuration parameter
		 */
		Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.accept_all);
		config = new ConnectionConfiguration(th.HOST, th.PORT);
		config.setSASLAuthenticationEnabled(false);
		th.configure(ProviderManager.getInstance());
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int start) {
		if (th.D) Log.e(TAG, "++ ListenerOnStartCommand ++");
		if (ThreadHelper.ACCOUNT_NAME != null &&
				ThreadHelper.ACCOUNT_PASSWORD != null) {
			th.cancelListener(false);
			new Thread(messages).start();
		}
		return Service.START_STICKY;
	}
	
	private Runnable messages = new Runnable() {
		@Override
		public void run() {
			if (th.D) Log.e(TAG, "++ ListenerMessages started ++");
			ThreadHelper.xmppConnection = new XMPPConnection(config);
			try {
				ThreadHelper.xmppConnection.connect();
				ThreadHelper.xmppConnection.login(
						ThreadHelper.ACCOUNT_NAME, ThreadHelper.ACCOUNT_PASSWORD);
			} catch (XMPPException e) {
				if (th.D) Log.e(TAG, "Exit. Cannot login!");
				return;
			}
			
			PacketFilter filter = new AndFilter(new PacketTypeFilter(Message.class));
	        PacketCollector collector = ThreadHelper.xmppConnection.createPacketCollector(filter);
	        while (!th.isListenerCancelled()) {
	        	android.os.Message response =
	        			android.os.Message.obtain();
	        	Bundle bundle = new Bundle();
	        	if (!userListRunning)
	            	new Thread(userlist).start();
	            Packet packet = collector.nextResult();
	            if (packet instanceof Message) {
	                Message message = (Message) packet;
	                if (message != null && message.getBody() != null) {
	                    response.arg1 = Activity.RESULT_OK;
	                    String user = packet.getFrom();
	                    String msg = message.getBody();
	                    
	                    user = user.replaceAll("^(.*?)\\/.*$", "$1"); 
						bundle.putString(ID, user);
						
						Log.e(TAG, "Debugger: "+msg);
						
						if (msg.startsWith("((PUBLICKEY))")) {
							response.arg2 = PUBKEY;
							msg = msg.substring(13, msg.length());
						} else sendNotification(user, msg);
						bundle.putString(MESSAGE, msg);
	                }
	            }
	            response.setData(bundle);
	            sendUpdate(response);
	        }
			// Disconnect from the server
			ThreadHelper.xmppConnection.disconnect();
		}
	};
		
	private Runnable userlist = new Runnable() {
		@Override
		public void run() {
			if (th.D) Log.e(TAG, "++ ListenerUserList started ++");
			userListRunning = true;
			while (!th.isListenerCancelled() &&
					ThreadHelper.isActivityVisible()) {
				final android.os.Message response =
	        			android.os.Message.obtain();
				response.arg1 = ROSTER;
				if (th.D) Log.e(TAG, "Sending results!");
	            sendUpdate(response);
	            try {
					Thread.sleep(ThreadHelper.REFRESH_USER_LIST);
				} catch (InterruptedException e) {
					if (th.D) Log.e(TAG, "Interrupt exception on user list thread!", e);
				}
			}
			userListRunning = false;
		}
	};
	
	private void sendUpdate(android.os.Message response) {
		if (outMessenger != null)
			try {
				outMessenger.send(response);
			} catch (RemoteException e) {
				if (th.D) Log.e(TAG, "Main thread wasn't created yet... ");
			}
	}
	
	public void sendNotification(CharSequence contentTitle, CharSequence contentText) {
		if (ThreadHelper.isActivityVisible()) return;

		Intent notify = new Intent(this, Main.class);
		notify.setAction(Intent.ACTION_MAIN);
		notify.addCategory(Intent.CATEGORY_LAUNCHER);

		PendingIntent intent = PendingIntent.getActivity(getBaseContext(), 0,
				notify, android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
		notifyDetails.setLatestEventInfo(this, contentTitle, contentText, intent);
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