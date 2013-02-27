package de.zauberstuhl.sechat.activity;

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

import java.util.ArrayList;
import java.util.Calendar;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import de.zauberstuhl.sechat.R;
import de.zauberstuhl.sechat.ThreadHelper;
import de.zauberstuhl.sechat.User;
import de.zauberstuhl.sechat.adapter.UserAdapter;
import de.zauberstuhl.sechat.services.Listener;
import de.zauberstuhl.sechat.services.ListenerHandler;
import de.zauberstuhl.sechat.services.SubscriptionHandler;
import de.zauberstuhl.sechat.task.AddManualContacts;
import de.zauberstuhl.sechat.task.SearchContacts;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Messenger;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class UserList extends Activity {
	
	private static boolean isBound = false;
	
	private static ThreadHelper th = new ThreadHelper();
	private String TAG = getClass().getName();
	
	public static ArrayList<User> listItems;
	public static UserAdapter adapter;
	
	public static TextView contactInfoBox;
	public static Button addManual;
	public static Button addAuto;
	public static ProgressBar updateUserListBar;
	
    Handler handler = new Handler();
	Runnable updateUserList = new Runnable() {
		@Override
		public void run() {
			updateUserListBar.setVisibility(View.VISIBLE);
			th.updateUserList(UserList.this);
			handler.postDelayed(this, ThreadHelper.USERLIST_REPEAT_TIME);
		}
	};
	
    @Override
    public synchronized void onResume() {
        super.onResume();
        if (th.D) Log.e(TAG, "++ onResume ++");
        ThreadHelper.activityResumed();        
        if (!isBound) {
        	Intent service = new Intent(getBaseContext(), Listener.class);
        	Messenger messenger = new Messenger(new ListenerHandler(this));
            service.putExtra("MESSENGER", messenger);
            isBound = bindService(service, th.conn, Context.BIND_AUTO_CREATE);
        }
        handler.post(updateUserList);
    }
    
    @Override
    public synchronized void onPause() {
        super.onPause();
        if (th.D) Log.e(TAG, "++ onPause ++");
        ThreadHelper.activityPaused();
        if (isBound) {
        	unbindService(th.conn);
        	isBound = false;
        }
        handler.removeCallbacks(updateUserList);
    }
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (th.D) Log.e(TAG, "++ onCreate ++");
        setContentView(R.layout.userlist_activity);
        
        contactInfoBox = (TextView)findViewById(R.id.noContactHint);
        addManual = (Button)findViewById(R.id.addContactManual);
        addAuto = (Button)findViewById(R.id.addContacts);
        updateUserListBar = (ProgressBar)findViewById(R.id.updateUserListBar);
        
        /**
    	 * Start service listener
    	 */
        Intent service = new Intent(getBaseContext(), Listener.class);
        Calendar cal = Calendar.getInstance();
        PendingIntent pending = PendingIntent.getService(
        		this, 0, service, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
        		ThreadHelper.REPEAT_TIME, pending);
        
        /**
         * Get roster contacts and print them as user list
         */
        ListView myContacts = (ListView) findViewById(R.id.myContacts);
    	listItems = new ArrayList<User>();
    	
    	/**
    	 * Display a hint if the user has no contacts
    	 */
    	contactInfoBox.setVisibility(View.VISIBLE);
    	addManual.setVisibility(View.VISIBLE);
    	addAuto.setVisibility(View.VISIBLE);
    	    	
		adapter = new UserAdapter(this, listItems);
		myContacts.setAdapter(adapter);
		
		/**
		 * Add a packet listener for subscriptions
		 * to the XMPP connection
		 */
		ThreadHelper.xmppConnection.addPacketListener(new SubscriptionHandler(this), new PacketFilter() {
			public boolean accept(Packet aPacket) {
				if (aPacket instanceof Presence) {
					Presence p = (Presence) aPacket;
					return ((Presence.Type.subscribe.equals(p.getType())) 
						|| (Presence.Type.unsubscribe.equals(p.getType()))
						|| (Presence.Type.subscribed.equals(p.getType()))
						|| (Presence.Type.unsubscribed.equals(p.getType())));
				}
				return false;
			}
		});
	}
	
	public void addContact(View v) {
		new AddManualContacts(this).execute();
	}
	
	public void addContacts(View v) {
		new SearchContacts(this).execute();
	}
	
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_BACK) {
    		Intent intent = new Intent(Intent.ACTION_MAIN);
    		intent.addCategory(Intent.CATEGORY_HOME);
    		startActivity(intent);
    		return true;
    	}
    	return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.activity_userlist, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if (item.getItemId() == R.id.searchContacts) {
    		View v = new View(getBaseContext());
    		addContacts(v);
    		return true;
    	}
    	if (item.getItemId() == R.id.addContact) {
    		View v = new View(getBaseContext());
    		addContact(v);
    		return true;
    	}
    	return false;
    }
    
}