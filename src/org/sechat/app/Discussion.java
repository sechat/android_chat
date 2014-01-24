package org.sechat.app;

/**
 * Copyright (c) 2014 Sechat GbR <support@sechat.org>
 *
 * You should have received a copy of the MIT License
 * along with this program (license.txt).
 * If not, see <http://sechat.github.io/license.txt>.
 */

import java.sql.Timestamp;

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
