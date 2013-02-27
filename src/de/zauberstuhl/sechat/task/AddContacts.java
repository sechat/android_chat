package de.zauberstuhl.sechat.task;

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

import de.zauberstuhl.sechat.ThreadHelper;
import de.zauberstuhl.sechat.activity.UserList;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

public class AddContacts extends AsyncTask<String, Void, Integer> {

	private static ThreadHelper th = new ThreadHelper();
	
	Activity act;
	String region = null;
	String TAG = this.getClass().getName();
	
	public AddContacts(Activity act) {
		this.act = act;
	}
	
	public AddContacts(Activity act, String region) {
		this.act = act;
		this.region = region;
	}
	
	@Override
	protected Integer doInBackground(String... numbers) {
		Integer newCnt = 0;
		TelephonyManager manager =
			(TelephonyManager) act.getSystemService(Context.TELEPHONY_SERVICE);
		if (region == null)
			region = manager.getSimCountryIso();
		
		for (String number: numbers) {
			if (th.D) Log.d(TAG, "Found number " + number + ". Try to parse it");
			PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
			try {
				PhoneNumber phoneNumber = phoneUtil.parse(number,
						region.toUpperCase(Locale.getDefault()));
				if (th.D) Log.d(TAG, "Number successfully parsed!");
				String identifier = region.toLowerCase(Locale.getDefault()) +
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
		th.updateUserList(act);
		return newCnt;
	}
	
	@Override
	protected void onPostExecute(Integer newCnt) {
		if (th.D) Log.d(TAG, "Added " + newCnt + " new friends");
		if (newCnt > 0)
			th.sendNotification(act, "Added " + newCnt + " contact(s)");
		else th.sendNotification(act, "Sorry! No contact was found");
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
