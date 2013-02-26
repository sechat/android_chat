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

import java.sql.Timestamp;
import java.util.LinkedList;

import de.zauberstuhl.encoapp.Discussion;
import de.zauberstuhl.encoapp.R;
import de.zauberstuhl.encoapp.ThreadHelper;

import android.app.Activity;
import android.content.Context;
import android.text.ClipboardManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MessageAdapter extends BaseAdapter {
	
	private static ThreadHelper th = new ThreadHelper();

	private Activity activity;
    private LinkedList<Discussion> data;
    private static LayoutInflater inflater = null;
    
    public MessageAdapter(Activity activity, LinkedList<Discussion> data) {
    	this.activity = activity;
        this.data = data;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final Discussion entry = data.get(position);
        
        if (entry.getMe()) vi = inflater.inflate(R.layout.message_list_out, null);
        else vi = inflater.inflate(R.layout.message_list_in, null);
        
        LinearLayout list = (LinearLayout)vi.findViewById(R.id.messageList);
        TextView time = (TextView)vi.findViewById(R.id.time);
        TextView message = (TextView)vi.findViewById(R.id.message);
        
        list.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View arg0) {
				ClipboardManager cm = (ClipboardManager) activity
					.getSystemService(Context.CLIPBOARD_SERVICE);
		        cm.setText(entry.getMessage());
		        th.sendNotification(activity, "Copied to clipboard");
				return true;
			}
        });
        message.setText(entry.getMessage());
        Timestamp timestamp = entry.getTimestamp();
        time.setText(timestamp.getHours()+":"+timestamp.getMinutes());
        return vi;
    }
}
