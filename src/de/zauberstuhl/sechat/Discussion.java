package de.zauberstuhl.sechat;

import java.sql.Timestamp;

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

public class Discussion {

	Timestamp timestamp;
	String message;
	Boolean me;
	
	public Discussion(Timestamp timestamp, String message, Boolean me) {
		this.timestamp = timestamp;
		this.message = message;
		this.me = me;
	}
	
	public Timestamp getTimestamp() {
		return this.timestamp;
	}
	
	public String getMessage() {
		return this.message;
	}
	
	public Boolean getMe() {
		return this.me;
	}
	
	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public void setMe(Boolean me) {
		this.me = me;
	}
	
}
