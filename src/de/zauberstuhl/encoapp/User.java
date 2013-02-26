package de.zauberstuhl.encoapp;

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

public class User {
	public String jid;
	public String name = null;
	public boolean online = false;
	public boolean newMessage = false;
	
   
    public User(String jid) {
        this.jid = jid;
    }
    
    public User(String jid, String name) {
        this.jid = jid;
        this.name = name;
    }
    
    public User(String jid, boolean online) {
        this.jid = jid;
        this.online = online;
    }
    
    public User(String jid, boolean online, boolean newMessage) {
        this.jid = jid;
        this.online = online;
        this.newMessage = newMessage;
    }
    
    public User(String jid, String name, boolean online) {
        this.jid = jid;
        this.name = name;
        this.online = online;
    }
    
    public User(String jid, String name, boolean online, boolean newMessage) {
        this.jid = jid;
        this.name = name;
        this.online = online;
        this.newMessage = newMessage;
    }
}
