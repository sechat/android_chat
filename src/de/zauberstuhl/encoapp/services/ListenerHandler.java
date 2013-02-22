package de.zauberstuhl.encoapp.services;

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

import org.jivesoftware.smack.Roster;
import de.zauberstuhl.encoapp.ThreadHelper;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ListenerHandler extends Handler {
	
	private static ThreadHelper th = new ThreadHelper();
	private String TAG = getClass().getName(); 
	
	Activity act;
	
	public ListenerHandler(Activity act) {
		this.act = act;
	}
	
	@Override
	public void handleMessage(Message message) {
		Bundle data = message.getData();
		String request = (String)message.obj;
		
		if (request.equals(Listener.MESSAGE) && data != null) {
			Roster roster = ThreadHelper.xmppConnection.getRoster();
			String user = data.getString(Listener.ID);
			String msg = data.getString(Listener.MESSAGE);
			
			if (th.D) Log.d(TAG, "Received user message!");
			if (!roster.contains(user)) {
				//TODO: request new user 
			} else {
				th.addDiscussionEntry(act, user, msg, false);
	    	}
		} else if (request.equals(Listener.ROSTER) || request.equals(Listener.SUBSCRIPTION)) {
			try {
				th.updateUserList(act);
			} catch (XMPPException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
	}
}
