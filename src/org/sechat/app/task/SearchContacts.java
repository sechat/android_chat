package org.sechat.app.task;

/**
 * Copyright (c) 2014 Sechat GbR <support@sechat.org>
 *
 * You should have received a copy of the MIT License
 * along with this program (license.txt).
 * If not, see <http://sechat.github.io/license.txt>.
 */

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract.CommonDataKinds.Phone;

public class SearchContacts extends AsyncTask<Void, Void, String[]> {

	Activity act;
	
	public SearchContacts(Activity act) {
		this.act = act;
	}
	
	@Override
	protected String[] doInBackground(Void... params) {
		String results = "";
		Cursor c = act.getContentResolver().query(
				Phone.CONTENT_URI,
				new String[]{Phone.NUMBER, Phone.DISPLAY_NAME},
				null, null, null);
		
		if (c != null) {
			while(c.moveToNext()) {
				String number = c.getString(c.getColumnIndex(Phone.NUMBER));
				//String name = c.getString(c.getColumnIndex(Phone.DISPLAY_NAME));
				results += number + ",";
			}
		}
		return results.split(",");
	}
	
	@Override
	protected void onPostExecute(String[] results) {
		new AddContacts(act).execute(results);
	}
}
