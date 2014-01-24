package org.sechat.app.adapter;

/**
 * Copyright (c) 2014 Sechat GbR <support@sechat.org>
 *
 * You should have received a copy of the MIT License
 * along with this program (license.txt).
 * If not, see <http://sechat.github.io/license.txt>.
 */

import java.sql.Timestamp;
import java.util.LinkedList;

import org.sechat.app.Discussion;
import org.sechat.app.R;
import org.sechat.app.ThreadHelper;

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
