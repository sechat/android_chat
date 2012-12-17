package de.zauberstuhl.encoapp;

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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
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

import de.zauberstuhl.encoapp.adapter.DataBaseAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Base64;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

public class ThreadHelper {
		
	public static XMPPConnection xmppConnection = null;
	public static String ACCOUNT_NAME = null;
	public static String ACCOUNT_PASSWORD = null;
	
	public final boolean D = true;
	public final String appName = "3nc0App";
	String TAG = appName+getClass().getName();
	public final String HOST = "connect.3nc0.de";
	public final String IP = "188.40.178.248";
	public final int PORT = 5222;
	
	/**
	 * To track the user messages
	 * no nice but it works :S
	 */
	private static String activeChatUser = null;
	private static String nickName = null;
	private static boolean activityVisible;
	
	private static HashMap<String,LinkedHashMap<String, String>> userDiscussion =
		new HashMap<String,LinkedHashMap<String, String>>();
	
	public final String USER_ID = "10";
	public final String MY_ID = "11";
	
	/**
	 * Database params
	 */
	public final static int DATABASE_VERSION = 15;
	public final static String DATABASE = "3ncoApp";
	public final static String DB_TABLE = "userTable";
	public final static String DB_ID = "id";
	public final static String DB_NAME = "name";
	public final static String DB_PASSWORD = "password";
	public final static String DB_PRIVATE = "private";
	public final static String DB_PUBLIC = "public";
	
	/**
	 * Service
	 */
	private static boolean cancelListener = false;
	public void cancelListener(boolean status) {
		ThreadHelper.cancelListener = status;
	}
	public boolean isListenerCancelled() {
		return ThreadHelper.cancelListener;
	}
	public static final int REPEAT_TIME = 1000 * 320;
	public static final int REFRESH_USER_LIST = 1000 * 10;
	
	/////////////////////////////////////////////////////
	//	Starting some public function
	/////////////////////////////////////////////////////
	
	/**
	 * Send your public key
	 * to the user who request it
	 */
	public boolean sendPublicKey(final XMPPConnection conn, DataBaseAdapter db, String user) {
		/**
		 * At the moment I have a view problems with smack.
		 * If I get file transfer working I will handle that nicer!!!
		 */
		if (ThreadHelper.xmppConnection != null ||
    			ThreadHelper.xmppConnection.isAuthenticated()) {
    		String publicKey = db.getPublicKey(0);
    		ChatManager chatmanager = ThreadHelper.xmppConnection.getChatManager();
    		Chat newChat = chatmanager.createChat(user, null);
    		try {
    			newChat.sendMessage("((PUBLICKEY))"+publicKey);
    			return true;
    		} catch (XMPPException e) {
    			if (D) Log.e(TAG, e.getMessage(), e);
    		}
    	}
		return false;
	}
	
	/**
	 * Add a new chat message to the active chat
	 */
	public void addDiscussionEntry(String user, String message, Boolean me) {
        Date date = new Date();
        String hours = String.valueOf(date.getHours());
        String minutes = String.valueOf(date.getMinutes());
		LinkedHashMap<String, String> map = ThreadHelper.userDiscussion.get(user);
		if (map == null) map = new LinkedHashMap<String, String>();
		String identifier = String.valueOf(map.size());
		if (me) identifier = MY_ID + hours + minutes + identifier;
		else identifier = USER_ID + hours + minutes + identifier;
		
		if (D) Log.e(TAG, "discussion user: "+user);
		
		map.put(identifier, message);
		ThreadHelper.userDiscussion.put(user, map);
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
	
	public LinkedHashMap<String, String> getUserDiscussion(String user) {
		return ThreadHelper.userDiscussion.get(user);
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
    
	public void updateChat(final Main main) {
		main.runOnUiThread(new Runnable(){
			@Override
			public void run() {
				if (getActiveChatUser() == null) return;
				LinkedHashMap<String, String> chatLog =
					getUserDiscussion(getActiveChatUser());
				if (chatLog == null) chatLog = new LinkedHashMap<String, String>();
				
				main.msgListItems.clear();
				main.msgListItems.putAll(chatLog);
				main.msgAdapter.notifyDataSetChanged();
				main.msgBoard.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
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
