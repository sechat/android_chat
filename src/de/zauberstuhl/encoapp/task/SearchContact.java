package de.zauberstuhl.encoapp.task;

/**
 * Copyright (C) 2013 Lukas Matt <lukas@zauberstuhl.de>
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
import java.util.Locale;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.ReportedData;
import org.jivesoftware.smackx.ReportedData.Row;
import org.jivesoftware.smackx.search.UserSearchManager;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import de.zauberstuhl.encoapp.ThreadHelper;
import de.zauberstuhl.encoapp.activity.UserList;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SearchContact extends AsyncTask<Void, Void, Integer> {

	private static ThreadHelper th = new ThreadHelper();
	private String TAG = getClass().getName();

	Activity act;
	
	public SearchContact(Activity act) {
		this.act = act;
	}
	
	@Override
	protected Integer doInBackground(Void... params) {
		Integer newCnt = 0;
		TelephonyManager manager =
			(TelephonyManager) act.getSystemService(Context.TELEPHONY_SERVICE);
		String countryIso = manager.getSimCountryIso();
		Cursor c = act.getContentResolver().query(
				Phone.CONTENT_URI,
				new String[]{Phone.NUMBER, Phone.DISPLAY_NAME},
				null, null, null);
		
		if (c != null) {
			while(c.moveToNext()) {
				String number = c.getString(c.getColumnIndex(Phone.NUMBER));
				//String name = c.getString(c.getColumnIndex(Phone.DISPLAY_NAME));
				if (th.D) Log.d(TAG, "Found number " + number + ". Try to parse it..");
				PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
					try {
						PhoneNumber phoneNumber = phoneUtil.parse(number,
								countryIso.toUpperCase(Locale.getDefault()));
						if (th.D) Log.d(TAG, "Number successfully parsed!");
						String identifier = countryIso.toLowerCase(Locale.getDefault()) +
											phoneNumber.getNationalNumber();
						String jid = identifier + "@" + th.HOST;
						if (th.D) Log.d(TAG, jid);
						Roster roster = ThreadHelper.xmppConnection.getRoster();
						if (!userExist(identifier, jid)) {
							if (th.D) Log.d(TAG, "User '"+jid+"' not found!");
							continue;
						}
						if (roster.contains(jid)) {
							if (th.D) Log.d(TAG, "This user is already in your contact list!");
							continue;
						}
						
						Presence packet = new Presence(Presence.Type.subscribe);
						packet.setTo(jid);
						ThreadHelper.xmppConnection.sendPacket(packet);
						newCnt++;
					} catch (NumberParseException e) {
						Log.e(TAG, "No regular phone number. Cannot use it!");
					}
			}
		}
		
		try {
			th.updateUserList(act);
		} catch (XMPPException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		return newCnt;
	}
	
	@Override
	protected void onPostExecute(Integer newCnt) {
		if (th.D) Log.d(TAG, "Added " + newCnt + " new friends!");
		th.sendNotification(act, "Added " + newCnt + " new friends!");
		UserList.addAuto.setEnabled(true);
	}
	
	boolean userExist(String user, String jid) {
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
                	   if (jids.next().equalsIgnoreCase(jid))
                		   return true;
                }
			} catch (XMPPException e) {
				if (th.D) Log.e(TAG, e.getMessage(), e);
			}
		}
		return false;
	}
}
