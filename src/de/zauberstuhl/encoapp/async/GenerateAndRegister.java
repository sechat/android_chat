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
import de.zauberstuhl.encoapp.enc.Encryption;
import android.os.AsyncTask;

public class GenerateAndRegister extends AsyncTask<Void, Integer, String> {
	
	private static ThreadHelper th = new ThreadHelper();
	private static Encryption encryption = new Encryption();
	
	Main main;
	String nickName;
	
	public GenerateAndRegister(Main main, String nickName) {
		this.main = main;
		this.nickName = nickName;	
	}
	
	@Override
	protected String doInBackground(Void... params) {
		DataBaseAdapter db = new DataBaseAdapter(main);
		String md5NickName = th.getMd5Sum(nickName);
		publishProgress(10);

		String check = th.exec(null, "USER("+md5NickName+")");
		if (check == null)
			return "Read timeout. Please try again!";
		if (check.equalsIgnoreCase("1"))
			return "Nickname already exists!";		
		publishProgress(20);
		
		encryption.generateKeyPair();
		publishProgress(60);
		
		db.clearTable(); // drop table and create a new one
		db.addContact(new Contact(0, nickName,
				new String(Encryption.privateKey),
				new String(Encryption.publicKey)));
		publishProgress(80);
		
		th.sendPubKey(null, md5NickName, new String(Encryption.publicKey));
		publishProgress(100);
		db.close();
		return null;
	}
	
	@Override
	protected void onProgressUpdate(Integer... progress) {
		main.genBar.setProgress(progress[0]);
    }
	
	@Override
    protected void onPostExecute(String result) {
		if (result != null)
			th.sendNotification(main.getBaseContext(), result);
		else main.initialize();
		main.genButton.setEnabled(true);
		publishProgress(0);
    }
}
