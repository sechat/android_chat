package de.zauberstuhl.sechat.sql;

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
import de.zauberstuhl.sechat.Contact;
import de.zauberstuhl.sechat.ThreadHelper;
import android.content.ContentValues;
import android.content.Context; 
import android.database.Cursor;
import android.database.SQLException; 
import android.database.sqlite.SQLiteDatabase; 
import android.database.sqlite.SQLiteOpenHelper; 
import android.util.Log;
 
public class DataBaseHelper extends SQLiteOpenHelper { 

	private static ThreadHelper th = new ThreadHelper();
	
	String TAG = this.getClass().getName();
	
	private Context context;
	private SQLiteDatabase mDataBase;
 
	public DataBaseHelper(Context context) {
		super(	context,
				ThreadHelper.DATABASE, null,
				ThreadHelper.DATABASE_VERSION);
		this.context = context;
	}
		
	public void createDataBase() throws IOException {
		//If database not exists copy it from the assets
		boolean mDataBaseExist = checkDataBase();
		if(!mDataBaseExist) {
			this.getReadableDatabase();
			this.close();
		}
	}
	
    private boolean checkDataBase() {
        return context.getDatabasePath(ThreadHelper.DATABASE).exists();
    }
 
    //Open the database, so we can query it 
    public boolean openDataBase() throws SQLException {
    	mDataBase = SQLiteDatabase.openOrCreateDatabase(
    			context.getDatabasePath(ThreadHelper.DATABASE), null);
        return mDataBase != null;
    } 
 
    @Override 
    public synchronized void close() { 
        if(mDataBase != null) 
            mDataBase.close(); 
        super.close();
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
		// registered user
    	db.execSQL("CREATE TABLE "+ThreadHelper.DB_USER_TABLE+" ("+
    			ThreadHelper.DB_ID+" INTEGER PRIMARY KEY, "+
    			ThreadHelper.DB_NAME+" TEXT, "+
    			ThreadHelper.DB_PASSWORD+" TEXT, "+
    			ThreadHelper.DB_PRIVATE+" TEXT, "+
    			ThreadHelper.DB_PUBLIC+" TEXT)");
    	// message history
    	db.execSQL("CREATE TABLE "+ThreadHelper.DB_HISTORY_TABLE+" ("+
    			ThreadHelper.DB_NAME+" TEXT, "+
    			ThreadHelper.DB_MESSAGE+" TEXT, "+
    			ThreadHelper.DB_ME+" TEXT, "+
    			ThreadHelper.DB_DATE+" TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (th.D) Log.d(TAG, "++ onUpgrade ++");
		Contact root = null;
		String selectQuery = "SELECT " +
			ThreadHelper.DB_NAME + ", " +
			ThreadHelper.DB_PASSWORD +", " +
			ThreadHelper.DB_PRIVATE + ", " +
			ThreadHelper.DB_PUBLIC + " FROM " +
			ThreadHelper.DB_USER_TABLE + " WHERE id = 0";
		try {
			Cursor cursor = db.rawQuery(selectQuery, null);
			if (cursor.moveToPosition(0)) {
				root = new Contact(0, cursor.getString(0),
						cursor.getString(1), cursor.getString(2), cursor.getString(3));
			}
		} catch (NullPointerException e) {
			// no results found
			if (th.D) Log.d(TAG, "No data found in " + ThreadHelper.DB_USER_TABLE);
		}
		if (th.D) Log.d(TAG, "Delete old tables");
		db.execSQL("DROP TABLE IF EXISTS " + ThreadHelper.DB_USER_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + ThreadHelper.DB_HISTORY_TABLE);
		if (th.D) Log.d(TAG, "Create new tables");
		onCreate(db);
		
		if (th.D) Log.d(TAG, "Write backup to new database");
		ContentValues values = new ContentValues();
		values.put(ThreadHelper.DB_ID, root.getID());
		values.put(ThreadHelper.DB_NAME, root.getName());
		values.put(ThreadHelper.DB_PASSWORD, root.getPass());
		values.put(ThreadHelper.DB_PRIVATE, root.getPriv());
		values.put(ThreadHelper.DB_PUBLIC, root.getPub());
		db.insert(ThreadHelper.DB_USER_TABLE, null, values);
	}
}