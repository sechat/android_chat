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
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;
import de.zauberstuhl.encoapp.adapter.DataBaseAdapter;
import de.zauberstuhl.encoapp.adapter.MessageAdapter;
import de.zauberstuhl.encoapp.adapter.UserAdapter;
import de.zauberstuhl.encoapp.classes.Contact;
import de.zauberstuhl.encoapp.classes.User;
import de.zauberstuhl.encoapp.services.Listener;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
	
	private static boolean isBound;
		
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
	ArrayList<User> listItems = new ArrayList<User>();
    UserAdapter adapter;
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
        if (isBound) unbindService(th.conn);
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
    		infoBox.setText(
    				Html.fromHtml(getString(R.string.maintenance)));
    		return;
    	}
    	
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
        	/**
        	 * Start service listener
        	 */
            Intent service = new Intent(getBaseContext(), Listener.class);
            Messenger messenger = new Messenger(ListenerHandler);
            service.putExtra("MESSENGER", messenger);
            isBound = bindService(service, th.conn, Context.BIND_AUTO_CREATE);
            
            Calendar cal = Calendar.getInstance();
            PendingIntent pending = PendingIntent.getService(this, 0, service, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), ThreadHelper.REPEAT_TIME, pending);
    	} else register();
    	db.close();
    }
    
    public void register() {
		infoBox.setVisibility(View.VISIBLE);
    	if (Encryption.privateKey == null) {
    		infoBox.setText(
    				Html.fromHtml(getString(R.string.stepone)));
    		genNickname.setVisibility(View.GONE);
    	} else {
    		infoBox.setText(
    				Html.fromHtml(getString(R.string.steptwo)));
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
            // reset the active user
            if (th.getActiveChatUser() != null) {
            	th.setActiveChatUser(null);
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
    		new AddContact(this).execute(friend);
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
        	new SendMessage(this).execute(msg);
        } else th.sendNotification(this, "Need message to send!");
    }
    
	/**
	 * This is the Handler for our Listener service
	 */
    private Handler ListenerHandler = new Handler() {
    	@Override
    	public void handleMessage(Message message) {
    		Bundle data = message.getData();
    		Roster roster = ThreadHelper.xmppConnection.getRoster();
    		
    		if (message.arg1 == Listener.ROSTER) {
    			if (th.D) Log.e(TAG, "Intialize listItems!");
    			ArrayList<User> refreshedUserList = new ArrayList<User>();
    			Iterator<RosterEntry> ire = roster.getEntries().iterator();
    			while (ire.hasNext()) {
    				RosterEntry re = ire.next();
    				refreshedUserList.add(new User(re.getUser(),
    						roster.getPresence(re.getUser()).isAvailable()));
    			}
    			if (th.D) Log.e(TAG, "Data set changed on user adapater!");
    			listItems.clear();
    			listItems.addAll(refreshedUserList);
    			adapter.notifyDataSetChanged();
    		}
    		
    		if (message.arg1 == Activity.RESULT_OK && data != null) {
    			final DataBaseAdapter db = new DataBaseAdapter(Main.this);
    			String user = data.getString(Listener.ID);
    			String msg = data.getString(Listener.MESSAGE);
    			
    			if (message.arg2 == Listener.PUBKEY) {
    				if (th.D) Log.e(TAG, "Received public key from "+user);
    				try {
    					//if (!roster.contains(user))
    						roster.createEntry(user, user, new String[] {});
    				} catch (XMPPException e) {
    					Log.e(TAG, "XMPPException on Handler class", e);
    				}
    				
    				if (db.isset(user)) {
    					final Contact contact = new Contact(user, null, null, msg);
    					String pk = null;
    			        if ((pk = db.getPublicKey(user)) != null) {
    			        	String fp = th.getMd5Sum(pk);
    			        	String nfp = th.getMd5Sum(msg);
    			        	if (!fp.equals(nfp)) {
    							DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
    							    @Override
    							    public void onClick(DialogInterface dialog, int which) {
    							        if (which == DialogInterface.BUTTON_POSITIVE)
    							        	db.update(contact);
    							    }};
    							AlertDialog.Builder builder = new AlertDialog.Builder(getBaseContext());
    							builder.setMessage("Warning! The new requested public key "+
    			        				"has a different fingerprint!\n"+
    			        				"Do you accept the new one?\n\n"+nfp)
    								.setPositiveButton("Yes", dialogClickListener)
    								.setNegativeButton("No", dialogClickListener).show();
    			        	} else db.update(contact);
    			        } else db.update(contact);
    				} else {
    					Contact contact = new Contact(user, null, null, msg);
    					db.addContact(contact);
    					listItems.add(new User(user,
    							roster.getPresence(user).isAvailable()));
						adapter.notifyDataSetChanged();
						th.sendNotification(Main.this, "New user added!");
    				}
    			} else {
    				if (th.D) Log.e(TAG, "Received user message!");
    				if (!roster.contains(user))
    					new AddContact(Main.this).execute(user);
    				msg = encryption.decrypt(db.getPrivateKey(0), msg);
	    			th.addDiscussionEntry(user, msg, false);
	    			th.updateChat(Main.this);
    			}
    			db.close();
    		}
    	}
	};
}