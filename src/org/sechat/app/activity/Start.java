package org.sechat.app.activity;

/**
 * Copyright (c) 2014 Sechat GbR <support@sechat.org>
 *
 * You should have received a copy of the MIT License
 * along with this program (license.txt).
 * If not, see <http://sechat.github.io/license.txt>.
 */

import org.sechat.app.R;
import org.sechat.app.ThreadHelper;
import org.sechat.app.adapter.DataBaseAdapter;
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
			Boolean existUser = db.issetUser();
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
