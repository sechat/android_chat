package de.zauberstuhl.encoapp.adapter;

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
import java.util.ArrayList;

import de.zauberstuhl.encoapp.ThreadHelper;
import de.zauberstuhl.encoapp.classes.Contact;
import de.zauberstuhl.encoapp.classes.User;
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
	private static String TAG = th.appName+"DataBaseAdapter";
	 
    private final Context mContext;
    private DataBaseHelper mDbHelper;
	
    public DataBaseAdapter(Context context) {
        this.mContext = context; 
        mDbHelper = new DataBaseHelper(mContext);
    } 
 
    public DataBaseAdapter createDatabase() throws SQLException { 
        try { 
            mDbHelper.createDataBase();
            mDbHelper.openDataBase(); 
            mDbHelper.close(); 
        } catch (IOException mIOException) { 
            Log.e(TAG, mIOException.toString() + "  UnableToCreateDatabase"); 
            throw new Error("UnableToCreateDatabase"); 
        }
        return this; 
    }
 
    public void close() {
    	mDbHelper.close();
    }
    
	public ArrayList<User> getAllContacts() {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		ArrayList<User> contactList = new ArrayList<User>();
		String selectQuery = "SELECT "+ThreadHelper.DB_NAME+" FROM "+ThreadHelper.DB_TABLE;
		try {
			Cursor cursor = db.rawQuery(selectQuery, null);
			if (cursor.moveToPosition(1)) { // set 1 cause 0 is myself
				do {
					contactList.add(new User(cursor.getString(0)));
				} while (cursor.moveToNext());
			}
		} catch (NullPointerException e) { 
			// No entry found
		}
		return contactList;
	}
	
	public String getContactPassword() {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.query(ThreadHelper.DB_TABLE, new String[] { ThreadHelper.DB_PASSWORD },
				ThreadHelper.DB_ID + "=?", new String[] { "0" }, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();
		try {
			return cursor.getString(0);
		} catch (CursorIndexOutOfBoundsException e) {
			Log.e(TAG, e.getMessage());
		}
		return null;
	}
	
	public String getContactName(int id) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.query(ThreadHelper.DB_TABLE, new String[] { ThreadHelper.DB_NAME },
				ThreadHelper.DB_ID + "=?", new String[] { String.valueOf(id) }, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();
		try {
			return cursor.getString(0);
		} catch (CursorIndexOutOfBoundsException e) {
			Log.e(TAG, e.getMessage());
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

		db.insert(ThreadHelper.DB_TABLE, null, values);
		db.close();
	}
	
	public boolean isset(String name) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		try {
			Cursor cursor = db.query(ThreadHelper.DB_TABLE, new String[] { ThreadHelper.DB_NAME },
					ThreadHelper.DB_NAME + "=?", new String[] { name }, null, null, null, null);
			if (cursor != null)
				cursor.moveToFirst();
			cursor.getString(0);
			return true;
		} catch (CursorIndexOutOfBoundsException e) {
			Log.e(TAG, e.getMessage());
		}
		return false;
	}
	
	public boolean isset(int id) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		try {
			Cursor cursor = db.query(ThreadHelper.DB_TABLE, new String[] { ThreadHelper.DB_NAME },
					ThreadHelper.DB_ID + "=?", new String[] { String.valueOf(id) }, null, null, null, null);
			if (cursor != null)
				cursor.moveToFirst();
			cursor.getString(0);
			return true;
		} catch (CursorIndexOutOfBoundsException e) {
			Log.e(TAG, e.getMessage());
		}
		return false;
	}
	
	public String getPublicKey(String name) {
		String result = null;
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.query(ThreadHelper.DB_TABLE, new String[] { ThreadHelper.DB_PUBLIC },
				ThreadHelper.DB_NAME + "=?", new String[] { name }, null, null, null, null);
		try {
			if (cursor != null)
				cursor.moveToFirst();
			result = cursor.getString(0);
		} catch (CursorIndexOutOfBoundsException e) {
			Log.e(TAG, e.getMessage());
		}
		return result;
	}
	
	public String getPublicKey(int id) {
		String result = null;
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.query(ThreadHelper.DB_TABLE, new String[] { ThreadHelper.DB_PUBLIC },
				ThreadHelper.DB_ID + "=?", new String[] { String.valueOf(id) }, null, null, null, null);
		try {
			if (cursor != null)
				cursor.moveToFirst();
			result = cursor.getString(0);
		} catch (CursorIndexOutOfBoundsException e) {
			Log.e(TAG, e.getMessage());
		}
		return result;
	}
	
	public String getPrivateKey(int id) {
		String result = null;
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.query(ThreadHelper.DB_TABLE, new String[] { ThreadHelper.DB_PRIVATE },
				ThreadHelper.DB_ID + "=?", new String[] { String.valueOf(id) }, null, null, null, null);
		try {
			if (cursor != null)
				cursor.moveToFirst();
			result = cursor.getString(0);
		} catch (CursorIndexOutOfBoundsException e) {
			Log.e(TAG, e.getMessage());
		}
		return result;
	}
}
