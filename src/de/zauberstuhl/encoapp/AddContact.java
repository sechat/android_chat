package de.zauberstuhl.encoapp;

import java.util.Iterator;

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

public class AddContact extends AsyncTask<String, String, Void> {

	private static ThreadHelper th = new ThreadHelper();
	private String TAG = th.appName+getClass().getName();

	Main main;
	Context context;
	XMPPConnection conn;
	Boolean success = false;
	
	public AddContact(Main main, XMPPConnection conn) {
		this.main = main;
		this.context = main.getBaseContext();
		this.conn = conn;
	}
	
	@Override
	protected Void doInBackground(String... params) {
		DataBaseAdapter db = new DataBaseAdapter(context);
		String friend = params[0]+"@"+th.HOST;
		if (!userExist(params[0])) {
			publishProgress("Username not found!");
			return null;
		}
		
		if (!db.isset(friend)) {
			success = true;
			db.addContact(new Contact(friend, null, null, null));
			th.sendPublicKey(ThreadHelper.xmppConnection, db, friend);
		}
		publishProgress(friend);
		db.close();
		return null;
	}

	@Override
	protected void onProgressUpdate(final String... params) {
		main.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (success) {
					main.listItems.add(new User(params[0]));
					main.adapter.notifyDataSetChanged();
				} else th.sendNotification(main, params[0]);
				main.addContactButton.setEnabled(true);
				main.addContactText.setEnabled(true);
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
