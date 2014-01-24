package org.sechat.app.adapter;

/**
 * Copyright (c) 2014 Sechat GbR <support@sechat.org>
 *
 * You should have received a copy of the MIT License
 * along with this program (license.txt).
 * If not, see <http://sechat.github.io/license.txt>.
 */

import java.util.ArrayList;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.LastActivityManager;
import org.jivesoftware.smackx.packet.LastActivity;

import org.sechat.app.R;
import org.sechat.app.ThreadHelper;
import org.sechat.app.User;
import org.sechat.app.activity.MessageBoard;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class UserAdapter extends BaseAdapter {

	private static ThreadHelper th = new ThreadHelper();
	
	String TAG = getClass().getName();
	Activity act;
	Context context;
    ArrayList<User> data;
    static LayoutInflater inflater = null;
    
    public UserAdapter(Activity act, ArrayList<User> data) {
    	this.act = act;
    	/**
    	 * There is a bug in several Android versions,
    	 * which returns null if you call getBaseContext from a Activity
    	 * This problem exists only for the AlertDialog function
    	 */
    	this.context = act; // workaround for AlertDialog
        this.data = data;
        inflater = (LayoutInflater) act.getBaseContext().getSystemService(
        		Context.LAYOUT_INFLATER_SERVICE);
    }

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        
        if (vi == null) vi = inflater.inflate(R.layout.user_adapter, null);
        
        RelativeLayout list = (RelativeLayout)vi.findViewById(R.id.userList);
        TextView text = (TextView)vi.findViewById(R.id.userTitle);
        ImageView status = (ImageView)vi.findViewById(R.id.userStatus);
        final ImageView newMessage = (ImageView)vi.findViewById(R.id.newMessageView);
        TextView lastActivity = (TextView)vi.findViewById(R.id.lastActivity);
        
        final User user = data.get(position);
        list.setOnClickListener(new OnClickListener() {
        	@Override
    		public void onClick(View arg0) {
    			String keyName = user.jid;
    			if (th.D) Log.e(TAG, "Switch to user chat: "+keyName);
    			th.setActiveChatUser(keyName);
    			th.hasUserNewMessages(keyName, false);
    			// switch to the message board
    			Intent intent = new Intent(context, MessageBoard.class);
    			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    			context.startActivity(intent);
    		}
        });
        list.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View arg0) {
				if (ThreadHelper.xmppConnection != null &&
						ThreadHelper.xmppConnection.isAuthenticated()) {
					DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
					    @Override
					    public void onClick(DialogInterface dialog, int which) {
					        if (which == DialogInterface.BUTTON_POSITIVE) {
					        	Roster roster = ThreadHelper.xmppConnection.getRoster();
								if (roster.contains(user.jid)) {
									RosterEntry entry = roster.getEntry(user.jid);
									try {
										roster.removeEntry(entry);
									} catch (XMPPException e) {
										Log.e(TAG, "Failed removing user from roster!", e);
									}
									th.updateUserList(act);
								}
					        }
					    }
					};
					
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setMessage("Do you want to remove '" + user.jid + "'?")
						.setPositiveButton("Yes", dialogClickListener)
						.setNegativeButton("No", dialogClickListener).show();
				}
				return true;
			}
        });
        
        if (user.online) status.setImageResource(R.drawable.online_icon);
        else status.setImageResource(R.drawable.offline_icon);
        if (user.name != null)
        	text.setText(user.name);
        else text.setText(user.jid);
        
        if (th.hasUserNewMessages(user.jid))
        	newMessage.setVisibility(View.VISIBLE);
        else newMessage.setVisibility(View.GONE);
        
        try {
			LastActivity activty = LastActivityManager.getLastActivity(
					ThreadHelper.xmppConnection, user.jid);
			String result;
			if (activty.lastActivity > 3600)
				result = (activty.lastActivity / 3600) + " hours";
			else if (activty.lastActivity > 60)
				result = (activty.lastActivity / 60) + " minutes";
			else result = activty.lastActivity + " seconds";
			lastActivity.setText("Idle since " + result);
		} catch (XMPPException e) {
			if (th.D) Log.e(TAG, e.getMessage());
			lastActivity.setText("Idle not available, yet");
		}
        return vi;
    }
}
