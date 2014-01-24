package org.sechat.app.services;

/**
 * Copyright (c) 2014 Sechat GbR <support@sechat.org>
 *
 * You should have received a copy of the MIT License
 * along with this program (license.txt).
 * If not, see <http://sechat.github.io/license.txt>.
 */

import org.jivesoftware.smack.Roster;
import org.sechat.app.ThreadHelper;
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
		}
	}
}
