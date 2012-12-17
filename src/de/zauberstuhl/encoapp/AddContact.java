package de.zauberstuhl.encoapp;

import java.util.Iterator;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.ReportedData;
import org.jivesoftware.smackx.ReportedData.Row;
import org.jivesoftware.smackx.search.UserSearchManager;

import de.zauberstuhl.encoapp.adapter.DataBaseAdapter;
import de.zauberstuhl.encoapp.classes.Contact;
import de.zauberstuhl.encoapp.classes.User;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class AddContact extends AsyncTask<String, String, String> {

	private static ThreadHelper th = new ThreadHelper();
	private String TAG = th.appName+getClass().getName();

	Main main;
	Context context;
	XMPPConnection conn;
	
	public AddContact(Main main, XMPPConnection conn) {
		this.main = main;
		this.context = main.getBaseContext();
		this.conn = conn;
	}
	
	@Override
	protected String doInBackground(String... params) {
		DataBaseAdapter db = new DataBaseAdapter(context);
		String friend = params[0]+"@"+th.HOST;
		
		if (th.getNickName().equalsIgnoreCase(params[0]))
			return "You cannot add yourself to the contact list ;)";
		
		Roster roster = ThreadHelper.xmppConnection.getRoster();
		if (roster.contains(friend))
			return "This user is already in your contact list!";
		
		if (!userExist(params[0]))
			return "Username not found!";
		
		if (db.isset(friend))
			return "Error! User already exists in Database?";

		try {
			roster.createEntry(friend, friend, new String[] {});
		} catch (XMPPException e) {
			Log.e(TAG, "XMPPException on addContact class", e);
		}
		
		th.sendPublicKey(ThreadHelper.xmppConnection, db, friend);
		if (!db.isset(friend))
			db.addContact(new Contact(friend, null, null, null));
		
		publishProgress(friend);
		return null;
	}
	
	@Override
	protected void onPostExecute(String result) {
		if (result != null)
			th.sendNotification(main, result);
		main.addContactButton.setEnabled(true);
		main.addContactText.setEnabled(true);
	}

	@Override
	protected void onProgressUpdate(final String... params) {
		main.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				main.listItems.add(new User(params[0]));
				main.adapter.notifyDataSetChanged();
			}
		});
	}
	
	public boolean userExist(String user) {
		if (conn.isAuthenticated()) {
			String service = "search."+conn.getServiceName();
			UserSearchManager search = new UserSearchManager(conn);
			try {
				Form queryForm = search.getSearchForm(service);
                Form searchForm = queryForm.createAnswerForm();
                searchForm.setAnswer("Username", true);
                searchForm.setAnswer("search", user);
                ReportedData data = search.getSearchResults(searchForm, service);
                
                Log.e(TAG, "The Usernames from our each of our hits:");
                Iterator<Row> rows = data.getRows();
                while (rows.hasNext()) {
                   Row row = rows.next();

                   Iterator<String> jids = row.getValues("jid");
                   while (jids.hasNext())
                	   if (jids.next().equalsIgnoreCase(user+"@"+th.HOST))
                		   return true;
                }
			} catch (XMPPException e) {
				if (th.D) Log.e(TAG, e.getMessage(), e);
			}
		}
		return false;
	}
}
