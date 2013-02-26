package de.zauberstuhl.encoapp.adapter;

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

import java.io.IOException; 
import java.sql.Timestamp;
import java.util.LinkedList;

import de.zauberstuhl.encoapp.Contact;
import de.zauberstuhl.encoapp.Discussion;
import de.zauberstuhl.encoapp.ThreadHelper;
import de.zauberstuhl.encoapp.sql.DataBaseHelper;

import android.content.ContentValues;
import android.content.Context; 
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.SQLException; 
import android.database.sqlite.SQLiteDatabase; 
import android.util.Log; 
 
public class DataBaseAdapter { 
	
	private static ThreadHelper th = new ThreadHelper();
	 
    private final Context mContext;
    private DataBaseHelper mDbHelper;
	
    String TAG = this.getClass().getName();
    
    public DataBaseAdapter(Context context) {
        this.mContext = context; 
        mDbHelper = new DataBaseHelper(mContext);
    } 
 
    public DataBaseAdapter createDatabase() throws SQLException { 
        try { 
            mDbHelper.createDataBase();
            mDbHelper.openDataBase(); 
            mDbHelper.close(); 
        } catch (IOException e) { 
        	Log.e(TAG, e.toString(), e);
        	return null;
        }
        return this; 
    }
 
    public void close() {
    	mDbHelper.close();
    }
	
	public String getPassword() {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.query(ThreadHelper.DB_USER_TABLE, new String[] { ThreadHelper.DB_PASSWORD },
				ThreadHelper.DB_ID + "=?", new String[] { "0" }, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();
		try {
			return cursor.getString(0);
		} catch (CursorIndexOutOfBoundsException e) {
			if (th.D) Log.d(TAG, e.getMessage());
		}
		return null;
	}
	
	public String getName() {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.query(ThreadHelper.DB_USER_TABLE, new String[] { ThreadHelper.DB_NAME },
				ThreadHelper.DB_ID + "=?", new String[] { "0" }, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();
		try {
			return cursor.getString(0);
		} catch (CursorIndexOutOfBoundsException e) {
			if (th.D) Log.d(TAG, e.getMessage());
		}
		return null;
	}
	
	public void addContact(Contact contact) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		if (contact.getID() != -1)
			values.put(ThreadHelper.DB_ID, contact.getID());
		values.put(ThreadHelper.DB_NAME, contact.getName());
		values.put(ThreadHelper.DB_PASSWORD, contact.getPass());
		values.put(ThreadHelper.DB_PRIVATE, contact.getPriv());
		values.put(ThreadHelper.DB_PUBLIC, contact.getPub());

		db.insert(ThreadHelper.DB_USER_TABLE, null, values);
		db.close();
	}
	
	public void addMessage(String jid, String message, Boolean me) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		
		values.put(ThreadHelper.DB_NAME, jid);
		values.put(ThreadHelper.DB_MESSAGE, message);
		values.put(ThreadHelper.DB_ME, String.valueOf(me));

		db.insert(ThreadHelper.DB_HISTORY_TABLE, null, values);
		db.close();
	}
	
	public LinkedList<Discussion> getMessagesFrom(String jid) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		LinkedList<Discussion> list = new LinkedList<Discussion>();
		String selectQuery = "SELECT "+ThreadHelper.DB_DATE+", "+
							 ThreadHelper.DB_MESSAGE+", "+ThreadHelper.DB_ME+
							 " FROM "+ThreadHelper.DB_HISTORY_TABLE+
							 " WHERE "+ThreadHelper.DB_NAME+" LIKE '"+jid+"'";
		
		try {
			Cursor cursor = db.rawQuery(selectQuery, null);
			if (cursor.moveToPosition(0)) {
				do {list.add(
						new Discussion(
							Timestamp.valueOf(cursor.getString(0)),
							cursor.getString(1),
							Boolean.valueOf(cursor.getString(2))
						));
				} while (cursor.moveToNext());
			}
		} catch (NullPointerException e) {
			// no results found
			return new LinkedList<Discussion>();
		}
		return list;
	}
	
	public void update(Contact contact) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		
		values.put(ThreadHelper.DB_NAME, contact.getName());
		values.put(ThreadHelper.DB_PASSWORD, contact.getPass());
		values.put(ThreadHelper.DB_PRIVATE, contact.getPriv());
		values.put(ThreadHelper.DB_PUBLIC, contact.getPub());

		db.update(ThreadHelper.DB_USER_TABLE, values, ThreadHelper.DB_NAME + "=?",
				new String []{ contact.getName() });
		db.close();
	}
	
	public boolean issetUser() {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		try {
			Cursor cursor = db.query(ThreadHelper.DB_USER_TABLE, new String[] { ThreadHelper.DB_NAME },
					ThreadHelper.DB_ID + "=?", new String[] { "0" }, null, null, null, null);
			if (cursor != null)
				cursor.moveToFirst();
			cursor.getString(0);
			return true;
		} catch (CursorIndexOutOfBoundsException e) {
			if (th.D) Log.d(TAG, e.getMessage());
		}
		return false;
	}
	
	public String getPublicKey() {
		String result = null;
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.query(ThreadHelper.DB_USER_TABLE, new String[] { ThreadHelper.DB_PUBLIC },
				ThreadHelper.DB_ID + "=?", new String[] { "0" }, null, null, null, null);
		try {
			if (cursor != null)
				cursor.moveToFirst();
			result = cursor.getString(0);
		} catch (CursorIndexOutOfBoundsException e) {
			if (th.D) Log.d(TAG, e.getMessage());
		}
		return result;
	}
	
	public String getPrivateKey() {
		String result = null;
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.query(ThreadHelper.DB_USER_TABLE, new String[] { ThreadHelper.DB_PRIVATE },
				ThreadHelper.DB_ID + "=?", new String[] { "0" }, null, null, null, null);
		try {
			if (cursor != null)
				cursor.moveToFirst();
			result = cursor.getString(0);
		} catch (CursorIndexOutOfBoundsException e) {
			if (th.D) Log.d(TAG, e.getMessage());
		}
		return result;
	}
}
