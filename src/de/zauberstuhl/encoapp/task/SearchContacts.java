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
		String[] results = {};
		Cursor c = act.getContentResolver().query(
				Phone.CONTENT_URI,
				new String[]{Phone.NUMBER, Phone.DISPLAY_NAME},
				null, null, null);
		
		if (c != null) {
			int i = 0;
			while(c.moveToNext()) {
				String number = c.getString(c.getColumnIndex(Phone.NUMBER));
				//String name = c.getString(c.getColumnIndex(Phone.DISPLAY_NAME));
				results[i] = number;
				i++;
			}
		}
		return results;
	}
	
	@Override
	protected void onPostExecute(String[] results) {
		new AddContacts(act).execute(results);
	}
}
