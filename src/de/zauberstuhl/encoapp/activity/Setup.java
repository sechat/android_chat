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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

public class Setup extends Activity {
	
	private static ThreadHelper th = new ThreadHelper();
	private String TAG = getClass().getName();
	
	public static Button start;
    public static ProgressBar progress;
    TextView phoneNumberHint;
    EditText nickName, phoneNumber;
    CheckBox search;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (th.D) Log.e(TAG, "++ onCreate ++");
		setContentView(R.layout.setup_activity);
		
        nickName = (EditText) findViewById(R.id.setup_nickname);
        phoneNumber = (EditText) findViewById(R.id.getPhoneNumberText);
        phoneNumberHint = (TextView) findViewById(R.id.getPhoneNumberHint);
        start = (Button) findViewById(R.id.setup_start);
        progress = (ProgressBar) findViewById(R.id.setup_progress);
        search = (CheckBox) findViewById(R.id.setup_ifsearch);
        
        Spinner spinner = (Spinner) findViewById(R.id.setup_server_dropdown);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
        		this, R.array.server_list, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        
        TelephonyManager manager =
			(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (manager.getLine1Number() == null) {
        	phoneNumberHint.setVisibility(View.VISIBLE);
        	phoneNumber.setVisibility(View.VISIBLE);
        } else phoneNumber.setText(manager.getLine1Number());
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
    	final String input = nickName.getText().toString().trim();
    	final String number = phoneNumber.getText().toString();
    	
    	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
    		@Override
    		public void onClick(DialogInterface dialog, int which) {
    			if (which == DialogInterface.BUTTON_POSITIVE) {
    				start.setVisibility(View.GONE);
    				progress.setVisibility(View.VISIBLE);
    				new Register(Setup.this, input, number, search.isChecked()).execute();
    			} else if (which == DialogInterface.BUTTON_NEGATIVE) {
    	        	phoneNumberHint.setVisibility(View.VISIBLE);
    	        	phoneNumber.setVisibility(View.VISIBLE);
    			}
    		}
    	};
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage("Is this phone number '"+number+"' correct?\n\n"+
    			"If not, friends will not find you.")
    			.setPositiveButton("Yes", dialogClickListener)
    			.setNegativeButton("No", dialogClickListener).show();
    }
	
}
