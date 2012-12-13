package de.zauberstuhl.encoapp;

import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.provider.ProviderManager;

import de.zauberstuhl.encoapp.adapter.DataBaseAdapter;
import de.zauberstuhl.encoapp.classes.Contact;
import de.zauberstuhl.encoapp.enc.Encryption;
import android.os.AsyncTask;
import android.util.Log;

public class Register extends AsyncTask<Void, Integer, String> {

	private static ThreadHelper th = new ThreadHelper();
	private String TAG = th.appName+getClass().getName();
	private static Encryption encryption = new Encryption();

	Main main;
	String _user;
	String _password;

	public Register(Main main, String user) {
		this.main = main;
		this._user = user;
	}

	@Override
	protected String doInBackground(Void... params) {
		DataBaseAdapter db = new DataBaseAdapter(main);
		publishProgress(10);
		
		if (Encryption.privateKey == null &&
				Encryption.publicKey == null) {
			encryption.generateKeyPair();
			publishProgress(30);
			return null;
		}
		publishProgress(40);
		
		if (_user.length() == 0)
			return "Need Nickname for registration!";
		
		ConnectionConfiguration config = new ConnectionConfiguration(th.HOST, th.PORT);
		config.setSASLAuthenticationEnabled(false);
		th.configure(ProviderManager.getInstance());
		ThreadHelper.xmppConnection = new XMPPConnection(config);
		try {
			ThreadHelper.xmppConnection.connect();
		} catch (XMPPException e) {
			return "No server connection! Try again later.";
		}
		publishProgress(50);
		
		_password = th.getMd5Sum(Encryption.privateKey);
		publishProgress(60);
		Boolean check = checkAndRegister(ThreadHelper.xmppConnection);
		if (!check) return "Please try an other name!";
		publishProgress(80);
				
		db.addContact(new Contact(0, _user, _password,
				new String(Encryption.privateKey),
				new String(Encryption.publicKey)));
		publishProgress(90);
		ThreadHelper.xmppConnection.disconnect();
		db.close();
		publishProgress(100);
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
    }
	
	public boolean checkAndRegister(Connection currentConn) {
		if (!currentConn.isAuthenticated()) {
			Map<String, String> mp = new HashMap<String, String>();
			AccountManager am = currentConn.getAccountManager();
			if (th.D) Log.e(TAG, _user+", "+_password);
			try {
				/**
				 * There is a problem in createAccount(config.userName, config.password);
				 * method of smack-issue 15.jar library.
				 */
				mp.put("username", _user);
                mp.put("password", _password);
                am.createAccount(_user, _password, mp);
	        } catch (XMPPException e) {
	        	if (th.D) Log.e(TAG, e.getMessage(), e);
	            return false;
	        }
	        return true;
	    } else return true;
	}
}
