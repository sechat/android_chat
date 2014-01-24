package org.sechat.app.task;

/**
 * Copyright (c) 2014 Sechat GbR <support@sechat.org>
 *
 * You should have received a copy of the MIT License
 * along with this program (license.txt).
 * If not, see <http://sechat.github.io/license.txt>.
 */

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.packet.VCard;

import org.sechat.app.Encryption;
import org.sechat.app.ThreadHelper;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class SendMessage extends AsyncTask<String, String, String> {

	private static ThreadHelper th = new ThreadHelper();
	private static Encryption encryption = new Encryption();
	
	Activity act;
	String TAG = getClass().getName();
	
	public SendMessage(Activity act) {
		this.act = act;
	}
	
	@Override
	protected String doInBackground(String... params) {
		if (ThreadHelper.xmppConnection != null ||
    			ThreadHelper.xmppConnection.isAuthenticated()) {
			VCard vCard = new VCard();
			String user = th.getActiveChatUser();
			try {
				vCard.load(ThreadHelper.xmppConnection, user);
			} catch (XMPPException e) {
				return "Sending failed! Missing public key for user. Please try again later...";
			}
			Roster roster = ThreadHelper.xmppConnection.getRoster();
    		ChatManager chatmanager = ThreadHelper.xmppConnection.getChatManager();
    		Chat newChat = chatmanager.createChat(user, null);
    		try {
    			String encMsg = encryption.encrypt(
    					vCard.getField("pubkey"), params[0]);
    			newChat.sendMessage(encMsg);
    			th.addDiscussionEntry(act, user, params[0], true);
    		} catch (XMPPException e) {
    			if (th.D) Log.d(TAG, e.getMessage(), e);
    			return "Sending failed: "+e.getMessage();
    		} catch (NullPointerException e) {
    			if (th.D) Log.d(TAG, e.getMessage(), e);
    			return "Cannot fetch public key from other user!";
    		}
    		if (!roster.getPresence(user).isAvailable())
    			return "User offline! The message will be delivered later.";
    	} else return "You are not connected! Message was not delivered.";
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		if (result != null)
			th.sendNotification(act, result, Toast.LENGTH_LONG);
	}
}
