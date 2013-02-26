package de.zauberstuhl.encoapp.activity;

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

import de.zauberstuhl.encoapp.R;
import de.zauberstuhl.encoapp.ThreadHelper;
import de.zauberstuhl.encoapp.adapter.DataBaseAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Start extends Activity {

	private static ThreadHelper th = new ThreadHelper();
	private String TAG = getClass().getName();
	
	TextView startHint;
	ProgressBar startProgress;
	
	final int NOLOGIN = 100;
	final int LOGGEDIN = 101;
	final int NODBENTRY = 102;
	
	Runnable startup = new Runnable() {
		@Override
		public void run() {
			Message msg = new Message();
			DataBaseAdapter db = new DataBaseAdapter(Start.this);
			Boolean existUser = db.isset(0);
			db.close();
			
        	if (existUser) {
        		if (th.xmppConnectAndLogin(Start.this))
        			msg.obj = LOGGEDIN;
        		else msg.obj = NOLOGIN;
        	} else msg.obj = NODBENTRY;
        	handler.sendMessage(msg);
		}
	};
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (th.D) Log.e(TAG, "++ onCreate ++");
        setContentView(R.layout.start_activity);
        
        startHint = (TextView) findViewById(R.id.start_hint);
        startProgress = (ProgressBar) findViewById(R.id.start_progress);
	}
	
	@Override
	public synchronized void onResume() {
		super.onResume();
		if (th.D) Log.e(TAG, "++ onResume ++");
		startProgress.setVisibility(View.VISIBLE);
		startHint.setText(getString(R.string.loading_hint));
		
        /**
		 * Check if the device has actually a network connection
		 * if not, do not start the service and display
		 * the error message
		 */
    	boolean serverAlive = th.isOnline(getBaseContext());
    	if (!serverAlive) {
    		startProgress.setVisibility(View.GONE);
    		startHint.setText(getString(R.string.no_network));
    	} else new Thread(startup).start(); // establish a connection
	}
	
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Intent intent = null;
			Integer status = (Integer)msg.obj;
			
			if (status == LOGGEDIN) {
				intent = new Intent(Start.this, UserList.class);
			} else if (status == NOLOGIN) {
				startProgress.setVisibility(View.GONE);
        		startHint.setText(getString(R.string.cannot_login));
			} else if (status == NODBENTRY) {
				intent = new Intent(Start.this, Setup.class);
			}
			
			if (intent != null) {
				intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
			}
		}
	};
}
