package de.zauberstuhl.encoapp;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.ReportedData;
import org.jivesoftware.smackx.search.UserSearchManager;

import de.zauberstuhl.encoapp.adapter.DataBaseAdapter;
import de.zauberstuhl.encoapp.classes.Contact;
import de.zauberstuhl.encoapp.classes.User;
import android.os.AsyncTask;
import android.util.Log;

public class AddContact extends AsyncTask<String, String, Void> {

	private static ThreadHelper th = new ThreadHelper();
	private String TAG = th.appName+getClass().getName();

	Main main;
	XMPPConnection conn;
	Boolean success = false;

	public AddContact(Main main, XMPPConnection conn) {
		this.main = main;
		this.conn = conn;
	}

	@Override
	protected Void doInBackground(String... params) {
		DataBaseAdapter db = new DataBaseAdapter(main);
		String friend = params[0]+"@"+th.HOST;
		if (!userExist(friend)) {
			publishProgress("Username not found!");
			return null;
		}
		
		if (!db.isset(friend)) {
			success = true;
			db.addContact(new Contact(friend, null, null, null));
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
		                        
		        if (data.getRows() != null) return true;
		        else return false;
			} catch (XMPPException e) {
				if (th.D) Log.e(TAG, e.getMessage(), e);
				return false;
			}
		} else return false;
	}
}
