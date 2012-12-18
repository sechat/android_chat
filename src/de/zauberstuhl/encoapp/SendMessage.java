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

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPException;

import de.zauberstuhl.encoapp.adapter.DataBaseAdapter;
import android.os.AsyncTask;
import android.widget.Toast;

public class SendMessage extends AsyncTask<String, String, String> {

	private static ThreadHelper th = new ThreadHelper();
	private static Encryption encryption = new Encryption();
	
	Main main;
	DataBaseAdapter db;
	
	public SendMessage(Main main) {
		this.main = main;
		this.db = new DataBaseAdapter(main);
	}
	
	@Override
	protected String doInBackground(String... params) {
		if (ThreadHelper.xmppConnection != null ||
    			ThreadHelper.xmppConnection.isAuthenticated()) {
			String user = th.getActiveChatUser();
			if (db.getPublicKey(user) == null)
				return "Sending failed! Missing public key for user. Please try again later...";
			Roster roster = ThreadHelper.xmppConnection.getRoster();
    		ChatManager chatmanager = ThreadHelper.xmppConnection.getChatManager();
    		Chat newChat = chatmanager.createChat(user, null);
    		try {
    			th.addDiscussionEntry(user, params[0], true);
    			String encMsg = encryption.encrypt(
    					db.getPublicKey(user), params[0]);
    			newChat.sendMessage(encMsg);
    			th.updateChat(main);
    		} catch (XMPPException e) {
    			return "Sending failed: "+e.getMessage();
    		}
    		if (!roster.getPresence(user).isAvailable())
    			return "User offline! The message will be delivered later.";
    	} else return "You are not connected! Message was not delivered.";
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		if (result != null)
			th.sendNotification(main, result, Toast.LENGTH_LONG);
		db.close();
	}
}
