package de.zauberstuhl.encoapp.async;

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

import de.zauberstuhl.encoapp.Main;
import de.zauberstuhl.encoapp.ThreadHelper;
import de.zauberstuhl.encoapp.adapter.DataBaseAdapter;
import de.zauberstuhl.encoapp.classes.Contact;
import de.zauberstuhl.encoapp.classes.User;
import android.os.AsyncTask;

public class AddContact extends AsyncTask<String, String, Void> {

	private static ThreadHelper th = new ThreadHelper();
	
	Main main;
	Boolean success = false;
	Boolean listener;
	
	public AddContact(Main main, Boolean listener) {
		this.main = main;
		this.listener = listener;
	}
	
	@Override
	protected Void doInBackground(String... params) {
		DataBaseAdapter db = new DataBaseAdapter(main);
		String plainFriend = params[0];
		String me = db.getContactName(0);
		String friend = th.getMd5Sum(plainFriend);
		String result = th.exec(null, "USER("+friend+")");
		
		if (result.equalsIgnoreCase("1")) {
			success = true;
			byte[] pubKey = th.receivePubKey(db, friend);
			if (!listener) th.exec(db, "IAM("+me+","+friend+")");
			if (!db.isset(friend))
				db.addContact(new Contact(plainFriend, null, th.base64Encode(pubKey)));
			publishProgress(plainFriend);
		} else publishProgress("Username not found!");
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
}
