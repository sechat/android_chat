package de.zauberstuhl.sechat;

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

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import android.util.Log;


public class Encryption {

	private static ThreadHelper th = new ThreadHelper();
	
	public static String TAG = th.appName+"Encryption";
	
	public static String privateKey = null;
	public static String publicKey = null;
	
	public void generateKeyPair() {
		PrivateKey priv = null;
		PublicKey pub = null;
		
		try {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(2048);
			KeyPair kp = kpg.genKeyPair();
			priv = kp.getPrivate();
			pub = kp.getPublic();
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, e.getMessage());
		}
		
		Encryption.privateKey = th.base64Encode(priv.getEncoded());
		Encryption.publicKey = th.base64Encode(pub.getEncoded());
	}
	
	public String encrypt(String publicKey, String msg) {
		try {
			PublicKey pubKey = 
			    KeyFactory.getInstance("RSA").generatePublic(
			    		new X509EncodedKeySpec(th.base64Decode(publicKey)));
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, pubKey);
			byte[] cipherData = cipher.doFinal(msg.getBytes());
			return th.base64Encode(cipherData);
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, e.getMessage());
		} catch (NoSuchPaddingException e) {
			Log.e(TAG, e.getMessage());
		} catch (InvalidKeyException e) {
			Log.e(TAG, e.getMessage());
		} catch (IllegalBlockSizeException e) {
			Log.e(TAG, e.getMessage());
		} catch (BadPaddingException e) {
			Log.e(TAG, e.getMessage());
		} catch (InvalidKeySpecException e) {
			Log.e(TAG, e.getMessage());
		}
		return null;
	}
	
	public String decrypt(String privateKey, String msg) {
		try {
			PrivateKey privKey = 
			    KeyFactory.getInstance("RSA").generatePrivate(
			    		new PKCS8EncodedKeySpec(th.base64Decode(privateKey)));
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, privKey);
			byte[] cipherData = cipher.doFinal(th.base64Decode(msg));
			return new String(cipherData);
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, e.getMessage());
		} catch (NoSuchPaddingException e) {
			Log.e(TAG, e.getMessage());
		} catch (InvalidKeyException e) {
			Log.e(TAG, e.getMessage());
		} catch (IllegalBlockSizeException e) {
			Log.e(TAG, e.getMessage());
		} catch (BadPaddingException e) {
			Log.e(TAG, e.getMessage());
		} catch (InvalidKeySpecException e) {
			Log.e(TAG, e.getMessage());
		}
		return null;
	}
}
