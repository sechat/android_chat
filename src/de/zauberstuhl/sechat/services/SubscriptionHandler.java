package de.zauberstuhl.sechat.services;

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

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import android.app.Activity;
import android.util.Log;

import de.zauberstuhl.sechat.ThreadHelper;

public class SubscriptionHandler implements PacketListener {
	
	private static ThreadHelper th = new ThreadHelper();
	
	Activity act;
	String TAG = getClass().getName();
	
	public SubscriptionHandler(Activity act) {
		this.act = act;
	}
	
	@Override
	public void processPacket(Packet aPacket) {
		if ((ThreadHelper.xmppConnection == null) || (!ThreadHelper.xmppConnection.isAuthenticated())) {
			Log.e(TAG, "Cannot accept subscription! Server connection was not authenticated.");
			return;
		}
		
		if (aPacket instanceof Presence) {
			Presence p = (Presence) aPacket;
			Roster roster = ThreadHelper.xmppConnection.getRoster();
			Boolean known = roster.contains(aPacket.getFrom());
			
			if (th.D) Log.d(TAG, aPacket.getFrom()+" "+p.getType().name());
			if (Presence.Type.subscribe.equals(p.getType())) {
				Presence subscribed = new Presence(Presence.Type.subscribed);
				subscribed.setTo(aPacket.getFrom());
				ThreadHelper.xmppConnection.sendPacket(subscribed);
				
				if (!known) {
					Presence subscribe = new Presence(Presence.Type.subscribe);
					subscribe.setTo(aPacket.getFrom());
					ThreadHelper.xmppConnection.sendPacket(subscribe);
				}
			}
						
			if (Presence.Type.unsubscribe.equals(p.getType()) && known) {
				Presence unsubscribed = new Presence(Presence.Type.unsubscribed);
				unsubscribed.setTo(aPacket.getFrom());
				ThreadHelper.xmppConnection.sendPacket(unsubscribed);
				
				if (known) {
					Presence unsubscribe = new Presence(Presence.Type.unsubscribe);
					unsubscribe.setTo(aPacket.getFrom());
					ThreadHelper.xmppConnection.sendPacket(unsubscribe);
				}
			}
		}
	}
}