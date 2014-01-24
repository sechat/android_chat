package org.sechat.app.task;

/**
 * Copyright (c) 2014 Sechat GbR <support@sechat.org>
 *
 * You should have received a copy of the MIT License
 * along with this program (license.txt).
 * If not, see <http://sechat.github.io/license.txt>.
 */

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.packet.VCard;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import org.sechat.app.Contact;
import org.sechat.app.Encryption;
import org.sechat.app.ThreadHelper;
import org.sechat.app.activity.Setup;
import org.sechat.app.activity.UserList;
import org.sechat.app.adapter.DataBaseAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;

public class Register extends AsyncTask<Void, Integer, String> {

	private static ThreadHelper th = new ThreadHelper();
	private String TAG = getClass().getName();
	private static Encryption encryption = new Encryption();

	Activity act;
	String nickName, password, identifier, phoneNumber;
	Boolean isChecked;

	public Register(Activity act, String nickName, String phoneNumber, Boolean isChecked) {
		this.act = act;
		this.nickName = nickName;
		this.isChecked = isChecked;
		this.phoneNumber = phoneNumber;
	}

	@Override
	protected String doInBackground(Void... params) {
		TelephonyManager manager =
			(TelephonyManager) act.getSystemService(Context.TELEPHONY_SERVICE);
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		
		if (nickName.length() == 0)
			return "Need Nickname for registration!";
		
		if (phoneNumber.length() == 0)
			return "Please specify a phone number!";
		
		try {
			PhoneNumber phoneNumber = phoneUtil.parse(
					this.phoneNumber, manager.getSimCountryIso().toUpperCase(Locale.getDefault()));
			identifier = manager.getSimCountryIso() + phoneNumber.getNationalNumber();
		} catch (NumberParseException e) {
			Log.e(TAG, e.getMessage(), e);
			return "It seams your phone number was not correct.";
		}
		
		if (Encryption.privateKey == null && Encryption.publicKey == null)
			encryption.generateKeyPair();
		password = th.getMd5Sum(Encryption.privateKey);
		
		// try to register the account
		try {
			th.xmppConnect();
		} catch (XMPPException e) {
			Log.e(TAG, e.getMessage(), e);
            return e.getMessage();
		}
		Map<String, String> mp = new HashMap<String, String>();
		AccountManager am = ThreadHelper.xmppConnection.getAccountManager();
		if (th.D) Log.d(TAG, identifier+", "+password);
		try {
			/**
			 * There is a problem in createAccount(config.userName, config.password);
			 * method of smack-issue 15.jar library.
			 */
			mp.put("username", identifier);
            mp.put("password", password);
            am.createAccount(identifier, password, mp);
        } catch (XMPPException e) {
        	Log.e(TAG, e.getMessage(), e);
            return e.getMessage();
        }
		
		DataBaseAdapter db = new DataBaseAdapter(act);
		db.addContact(new Contact(0, identifier, password,
				new String(Encryption.privateKey),
				new String(Encryption.publicKey)));
		db.close();
		
		// now login with your new credentials
		if (!th.xmppLogin(act)) {
			return "Cannot login!";
		}
		
		VCard vCard = new VCard();
		try {
			vCard.load(ThreadHelper.xmppConnection);
			vCard.setNickName(nickName);
			vCard.setField("pubkey", Encryption.publicKey);
			vCard.save(ThreadHelper.xmppConnection);
		} catch (XMPPException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		if (isChecked) new SearchContacts(act).execute();
		return null;
	}

	@Override
    protected void onPostExecute(String result) {
		if (result != null) {
			th.sendNotification(act, result);
			Setup.start.setVisibility(View.VISIBLE);
	    	Setup.progress.setVisibility(View.GONE);
			return;
		}
		Intent userList = new Intent(act, UserList.class);
		act.startActivity(userList);
    }
}
