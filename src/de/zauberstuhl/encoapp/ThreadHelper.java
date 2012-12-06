package de.zauberstuhl.encoapp;

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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import de.zauberstuhl.encoapp.adapter.DataBaseAdapter;
import de.zauberstuhl.encoapp.async.AddContact;
import de.zauberstuhl.encoapp.async.services.Listener;
import de.zauberstuhl.encoapp.classes.User;
import de.zauberstuhl.encoapp.enc.Encryption;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

public class ThreadHelper {
	
	private static Encryption encryption = new Encryption();
	
	private static String activeChatUser = null;
	private static String nickName = null;
	private static boolean activityVisible;
	
	private static HashMap<String,LinkedHashMap<Integer, String>> userDiscussion =
		new HashMap<String,LinkedHashMap<Integer, String>>();
	
	public final String USER_ID = "10";
	public final String MY_ID = "11";
	
	/**
	 * Service
	 */
	public static final long REPEAT_TIME = 1000 * 320;
	
	public final boolean D = true;
	public final String appName = "3nc0App";
	public final String TAG = appName+"ThreadHelper";
	public final String HOST = "www.3nc0.de";
	public final String IP = "188.40.178.248";
	public final int PORT = 9001;
	
	/**
	 * Database params
	 */
	public final static int DATABASE_VERSION = 3;
	public final static String DATABASE = "3ncoApp";
	public final static String DB_TABLE = "userTable";
	public final static String DB_ID = "id";
	public final static String DB_NAME = "name";
	public final static String DB_MD5 = "md5";
	public final static String DB_PRIVATE = "private";
	public final static String DB_PUBLIC = "public";
	
	public static boolean isActivityVisible() {
		return activityVisible;
	}
	
	public static void activityResumed() {
		activityVisible = true;
	}
	
	public static void activityPaused() {
		activityVisible = false;
	}
	 
	public void addDiscussionEntry(String user, String message, Boolean me) {
		LinkedHashMap<Integer, String> map = ThreadHelper.userDiscussion.get(user);
		if (map == null) map = new LinkedHashMap<Integer, String>();
		String identifier = String.valueOf(map.size());
		if (me) identifier = MY_ID + identifier;
		else identifier = USER_ID + identifier;
		
		if (D) Log.e(TAG, "discussion user: "+user);
		
		map.put(Integer.valueOf(identifier), message);
		ThreadHelper.userDiscussion.put(user, map);
	}
	
	public LinkedHashMap<Integer, String> getUserDiscussion(String user) {
		return ThreadHelper.userDiscussion.get(user);
	}
	
	public void setActiveChatUser(String input) {
		ThreadHelper.activeChatUser = input;
	}
	
	public String getActiveChatUser() {
		return ThreadHelper.activeChatUser;
	}
	
	public void setNickName(String input) {
		ThreadHelper.nickName = input;
	}
	
	public String getNickName() {
		String nick = ThreadHelper.nickName;
		if (nick == null)
			return "";
		return nick;
	}
	
	public SSLSocket getConnection() throws KeyManagementException, UnknownHostException, IOException, NoSuchAlgorithmException {
		SSLContext sc = SSLContext.getInstance("SSL");
	    // Create empty HostnameVerifier
	    HostnameVerifier hv = new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
	    };
	    // Create a trust manager that does not validate certificate chains
	    TrustManager[] trustAllCerts = new TrustManager[]{
	    new X509TrustManager() {
	    	public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	    		return null;
	    	}
	    	public void checkClientTrusted(
	    			java.security.cert.X509Certificate[] certs, String authType) {}
	    	public void checkServerTrusted(
	    			java.security.cert.X509Certificate[] certs, String authType) {}
	    	}
	    };

	    sc.init(null, trustAllCerts, new java.security.SecureRandom());
	    SSLSocketFactory factory = sc.getSocketFactory();
	    HttpsURLConnection.setDefaultSSLSocketFactory(factory);
	    HttpsURLConnection.setDefaultHostnameVerifier(hv);
	    SSLSocket socket = (SSLSocket) factory.createSocket(IP, PORT);
	    //socket.setSoTimeout(600000); // 10 minute timeout
        return socket;
	}
	
	public void close(SSLSocket socket) {
		try {
			if (D) Log.e(TAG, "Close socket!");
			socket.close();
		} catch (IOException e) {
			if (D) Log.e(TAG, e.getMessage());
		}
	}
	
	public void close(ObjectOutputStream oout) {
		try {
			if (D) Log.e(TAG, "Close socket!");
			oout.close();
		} catch (IOException e) {
			if (D) Log.e(TAG, e.getMessage());
		}
	}
	
	public void close(ObjectInputStream oin) {
		try {
			if (D) Log.e(TAG, "Close socket!");
			oin.close();
		} catch (IOException e) {
			if (D) Log.e(TAG, e.getMessage());
		}
	}
	
	public String getMd5Sum(File file)	{
		MessageDigest messageDigest = null;
		byte[] ba = new byte[8192];
		try {
			InputStream is = new FileInputStream(file);
			messageDigest = MessageDigest.getInstance("MD5");
			for( int n = 0; (n = is.read( ba )) > -1; ) { messageDigest.update( ba, 0, n ); }
		} catch (IOException e) {
			if (D) Log.e(TAG, e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			if (D) Log.e(TAG, e.getMessage());
		}
		return getMd5Sum(
				new String(messageDigest.digest()));
	}
	
    public String getMd5Sum(String in) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
	        md5.reset();
	        md5.update(in.getBytes());
	        byte[] result = md5.digest();

	        StringBuffer hexString = new StringBuffer();
	        for (int i=0; i<result.length; i++) {
	        	if(result[i] <= 15 && result[i] >= 0){
	        		hexString.append("0");
	        	}
	        	hexString.append(Integer.toHexString(0xFF & result[i]));
	        }
	        return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			if (D) Log.e(TAG, e.getMessage());
		}
		return null;
    }
	
	public void updateChat(final Main main) {
		main.runOnUiThread(new Runnable(){
			@Override
			public void run() {
				if (getActiveChatUser() == null) return;
				LinkedHashMap<Integer, String> chatLog =
					getUserDiscussion(getMd5Sum(getActiveChatUser()));
				if (chatLog == null) chatLog = new LinkedHashMap<Integer, String>();
				
				main.msgListItems.clear();
				main.msgListItems.putAll(chatLog);
				main.msgAdapter.notifyDataSetChanged();
				main.msgBoard.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
			}
		});
	}
	
	public void updateListViewEntry(String keyName, Integer typeface, ArrayList<User> listItems) {
    	for (int i = 0; i < listItems.size(); i++) {
    		if (listItems.get(i).text.equals(keyName)) {
    			listItems.set(i, new User(keyName, typeface));
    			break;
    		}
    	}
	}
    
    public void sendNotification(Context context, String message) {
    	sendNotification(context, message, Toast.LENGTH_SHORT);
    }
    
    public void sendNotification(Context context, String message, int length) {
    	Toast.makeText(context, message, length).show();
    }
    
	public String exec(String command) {
		SSLSocket socket = null;

	    try {
            socket = getConnection();
            socket.setSoTimeout(10000); // set timeout to 10 seconds
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
            		new InputStreamReader(socket.getInputStream())
            );
            if (D) Log.e(TAG, "Sending command: "+command);
            out.println(command);
           	return in.readLine();
	    } catch (IOException e) {
            Log.e(TAG, e.getMessage());
	    } catch (NoSuchAlgorithmException e) {
	    	Log.e(TAG, e.getMessage());
		} catch (KeyManagementException e) {
			Log.e(TAG, e.getMessage());
		} finally {
			close(socket);
		}
		return null;
	}
	
	public boolean sendMessage(DataBaseAdapter db, String me, String friend, String message) {
		SSLSocket socket = null;
	    try {
	    	socket = getConnection();
	    	String publicKey = db.getPublicKey(friend);
	    	PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            if (D) Log.e(TAG, "Sending message to "+friend);
            if (D) Log.e(TAG, "PublicKey: "+publicKey);
            message = encryption.encrypt(publicKey, message);
            if (message != null) {
            	out.println("MSG("+me+","+getMd5Sum(friend)+","+message+")");
            	return true;
            }
	    } catch(NullPointerException e) {
	    	Log.e(TAG, e.getMessage());
	    	e.printStackTrace();
	    } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
	    } catch (NoSuchAlgorithmException e) {
	    	Log.e(TAG, e.getMessage());
	    	e.printStackTrace();
		} catch (KeyManagementException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		} finally {
			close(socket);
		}
		return false;
	}
	
	public void sendPubKey(DataBaseAdapter db) {
		SSLSocket socket = null;
		String nickName = getMd5Sum(db.getContactName(0));
		String publicKey = db.getPublicKey(0);
		
	    try {
            socket = getConnection();
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedInputStream bis = new BufferedInputStream(
            		new ByteArrayInputStream(publicKey.getBytes()));
            OutputStream os = socket.getOutputStream();
            
            out.println("PUBKEY("+nickName+")");
            if (D) Log.e(TAG, "Sending public key...");
	        int aByte;
	        while ((aByte = bis.read()) != -1) os.write(aByte);
	        os.flush();
	        os.close();
	        bis.close();
	    } catch (IOException e) {
            Log.e(TAG, e.getMessage());
	    } catch (NoSuchAlgorithmException e) {
	    	Log.e(TAG, e.getMessage());
		} catch (KeyManagementException e) {
			Log.e(TAG, e.getMessage());
		} finally {
			close(socket);
		}
    }
	
	public byte[] receivePubKey(String nickName) {
		byte[] result = null;
        SSLSocket socket = null;
	    try {
			socket = getConnection();
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println("GETKEY("+nickName+")");
			
			int aByte;
			InputStream is = socket.getInputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();	        
	        while ((aByte = is.read()) != -1) baos.write(aByte);
	        baos.flush();
	        baos.close();
	        is.close();
	        result = baos.toByteArray();
		} catch (KeyManagementException e) {
			Log.e(TAG, e.getMessage());
		} catch (UnknownHostException e) {
			Log.e(TAG, e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		} finally {
			close(socket);
		}
		return result;
    }
	
	public Handler getListenerHandler(final Main main) {
		return new Handler() {
			public void handleMessage(Message message) {
	    		Bundle data = message.getData();
	    		if (D) Log.e(TAG, "++ Initial Message ++");
	    		if (message.arg1 == Activity.RESULT_OK && data != null) {
	    			String iam;
	    			if ((iam = data.getString(Listener.IAM)) == null) {
	    				if (D) Log.e(TAG, "++ Message MSG ++");
						updateChat(main);
	    			} else {
	    				if (D) Log.e(TAG, "++ Message IAM ++");
	    				new AddContact(main, true).execute(iam);
	    			}
	    		}
	    	}};	
	}
	    
    public ServiceConnection conn = new ServiceConnection() {
    	Messenger messenger = null;
    	
    	public void onServiceConnected(ComponentName className, IBinder binder) {
    		messenger = new Messenger(binder);
    	}
    	
    	public void onServiceDisconnected(ComponentName className) {
    		messenger = null;
    	}
    };
}
