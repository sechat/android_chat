package org.sechat.app.task;

/**
 * Copyright (c) 2014 Sechat GbR <support@sechat.org>
 *
 * You should have received a copy of the MIT License
 * along with this program (license.txt).
 * If not, see <http://sechat.github.io/license.txt>.
 */

import java.util.Set;

import com.google.i18n.phonenumbers.PhoneNumberUtil;

import org.sechat.app.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class AddManualContacts extends AsyncTask<Void, Void, Builder> {

	Activity act;
	
	public AddManualContacts(Activity act) {
		this.act = act;
	}
	
	@Override
	protected Builder doInBackground(Void... arg0) {
		TelephonyManager manager =
			(TelephonyManager) act.getSystemService(Context.TELEPHONY_SERVICE);
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		Set<String> regions = phoneUtil.getSupportedRegions();
		String[] locales = regions.toArray(new String[regions.size()]);
		
		final ArrayAdapter<String> adp = new ArrayAdapter<String>(act,
	            android.R.layout.simple_spinner_item, locales);
		LinearLayout layout = new LinearLayout(act);
		layout.setOrientation(LinearLayout.HORIZONTAL);
		
		final Spinner sp = new Spinner(act);
		sp.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		sp.setAdapter(adp);
		sp.setSelection(getPostiton(locales, manager.getSimCountryIso()));
		layout.addView(sp);
		
		final EditText input = new EditText(act);
		input.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		layout.addView(input);
		
		Builder builder = new AlertDialog.Builder(act)
	    .setTitle("Add contact")
	    .setMessage(act.getString(R.string.add_contact_description))
	    .setView(layout)
	    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	        	String[] numbers = input.getText().toString().split(",");
	        	new AddContacts(act, sp.getSelectedItem().toString()).execute(numbers);
	        }
	    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {}
	    });
		return builder;
	}
	
	@Override
	protected void onPostExecute(Builder builder) {
		builder.show();
	}
	
	private int getPostiton(String[] locales, String value) {
		for(int i = 0; i < locales.length; i++) {
			if (locales[i].equalsIgnoreCase(value)) return i;
		}
		return 0;
	}

}
