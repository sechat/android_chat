package de.zauberstuhl.encoapp.activity;

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

import de.zauberstuhl.encoapp.R;
import de.zauberstuhl.encoapp.ThreadHelper;
import de.zauberstuhl.encoapp.task.Register;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;

public class Setup extends Activity {
	
	private static ThreadHelper th = new ThreadHelper();
	private String TAG = getClass().getName();
	
	public static Button start;
    public static ProgressBar progress;
    EditText nickName;
    CheckBox search;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (th.D) Log.e(TAG, "++ onCreate ++");
		setContentView(R.layout.setup_activity);
		
        nickName = (EditText) findViewById(R.id.setup_nickname);
        start = (Button) findViewById(R.id.setup_start);
        progress = (ProgressBar) findViewById(R.id.setup_progress);
        search = (CheckBox) findViewById(R.id.setup_ifsearch);
        
        Spinner spinner = (Spinner) findViewById(R.id.setup_server_dropdown);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
        		this, R.array.server_list, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
	}

	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_BACK) {
    		Intent intent = new Intent(Intent.ACTION_MAIN);
    		intent.addCategory(Intent.CATEGORY_HOME);
    		startActivity(intent);
    		return true;
    	}
    	return super.onKeyDown(keyCode, event);
    }
	
	public void generate(View v) {
    	String input = nickName.getText().toString().trim();
    	start.setVisibility(View.GONE);
    	progress.setVisibility(View.VISIBLE);
    	new Register(this, input, search.isChecked()).execute();
    }
	
}
