package org.sechat.app.services;

/**
 * Copyright (c) 2014 Sechat GbR <support@sechat.org>
 *
 * You should have received a copy of the MIT License
 * along with this program (license.txt).
 * If not, see <http://sechat.github.io/license.txt>.
 */

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.sechat.app.Encryption;
import org.sechat.app.R;
import org.sechat.app.ThreadHelper;
import org.sechat.app.adapter.DataBaseAdapter;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class Listener extends Service {
	
	private static ThreadHelper th = new ThreadHelper();
	private String TAG = getClass().getName();
	
	private Messenger outMessenger;
	
	public static boolean listenerRunning = true;
	public static final String ROSTER = "roster";
	public static final String ID = "id";
	public static final String MESSAGE = "message";
	
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
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int start) {
		if (th.D) Log.e(TAG, "++ ListenerOnStartCommand ++");
		if (th.xmppLogin(getBaseContext())) {
			if (ThreadHelper.listenerThread == null ||
					!ThreadHelper.listenerThread.isAlive()) {
				ThreadHelper.listenerThread = new Thread(messages);
				ThreadHelper.listenerThread.start();
			} else if (th.D) Log.d(TAG, "ListenerThread already started");
		}
		return Service.START_STICKY;
	}
	
	private Runnable messages = new Runnable() {
		@Override
		public void run() {
			if (th.D) Log.e(TAG, "++ ListenerThread started ++");
			PacketFilter filter = new AndFilter(new PacketTypeFilter(Message.class));
	        PacketCollector collector = ThreadHelper.xmppConnection.createPacketCollector(filter);
	        while (listenerRunning) {
	        	Bundle bundle = new Bundle();
				android.os.Message response = android.os.Message.obtain();
	            Packet packet = collector.nextResult();
	            if (packet instanceof Message) {
	                Message message = (Message) packet;
	                if (message != null && message.getBody() != null) {
	            		Encryption e = new Encryption();
	            		
	                    response.obj = MESSAGE;
	                    String user = packet.getFrom();
	                    String msg = message.getBody();
	                    
	                    if (th.D) Log.d(TAG, "Message received: "+msg);
	                    DataBaseAdapter db = new DataBaseAdapter(getBaseContext());
	            		msg = e.decrypt(db.getPrivateKey(), msg);
	            		db.close();
	                    
	                    user = user.replaceAll("^(.*?)\\/.*$", "$1"); 
						bundle.putString(ID, user);
						bundle.putString(MESSAGE, msg);
						
						th.sendNotification(Listener.this,
								mNotificationManager,
								notifyDetails, user, msg);
	                }
	            }
	            response.setData(bundle);
	            sendResponse(response);
	        }
			// Disconnect from the server
			ThreadHelper.xmppConnection.disconnect();
			if (th.D) Log.e(TAG, "++ ListenerThread stopped ++");
		}
	};
	
	void sendResponse(android.os.Message response) {
		try {
			if (outMessenger != null) outMessenger.send(response);
		} catch (RemoteException e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}
}
