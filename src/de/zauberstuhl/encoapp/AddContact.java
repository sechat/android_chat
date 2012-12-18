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

import java.util.Iterator;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.ReportedData;
import org.jivesoftware.smackx.ReportedData.Row;
import org.jivesoftware.smackx.search.UserSearchManager;

import de.zauberstuhl.encoapp.adapter.DataBaseAdapter;
import de.zauberstuhl.encoapp.classes.Contact;
import de.zauberstuhl.encoapp.classes.User;
import android.os.AsyncTask;
import android.util.Log;

public class AddContact extends AsyncTask<String, String, String> {

	private static ThreadHelper th = new ThreadHelper();
	private String TAG = th.appName+getClass().getName();

	Main main;
	DataBaseAdapter db;
	
	public AddContact(Main main) {
		this.main = main;
		this.db = new DataBaseAdapter(main.getBaseContext());
	}
	
	@Override
	protected String doInBackground(String... params) {
		
		String friend = params[0]+"@"+th.HOST;
		
		if (th.getNickName().equalsIgnoreCase(params[0]))
			return "You cannot add yourself to the contact list ;)";
		
		Roster roster = ThreadHelper.xmppConnection.getRoster();
		if (roster.contains(friend))
			return "This user is already in your contact list!";
		
		if (!userExist(params[0]))
			return "Username not found!";
		
		//if (db.isset(friend))
			//return "Error! User already exists in Database?";

		try {
			roster.createEntry(friend, friend, new String[] {});
		} catch (XMPPException e) {
			Log.e(TAG, "XMPPException on addContact class", e);
		}
		
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
		db.close();
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
		if (ThreadHelper.xmppConnection.isAuthenticated()) {
			String service = "search."+ThreadHelper.xmppConnection.getServiceName();
			UserSearchManager search = new UserSearchManager(ThreadHelper.xmppConnection);
			try {
				Form queryForm = search.getSearchForm(service);
                Form searchForm = queryForm.createAnswerForm();
                searchForm.setAnswer("Username", true);
                searchForm.setAnswer("search", user);
                ReportedData data = search.getSearchResults(searchForm, service);
                
                Iterator<Row> rows = data.getRows();
                while (rows.hasNext()) {
                   Row row = rows.next();

                   Iterator<String> jids = row.getValues("jid");
                   while (jids.hasNext())
                	   if (jids.next().equalsIgnoreCase(user+"@"+th.HOST))
                		   return true;
                }
			} catch (XMPPException e) {
				if (th.D) Log.e(TAG, "XMPPException on addContact class", e);
			}
		}
		return false;
	}
}
