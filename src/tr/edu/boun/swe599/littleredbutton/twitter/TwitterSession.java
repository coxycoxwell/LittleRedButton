/*
 * 
 * Bogazici University
 * MS in Software Engineering
 * SWE 599 - Project
 * 
 * Mustafa Goksu GURKAS
 * ID: 2011719225
 * 
 * */

package tr.edu.boun.swe599.littleredbutton.twitter;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import tr.edu.boun.swe599.littleredbutton.twitter.Constants;

public class TwitterSession {
	private SharedPreferences mSharedPreferences;
	private Context mContext;
	
	public TwitterSession(Context context){
		mContext=context;
		mSharedPreferences = mContext.getSharedPreferences("tr.edu.boun.swe599.littleredbutton.twitter", 0);
	}
	
	public void saveSession(AccessToken accessToken, Twitter twitter) throws TwitterException{
		// Shared Preferences
		long userID = accessToken.getUserId();
		User user = twitter.showUser(userID);
		String username = user.getName();
		Editor e = mSharedPreferences.edit();
		// After getting access token, access token secret
		// store them in application preferences
		e.putString(Constants.PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
		e.putString(Constants.PREF_KEY_OAUTH_SECRET,accessToken.getTokenSecret());
		// Store login status - true
		e.putBoolean(Constants.PREF_KEY_TWITTER_LOGIN, true);
		e.putString(Constants.TWITTER_USER_NAME, username);
		e.commit(); // save changes
		Log.e("Twitter OAuth Token", "> " + accessToken.getToken());
	}
	public boolean isTwitterLoggedInAlready() {
		// return twitter login status from Shared Preferences
		return mSharedPreferences.getBoolean(Constants.PREF_KEY_TWITTER_LOGIN, false);
	}
	public String getDefaultAccessToken(){
		return mSharedPreferences.getString(Constants.PREF_KEY_OAUTH_TOKEN, "");
		
	}
	public String getDefaultSecret(){
		return mSharedPreferences.getString(Constants.PREF_KEY_OAUTH_SECRET, "");
	}
	public String getUserName(){
		return mSharedPreferences.getString(Constants.TWITTER_USER_NAME, "");
	}
	public void logout(){
		// clear twitter credentials from sharedpreferences
		Editor e = mSharedPreferences.edit();
		e.remove(Constants.PREF_KEY_OAUTH_TOKEN);
		e.remove(Constants.PREF_KEY_OAUTH_SECRET);
		e.remove(Constants.PREF_KEY_TWITTER_LOGIN);
		e.commit();
	}
}
