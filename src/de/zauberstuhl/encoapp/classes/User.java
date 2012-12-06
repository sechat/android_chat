package de.zauberstuhl.encoapp.classes;

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

import android.graphics.Typeface;

public class User {
	public String text;
	public int typeface;
	
    public User() {
        super();
    }
   
    public User(String text) {
        super();
        this.text = text;
        this.typeface = Typeface.NORMAL;
    }
    
    public User(String text, int typeface) {
        super();
        this.text = text;
        this.typeface = typeface;
    }
}
