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

import java.io.IOException;
import java.io.PrintWriter;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLSocket;

import de.zauberstuhl.encoapp.Main;
import de.zauberstuhl.encoapp.ThreadHelper;
import de.zauberstuhl.encoapp.adapter.DataBaseAdapter;
import de.zauberstuhl.encoapp.enc.Encryption;
import android.os.AsyncTask;
import android.util.Log;

public class SendMessage extends AsyncTask<String, String, Void> {

	private static ThreadHelper th = new ThreadHelper();
	private static Encryption encryption = new Encryption();
	private static String TAG = th.appName+"SendMessage";
	
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
		if (!sendMessage(db, me, plainFriend, message)){
			onProgressUpdate("Encrypting the message failed! Retrying it ...");
			//th.receivePubKey(th.getMd5Sum(plainFriend));
			if (!sendMessage(db, me, plainFriend, message)) {
				onProgressUpdate("Encryption failed again! Message was not delivered!");
				return null;
			}
		}
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
	
	private boolean sendMessage(DataBaseAdapter db, String me, String friend, String message) {
		SSLSocket socket = null;
	    try {
	    	socket = th.getConnection(db);
	    		
	    	String publicKey = db.getPublicKey(friend);
	    	PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            if (th.D) Log.e(TAG, "Sending message to "+friend);
            if (th.D) Log.e(TAG, "PublicKey: "+publicKey);
            message = encryption.encrypt(publicKey, message);
            if (message != null) {
            	out.println("MSG("+me+","+th.getMd5Sum(friend)+","+message+")");
            	return true;
            }
	    } catch(NullPointerException e) {
	    	Log.e(TAG, e.getMessage());
	    } catch (IOException e) {
            Log.e(TAG, e.getMessage());
		} catch (KeyManagementException e) {
			Log.e(TAG, e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, e.getMessage());
		} finally {
			th.close(socket);
		}
		return false;
	}
}
