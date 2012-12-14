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

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.XMPPException;
import de.zauberstuhl.encoapp.adapter.DataBaseAdapter;
import de.zauberstuhl.encoapp.adapter.MessageAdapter;
import de.zauberstuhl.encoapp.adapter.UserAdapter;
import de.zauberstuhl.encoapp.classes.User;
import de.zauberstuhl.encoapp.enc.Encryption;
import de.zauberstuhl.encoapp.services.Listener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Messenger;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class Main extends Activity {
	
	private static Encryption encryption = new Encryption();
	private static ThreadHelper th = new ThreadHelper();
	private static String TAG = th.appName+"Main";
		
    Button sendButton;
	public Button genButton, addContactButton;
    public ProgressBar genBar;
    public EditText genNickname;

	EditText msgTextField;
	public EditText addContactText;
    public TextView infoBox;
    public ListView myContacts;

	ListView msgBoard;
    
    public ViewFlipper viewFlipper;
    
    LinkedHashMap<String, String> msgListItems = new LinkedHashMap<String, String>();
	public ArrayList<User> listItems = new ArrayList<User>();
    public UserAdapter adapter;
    MessageAdapter msgAdapter;
    
    @Override
    public synchronized void onResume() {
        super.onResume();
        if (th.D) Log.e(TAG, "++ onResume ++");
        ThreadHelper.activityResumed();
        initialize();
    }
    
    @Override
    public synchronized void onPause() {
        super.onPause();
        if (th.D) Log.e(TAG, "++ onPause ++");
        ThreadHelper.activityPaused();
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (th.D) Log.e(TAG, "++ onCreate ++");
        setContentView(R.layout.activity_main);
        
        // switch between contacts and msgBoard
        viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        
        // chat window plus textfield and send-button
        msgTextField = (EditText) findViewById(R.id.msgTextField);
        sendButton = (Button) findViewById(R.id.sendButton);
        
        msgBoard = (ListView) findViewById(R.id.msgBoard);
        msgAdapter = new MessageAdapter(this, msgListItems);
        msgBoard.setAdapter(msgAdapter);
        msgBoard.setDivider(null);
        
        // registration part
        infoBox = (TextView) findViewById(R.id.infoBox);
        genNickname = (EditText) findViewById(R.id.genNickname);
        genButton = (Button) findViewById(R.id.genButton);
        genBar = (ProgressBar) findViewById(R.id.genBar);
        
        // initialize contact list and add database entries to it
        addContactButton = (Button) findViewById(R.id.addContactButton);
        addContactText = (EditText) findViewById(R.id.addContactText);
        myContacts = (ListView) findViewById(R.id.myContacts);
    }
    
    public void initialize() {
		/**
		 * Check if the device has actually a network connection
		 * if not, do not start the service and display
		 * the error message
		 */
    	boolean serverAlive = th.isOnline(getBaseContext());
    	if (!serverAlive) {
    		infoBox.setVisibility(View.VISIBLE);
    		infoBox.setText(Html.fromHtml(
		    		getResources().getString(R.string.maintenance)));
    		return;
    	}
    	/**
    	 * Start service listener
    	 */
    	Intent service = new Intent(getBaseContext(), Listener.class);
    	Messenger messenger = new Messenger(th.getListenerHandler(this));
        service.putExtra("MESSENGER", messenger);
        bindService(service, th.conn, Context.BIND_AUTO_CREATE);
    	getBaseContext().startService(service);
    	    	
    	DataBaseAdapter db = new DataBaseAdapter(this);
    	if (this.getBaseContext().getDatabasePath(
    			ThreadHelper.DATABASE).exists() && db.isset(0)) {
    		String user = db.getContactName(0);
    		th.setNickName(user);
    		ThreadHelper.ACCOUNT_NAME = user;
    		ThreadHelper.ACCOUNT_PASSWORD = db.getContactPassword();
    		setTitle("Welcome "+th.getNickName());
    		
    		listItems = db.getAllContacts();
    		adapter = new UserAdapter(this, listItems);
    		myContacts.setAdapter(adapter);
    		
    		infoBox.setVisibility(View.GONE);
    		myContacts.setVisibility(View.VISIBLE);
        	addContactButton.setVisibility(View.VISIBLE);
        	addContactText.setVisibility(View.VISIBLE);
        	genNickname.setVisibility(View.GONE);
        	genButton.setVisibility(View.GONE);
        	genBar.setVisibility(View.GONE);
    	} else register();
    	db.close();
    }
    
    public void register() {
		infoBox.setVisibility(View.VISIBLE);
    	if (Encryption.privateKey == null) {
    		infoBox.setText(Html.fromHtml(
    	    		getResources().getString(R.string.stepone)));
    		genNickname.setVisibility(View.GONE);
    	} else {
    		infoBox.setText(Html.fromHtml(
    	    		getResources().getString(R.string.steptwo)));
    		genNickname.setVisibility(View.VISIBLE);
    	}
    	genButton.setVisibility(View.VISIBLE);
    	genBar.setVisibility(View.VISIBLE);
    }
    
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(viewFlipper.getDisplayedChild() != 0){
               viewFlipper.showPrevious();
               setTitle("Welcome "+th.getNickName());
               return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void addContact(View v) {
    	final String friend = addContactText.getText().toString().trim();
    	addContactButton.setEnabled(false);
    	addContactText.setEnabled(false);
    	if (friend.length() > 0) {
    		new AddContact(this, ThreadHelper.xmppConnection).execute(friend);
    	} else {
    		th.sendNotification(this, "Please type a Nickname into the textfield!");
    		addContactButton.setEnabled(true);
    		addContactText.setEnabled(true);
    	}
    }
    
    public void generate(View v) {
    	String input = genNickname.getText().toString().trim();
    	genButton.setEnabled(false);
    	new Register(this, input).execute();
    }

    public void send(View v) {
        final String msg = msgTextField.getText().toString();
        msgTextField.setText("");
        if (msg.length() > 0) {
        	new Thread(new Runnable() {
        		@Override
				public void run() {
        			if (ThreadHelper.xmppConnection != null ||
                			ThreadHelper.xmppConnection.isAuthenticated()) {
        				DataBaseAdapter db = new DataBaseAdapter(Main.this);
                		String user = th.getActiveChatUser();
                		ChatManager chatmanager = ThreadHelper.xmppConnection.getChatManager();
                		Chat newChat = chatmanager.createChat(user, null);
                		try {
                			th.addDiscussionEntry(user, msg, true);
                			String encMsg = encryption.encrypt(
                					db.getPublicKey(user), msg);
                			newChat.sendMessage(encMsg);
        	    			th.updateChat(Main.this);
                		} catch (XMPPException e) {
                			th.sendNotification(Main.this, "Sending failed!");
                		}
                		db.close();
                	} else th.sendNotification(Main.this, "You are not connected!");
        		}
        	}).start();
        } else th.sendNotification(this, "Need message to send!");
    }
}