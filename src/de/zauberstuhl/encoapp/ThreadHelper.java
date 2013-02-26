package de.zauberstuhl.encoapp;

/**
 * Copyright (C) 2013 Lukas Matt <lukas@zauberstuhl.de>
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.provider.PrivacyProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.PrivateDataManager;
import org.jivesoftware.smackx.bytestreams.ibb.provider.CloseIQProvider;
import org.jivesoftware.smackx.bytestreams.ibb.provider.DataPacketProvider;
import org.jivesoftware.smackx.bytestreams.socks5.provider.BytestreamsProvider;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.packet.LastActivity;
import org.jivesoftware.smackx.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.packet.SharedGroupsInfo;
import org.jivesoftware.smackx.packet.VCard;
import org.jivesoftware.smackx.provider.AdHocCommandDataProvider;
import org.jivesoftware.smackx.provider.DataFormProvider;
import org.jivesoftware.smackx.provider.DelayInformationProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.provider.MUCAdminProvider;
import org.jivesoftware.smackx.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.provider.MUCUserProvider;
import org.jivesoftware.smackx.provider.MessageEventProvider;
import org.jivesoftware.smackx.provider.MultipleAddressesProvider;
import org.jivesoftware.smackx.provider.RosterExchangeProvider;
import org.jivesoftware.smackx.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.provider.XHTMLExtensionProvider;
import org.jivesoftware.smackx.search.UserSearch;

import de.zauberstuhl.encoapp.activity.MessageBoard;
import de.zauberstuhl.encoapp.activity.UserList;
import de.zauberstuhl.encoapp.adapter.DataBaseAdapter;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class ThreadHelper {
		
	public static XMPPConnection xmppConnection = null;
	public static String ACCOUNT_NAME = null;
	public static String ACCOUNT_PASSWORD = null;
	
	public final boolean D = true;
	public final String appName = "3nc0App";
	String TAG = getClass().getName();
	public final String HOST = "connect.3nc0.de";
	public final String IP = "188.40.178.248";
	public final int PORT = 5222;
	
	private static String activeChatUser = null;
	private static String nickName = null;
	private static boolean activityVisible;
	
	private static HashMap<String, Boolean> newMessages = new HashMap<String, Boolean>();
	
	/**
	 * Database params
	 */
	public final static int DATABASE_VERSION = 19;
	public final static String DATABASE = "3ncoApp";
	// table
	public final static String DB_USER_TABLE = "userTable";
	public final static String DB_HISTORY_TABLE = "userHistory";
	// rows
	public final static String DB_ID = "id";
	public final static String DB_NAME = "name";
	public final static String DB_ME = "me";
	public final static String DB_MESSAGE = "message";
	public final static String DB_DATE = "date";
	public final static String DB_PASSWORD = "password";
	public final static String DB_PRIVATE = "private";
	public final static String DB_PUBLIC = "public";
	
	/**
	 * Service
	 */
	public static final int REPEAT_TIME = 1000 * 360;
	public static final int USERLIST_REPEAT_TIME = 1000 * 10;
	public static Thread listenerThread = null;
	
	/////////////////////////////////////////////////////
	//	Starting some public function
	/////////////////////////////////////////////////////
	
	/**
	 * XMPP
	 * @throws XMPPException 
	 */
	public void xmppConnect() throws XMPPException {
		if (!(ThreadHelper.xmppConnection == null) &&
				ThreadHelper.xmppConnection.isConnected()) return;
		// Set configuration parameter
		Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.manual);
		ConnectionConfiguration config = new ConnectionConfiguration(HOST, PORT);
		config.setSASLAuthenticationEnabled(false);
		configure(ProviderManager.getInstance());
		ThreadHelper.xmppConnection = new XMPPConnection(config);
		ThreadHelper.xmppConnection.connect();
	}

	public boolean xmppLogin(Context context) {
		if (ThreadHelper.xmppConnection == null)
			return false;
		if (ThreadHelper.xmppConnection.isAuthenticated())
			return true;
		
		DataBaseAdapter db = new DataBaseAdapter(context);
		ThreadHelper.ACCOUNT_NAME = db.getName();
		ThreadHelper.ACCOUNT_PASSWORD = db.getPassword();
		db.close();
		
		try {
			ThreadHelper.xmppConnection.login(
					ThreadHelper.ACCOUNT_NAME, ThreadHelper.ACCOUNT_PASSWORD);
		} catch (XMPPException e) {
			Log.e(TAG, e.getMessage(), e);
			return false;
		}
		if (ThreadHelper.xmppConnection.isAuthenticated())
			return true;
		return false;
	}
	
	public boolean xmppConnectAndLogin(Context context) {
		try {
			xmppConnect();
		} catch (XMPPException e) {
			Log.e(TAG, e.getMessage(), e);
			return false;
		}
		if (xmppLogin(context))
			return true;
		return false;
	}
	
	/**
	 * Send notification to status bar
	 * 
	 * @param Context
	 * @param NotificationManager
	 * @param Notification
	 * @param CharSequence Title
	 * @param CharSequence Text
	 * @return void
	 */
	public void sendNotification(Context context, NotificationManager mNotificationManager,
			Notification notifyDetails, CharSequence contentTitle, CharSequence contentText) {
		// notify only if the app is in background
		if (!ThreadHelper.isActivityVisible()) {
			Intent notify = new Intent(context, MessageBoard.class);
			notify.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			notify.putExtra("activeChatUser", contentTitle);
			//notify.setAction(Intent.ACTION_MAIN);
			//notify.addCategory(Intent.CATEGORY_LAUNCHER);

			PendingIntent intent = PendingIntent.getActivity(context, 0,
					notify, android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
			notifyDetails.setLatestEventInfo(context, contentTitle, contentText, intent);
			notifyDetails.flags |= Notification.FLAG_AUTO_CANCEL;
			// vibrate on new notification
			notifyDetails.defaults |= Notification.DEFAULT_VIBRATE;
			notifyDetails.vibrate = new long[]{100, 200, 100, 500};
			// and turn on the status LED
			notifyDetails.flags |= Notification.FLAG_SHOW_LIGHTS;
			notifyDetails.ledARGB = Color.GREEN;
			notifyDetails.ledOffMS = 300;
			notifyDetails.ledOnMS = 300;

			mNotificationManager.notify(0, notifyDetails);
		}
    }
	
	/**
	 * Add a new chat message to the active chat
	 */
	public void addDiscussionEntry(Activity act, String user, String message, Boolean me) {
		if (D) Log.e(TAG, "Add new discussion entry to "+user);
        DataBaseAdapter db = new DataBaseAdapter(act.getBaseContext());
        db.addMessage(user, message, me);
        db.close();
		// update/refresh the message board
        if (getActiveChatUser() == null) {
        	hasUserNewMessages(user, true);
        }
		updateDiscussion(act);
	}
	
	public void updateDiscussion(Activity act) {
		if (getActiveChatUser() == null) return;
		DataBaseAdapter db = new DataBaseAdapter(act.getBaseContext());
        final LinkedList<Discussion> discussions = db.getMessagesFrom(getActiveChatUser());
        db.close();
        
		act.runOnUiThread(new Runnable(){
			@Override
			public void run() {
				MessageBoard.listItems.clear();
				MessageBoard.listItems.addAll(discussions);
				MessageBoard.msgAdapter.notifyDataSetChanged();
				MessageBoard.msgBoard.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
			}
		});
	}
	
	public boolean hasUserNewMessages(String jid) {
		if (newMessages.containsKey(jid))
			return newMessages.get(jid);
		return false;
	}
	
	public void hasUserNewMessages(String jid, Boolean result) {
		newMessages.put(jid, result);
	}
	
	public static boolean isActivityVisible() {
		return activityVisible;
	}
	
	public static void activityResumed() {
		activityVisible = true;
	}
	
	public static void activityPaused() {
		activityVisible = false;
	}
	
	public void setActiveChatUser(String input) {
		ThreadHelper.activeChatUser = input;
	}
	
	public String getActiveChatUser() {
		return ThreadHelper.activeChatUser;
	}
	
	public void setNickName(String input) {
		ThreadHelper.nickName = input;
	}
	
	public String getNickName() {
		String nick = ThreadHelper.nickName;
		if (nick == null)
			return "";
		return nick;
	}

	public String base64Encode(byte[] input) {
		//encoding  byte array into base 64
		return Base64.encodeToString(input, Base64.DEFAULT).replaceAll("\\n", "");
	}
		
	public byte[] base64Decode(String input) {
		//decoding byte array into base64
		return Base64.decode(input, Base64.DEFAULT);
	}
	
    public void sendNotification(Context context, String message) {
    	sendNotification(context, message, Toast.LENGTH_SHORT);
    }
    
    public void sendNotification(Context context, String message, int length) {
    	Toast.makeText(context, message, length).show();
    }
	
	public String getMd5Sum(String in) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
	        md5.reset();
	        md5.update(in.getBytes());
	        byte[] result = md5.digest();

	        StringBuffer hexString = new StringBuffer();
	        for (int i=0; i<result.length; i++) {
	        	if(result[i] <= 15 && result[i] >= 0){
	        		hexString.append("0");
	        	}
	        	hexString.append(Integer.toHexString(0xFF & result[i]));
	        }
	        return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			if (D) Log.e(TAG, e.getMessage());
		}
		return null;
    }
	
	public boolean isOnline(Context context) {
	    ConnectivityManager cm =
	        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnected()) {
	        return true;
	    }
	    return false;
	}
    
	public void updateUserList(Activity act, User user) {
		if (!ThreadHelper.xmppConnection.isAuthenticated()) {
			if (D) Log.d(TAG, "XMPP connection is not authenticated!");
			return;
		}
		if (UserList.listItems.contains(user)) {
			if (D) Log.d(TAG, "Update userlist failed! User already in list.");
			return;
		}
		ArrayList<User> list = new ArrayList<User>();;
		if (!UserList.listItems.isEmpty())
			list.addAll(UserList.listItems);
		list.add(user);
    	updateUserList(act, list);
	}
	
	public void updateUserList(Activity act) {
		if (!ThreadHelper.xmppConnection.isAuthenticated()) {
			if (D) Log.d(TAG, "XMPP connection is not authenticated!");
			return;
		}
		ArrayList<User> list = new ArrayList<User>();
		Roster roster = ThreadHelper.xmppConnection.getRoster();
    	Iterator<RosterEntry> cit = roster.getEntries().iterator();
    	while(cit.hasNext()) {
    		RosterEntry entry = cit.next();
    		VCard vCard = new VCard();
    		try {
				vCard.load(ThreadHelper.xmppConnection, entry.getUser());
			} catch (XMPPException e) {
				Log.e(TAG, e.getMessage(), e);
			}
    		list.add(new User(entry.getUser(), vCard.getNickName(),
    				roster.getPresence(entry.getUser()).isAvailable()));
    	}
    	updateUserList(act, list);
	}
	
	private void updateUserList(Activity act, final ArrayList<User> list) {
		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Integer setTo;
				UserList.listItems.clear();
				UserList.listItems.addAll(list);
				UserList.adapter.notifyDataSetChanged();
				if (D) Log.d(TAG, "User-list notify data set changed!");
				if (UserList.listItems.size() > 0)
					setTo = View.GONE;
				else setTo = View.VISIBLE;
				UserList.contactInfoBox.setVisibility(setTo);
				UserList.addAuto.setVisibility(setTo);
				UserList.addManual.setVisibility(setTo);
			}
		});
	}
	
	public ServiceConnection conn = new ServiceConnection() {
    	public void onServiceConnected(ComponentName className, IBinder binder) {
    		new Messenger(binder);
    	}

    	public void onServiceDisconnected(ComponentName className) {}
    };

	/** ******************************************** **/
	
	public void close(ObjectOutputStream oout) {
		try {
			if (D) Log.e(TAG, "Close socket!");
			oout.close();
		} catch (IOException e) {
			if (D) Log.e(TAG, e.getMessage());
		}
	}
	
	public void close(ObjectInputStream oin) {
		try {
			if (D) Log.e(TAG, "Close socket!");
			oin.close();
		} catch (IOException e) {
			if (D) Log.e(TAG, e.getMessage());
		}
	}

    /**
     * It seems it's a known issue: the smack.providers file,
     * usually in /META-INF folder in normal versions of smack,
     * can't be loaded in Android because its jar packaging.
     * So all the providers must be initialized by hand,
     * as shown in Mike Ryan's answer in this thread:
     * http://community.igniterealtime.org/message/201866#201866
     */
    public void configure(ProviderManager pm) {
    	// private data storage
    	pm.addIQProvider("query","jabber:iq:private",
    			new PrivateDataManager.PrivateDataIQProvider());
    	// time
        try {
        	pm.addIQProvider("query","jabber:iq:time",
        			Class.forName("org.jivesoftware.smackx.packet.Time"));
        } catch (ClassNotFoundException e) {
            Log.w(TAG, "Can't load class for org.jivesoftware.smackx.packet.Time");
        }
        // roster exchange
        pm.addExtensionProvider("x","jabber:x:roster", new RosterExchangeProvider());
        // message events
        pm.addExtensionProvider("x","jabber:x:event", new MessageEventProvider());
        // chat state
        pm.addExtensionProvider("active","http://jabber.org/protocol/chatstates",
        		new ChatStateExtension.Provider());
        pm.addExtensionProvider("composing","http://jabber.org/protocol/chatstates",
        		new ChatStateExtension.Provider());
        pm.addExtensionProvider("paused","http://jabber.org/protocol/chatstates",
        		new ChatStateExtension.Provider());
        pm.addExtensionProvider("inactive","http://jabber.org/protocol/chatstates",
        		new ChatStateExtension.Provider());
        pm.addExtensionProvider("gone","http://jabber.org/protocol/chatstates",
        		new ChatStateExtension.Provider());
        // XHTML
        pm.addExtensionProvider("html","http://jabber.org/protocol/xhtml-im",
        		new XHTMLExtensionProvider());
        // group chat invitations
        pm.addExtensionProvider("x","jabber:x:conference",
        		new GroupChatInvitation.Provider());
        // service discovery # Items
        pm.addIQProvider("query","http://jabber.org/protocol/disco#items",
        		new DiscoverItemsProvider());
        // service discovery # Info
        pm.addIQProvider("query","http://jabber.org/protocol/disco#info",
        		new DiscoverInfoProvider());
        // data forms
        pm.addExtensionProvider("x","jabber:x:data", new DataFormProvider());
        // MUC user
        pm.addExtensionProvider("x","http://jabber.org/protocol/muc#user",
        		new MUCUserProvider());
        // MUC admin
        pm.addIQProvider("query","http://jabber.org/protocol/muc#admin",
        		new MUCAdminProvider());
        // MUC owner    
        pm.addIQProvider("query","http://jabber.org/protocol/muc#owner",
        		new MUCOwnerProvider());
        // delayed delivery
        pm.addExtensionProvider("x","jabber:x:delay", new DelayInformationProvider());
        // version
        try {
            pm.addIQProvider("query","jabber:iq:version",
            		Class.forName("org.jivesoftware.smackx.packet.Version"));
        } catch (ClassNotFoundException e) {
        	Log.e(TAG, e.getMessage(), e);
        }
        // vCard
        pm.addIQProvider("vCard","vcard-temp", new VCardProvider());
        // offline message requests
        pm.addIQProvider("offline","http://jabber.org/protocol/offline",
        		new OfflineMessageRequest.Provider());
        // offline message indicator
        pm.addExtensionProvider("offline","http://jabber.org/protocol/offline",
        		new OfflineMessageInfo.Provider());
        // last activity
        pm.addIQProvider("query","jabber:iq:last", new LastActivity.Provider());
        // user search
        pm.addIQProvider("query","jabber:iq:search", new UserSearch.Provider());
        // sharedGroupsInfo
        pm.addIQProvider("sharedgroup","http://www.jivesoftware.org/protocol/sharedgroup",
        		new SharedGroupsInfo.Provider());
        // JEP-33: Extended Stanza Addressing
        pm.addExtensionProvider("addresses","http://jabber.org/protocol/address",
        		new MultipleAddressesProvider());
    	// file transfer
    	pm.addIQProvider("si","http://jabber.org/protocol/si", new StreamInitiationProvider());
    	pm.addIQProvider("query","http://jabber.org/protocol/bytestreams",
    			new BytestreamsProvider());
        pm.addIQProvider("open","http://jabber.org/protocol/ibb", new BytestreamsProvider());
        pm.addIQProvider("close","http://jabber.org/protocol/ibb", new CloseIQProvider());
        pm.addExtensionProvider("data","http://jabber.org/protocol/ibb",
        		new DataPacketProvider());
        // privacy
        pm.addIQProvider("query","jabber:iq:privacy", new PrivacyProvider());
        pm.addIQProvider("command", "http://jabber.org/protocol/commands",
        		new AdHocCommandDataProvider());
        pm.addExtensionProvider("malformed-action", "http://jabber.org/protocol/commands",
        		new AdHocCommandDataProvider.MalformedActionError());
        pm.addExtensionProvider("bad-locale", "http://jabber.org/protocol/commands",
        		new AdHocCommandDataProvider.BadLocaleError());
        pm.addExtensionProvider("bad-payload", "http://jabber.org/protocol/commands",
        		new AdHocCommandDataProvider.BadPayloadError());
        pm.addExtensionProvider("bad-sessionid", "http://jabber.org/protocol/commands",
        		new AdHocCommandDataProvider.BadSessionIDError());
        pm.addExtensionProvider("session-expired", "http://jabber.org/protocol/commands",
        		new AdHocCommandDataProvider.SessionExpiredError());
    }
}
