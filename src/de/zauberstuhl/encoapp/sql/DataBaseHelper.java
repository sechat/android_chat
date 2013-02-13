package de.zauberstuhl.encoapp.sql;

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
import de.zauberstuhl.encoapp.ThreadHelper;
 
import android.content.Context; 
import android.database.SQLException; 
import android.database.sqlite.SQLiteDatabase; 
import android.database.sqlite.SQLiteOpenHelper; 
 
public class DataBaseHelper extends SQLiteOpenHelper { 

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
    	db.execSQL("CREATE TABLE "+ThreadHelper.DB_TABLE+" ("+
    			ThreadHelper.DB_ID+" INTEGER PRIMARY KEY, "+
    			ThreadHelper.DB_NAME+" TEXT, "+
    			ThreadHelper.DB_PASSWORD+" TEXT, "+
    			ThreadHelper.DB_PRIVATE+" TEXT, "+
    			ThreadHelper.DB_PUBLIC+" TEXT)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS "+ThreadHelper.DB_TABLE);
		onCreate(db);
	}
}