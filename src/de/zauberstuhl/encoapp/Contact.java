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

public class Contact {
	
	//private variables
	int _id;
	String _name;
	String _password;
	String _priv;
	String _pub;
	
	// constructor
	public Contact(int id, String name, String password, String priv, String pub) {
		this._id = id;
		this._name = name;
		this._password = password;
		this._priv = priv;
		this._pub = pub;
	}
	
	// constructor
	public Contact(String name, String password, String _priv, String _pub){
		this._id = -1;
		this._name = name;
		this._password = password;
		this._priv = _priv;
		this._pub = _pub;
	}
	
	// constructor
	public Contact(String name, String _priv, String _pub){
		this._id = -1;
		this._name = name;
		this._password = null;
		this._priv = _priv;
		this._pub = _pub;
	}
	
	// getting ID
	public int getID(){
		return this._id;
	}
	
	// setting id
	public void setID(int id){
		this._id = id;
	}
	
	// getting name
	public String getName(){
		return this._name;
	}
	
	// setting name
	public void setName(String name){
		this._name = name;
	}
	
	public String getPass(){
		return this._password;
	}
	
	public void setPass(String password){
		this._password = password;
	}
	
	public String getPriv(){
		return this._priv;
	}
	
	public void setPriv(String _priv){
		this._priv = _priv;
	}
	
	public String getPub(){
		return this._pub;
	}
	
	public void setPub(String _pub){
		this._pub = _pub;
	}
}
