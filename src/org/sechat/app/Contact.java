package org.sechat.app;

/**
 * Copyright (c) 2014 Sechat GbR <support@sechat.org>
 *
 * You should have received a copy of the MIT License
 * along with this program (license.txt).
 * If not, see <http://sechat.github.io/license.txt>.
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
