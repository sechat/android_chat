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
import android.os.AsyncTask;

public class SendMessage extends AsyncTask<String, String, Void> {

	private static ThreadHelper th = new ThreadHelper();
	Main main;
	
	public SendMessage(Main main) {
		this.main = main;
	}
	
	@Override
	protected Void doInBackground(String... data) {
		DataBaseAdapter db = new DataBaseAdapter(main);
		String me = th.getMd5Sum(db.getContactName(0));
		String plainFriend = th.getActiveChatUser();
		final String message = data[0];		
		if (!th.sendMessage(db, me, plainFriend, message)){
			onProgressUpdate("Encrypting the message failed! Retrying it ...");
			//th.receivePubKey(th.getMd5Sum(plainFriend));
			if (!th.sendMessage(db, me, plainFriend, message)) {
				onProgressUpdate("Encryption failed again! Message was not delivered!");
				return null;
			}
		}
		
		main.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				th.addDiscussionEntry(th.getMd5Sum(
						th.getActiveChatUser()), message, true);
				th.updateChat(main);
			}
		});
		db.close();
		return null;
	}
	
	@Override
	protected void onProgressUpdate(final String... data) {
		main.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				th.sendNotification(main, data[0]);
			}
		});
     }

}
