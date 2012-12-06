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

import java.util.ArrayList;

import de.zauberstuhl.encoapp.Main;
import de.zauberstuhl.encoapp.R;
import de.zauberstuhl.encoapp.ThreadHelper;
import de.zauberstuhl.encoapp.classes.User;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class UserAdapter extends BaseAdapter {

	private static ThreadHelper th = new ThreadHelper();
	
	private Main main;
    private ArrayList<User> data;
    private static LayoutInflater inflater = null;
    
    public UserAdapter(Main main, ArrayList<User> data) {
    	this.main = main;
        this.data = data;
        inflater = (LayoutInflater)main.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        
        if (vi == null) vi = inflater.inflate(R.layout.user_adapter, null);
        
        RelativeLayout list = (RelativeLayout)vi.findViewById(R.id.userList);
        TextView text = (TextView)vi.findViewById(R.id.txtTitle);
        TextView fingerPrint = (TextView)vi.findViewById(R.id.txtFingerprint);
        
        final User user = data.get(position);
        DataBaseAdapter db = new DataBaseAdapter(main);
        
        list.setOnClickListener(new OnClickListener() {
        	@Override
    		public void onClick(View arg0) {
    			String keyName = user.text;
    			// set window title
    			main.setTitle(keyName);
    			th.setActiveChatUser(keyName);
    			// set contact to normal size
    			th.updateListViewEntry(keyName, Typeface.NORMAL, data);
    			notifyDataSetChanged();
    			// switch to the message board
    			main.viewFlipper.showNext();
    		}
        });
        text.setText(user.text);
        text.setTypeface(null, user.typeface);
        String fp = null;
        if ((fp = db.getPublicKey(user.text)) != null)
        	fingerPrint.setText(th.getMd5Sum(fp));
        db.close();
        
        return vi;
    }
}