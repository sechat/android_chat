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

import de.zauberstuhl.encoapp.adapter.DataBaseAdapter;
import de.zauberstuhl.encoapp.adapter.MessageAdapter;
import de.zauberstuhl.encoapp.adapter.UserAdapter;
import de.zauberstuhl.encoapp.async.AddContact;
import de.zauberstuhl.encoapp.async.GenerateAndRegister;
import de.zauberstuhl.encoapp.async.SendMessage;
import de.zauberstuhl.encoapp.async.services.Listener;
import de.zauberstuhl.encoapp.classes.User;
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
    
    LinkedHashMap<Integer, String> msgListItems = new LinkedHashMap<Integer, String>();
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
    	
        DataBaseAdapter db = new DataBaseAdapter(this);
        if (this.getBaseContext().getDatabasePath(
        		ThreadHelper.DATABASE).exists() && db.isset(0)) {
        	th.setNickName(db.getContactName(0));
        	setTitle("Welcome "+th.getNickName());
    		genNickname.setVisibility(View.GONE);
		    genButton.setVisibility(View.GONE);
		    genBar.setVisibility(View.GONE);
		    infoBox.setVisibility(View.GONE);
		    myContacts.setVisibility(View.VISIBLE);
		    addContactButton.setVisibility(View.VISIBLE);
		    addContactText.setVisibility(View.VISIBLE);
		    
		    listItems = db.getAllContacts();
	        adapter = new UserAdapter(this, listItems);
	    	myContacts.setAdapter(adapter);
	    	startListenerService();
        } else {
        	db.createDatabase();
        	genNickname.setVisibility(View.VISIBLE);
		    genButton.setVisibility(View.VISIBLE);
		    genBar.setVisibility(View.VISIBLE);
		    infoBox.setVisibility(View.VISIBLE);
		    myContacts.setVisibility(View.GONE);
		    addContactButton.setVisibility(View.GONE);
		    addContactText.setVisibility(View.GONE);
		    infoBox.setText(Html.fromHtml(
		    		getResources().getString(R.string.register)));
        }
    	db.close();
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
    
    public void startListenerService() {
    	Intent listener = new Intent(this, Listener.class);
        Messenger messenger = new Messenger(th.getListenerHandler(this));
        listener.putExtra("MESSENGER", messenger);
        bindService(listener, th.conn, Context.BIND_AUTO_CREATE);
        startService(listener);
    }

    public void addContact(View v) {
    	final String plainFriend = addContactText.getText().toString().trim();
    	addContactButton.setEnabled(false);
    	addContactText.setEnabled(false);
    	if (plainFriend.length() > 0)
    		new AddContact(this, false).execute(plainFriend);
    	else {
    		th.sendNotification(this, "Please type a Nickname into the textfield!");
    		addContactButton.setEnabled(true);
    		addContactText.setEnabled(true);
    	}
    }
    
    public void generate(View v) {
    	String input = genNickname.getText().toString().trim();
    	genButton.setEnabled(false);
    	if (input.length() > 0)
    		new GenerateAndRegister(this, input).execute();
    	else {
    		th.sendNotification(this, "Need Nickname for registration!");
            genButton.setEnabled(true);
    	}
    }

    public void send(View v) {
        final String msg = msgTextField.getText().toString();
        msgTextField.setText("");
        if (msg.length() > 0)
        	new SendMessage(this).execute(msg);
        else th.sendNotification(this, "Need message to send!");
    }
}