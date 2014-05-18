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

package tr.edu.boun.swe599.littleredbutton.sms;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class AsyncSmsSender extends AsyncTask<Void, Void, Boolean> {
	Context context;
	String coordinates;
	List <String> phoneNumberList;

	// Used to send SMS message to a phoneNumberList with the given coordinates
	public AsyncSmsSender (Context context, String coordinates, List<String> phoneNumbers) {
		this.context = context;
		this.coordinates = coordinates;
		this.phoneNumberList = phoneNumbers;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		Toast.makeText(context,
				"Sending SMS", Toast.LENGTH_SHORT)
				.show();
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
		// if no phoneNumbers to send abort
		if(phoneNumberList.size() == 0)
			return false;
		
		SmsSender s = new SmsSender();

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		// construct SMS body
		String smsBody = sharedPrefs.getString("pref_key_message_text", null);
		if(smsBody == null || smsBody.equals("")) 
			smsBody = "Hi!\n";
		else
			smsBody += "\n";
		smsBody += "I need your help!\n";
		smsBody += "At: " + coordinates + "\n";
		// send SMSs
		try {
			return s.sendSMS(phoneNumberList.toArray(new String[phoneNumberList.size()]), smsBody);
		} catch (Exception e) {
			Log.e("SMSApp", "Could not send SMS", e);
			return false;
		}
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		if(result)
			Toast.makeText(context,
					"SMS was sent successfully!", Toast.LENGTH_SHORT)
					.show();
		else {
			if(phoneNumberList.size() == 0)
				Toast.makeText(context,
						"No SMS recipients!", Toast.LENGTH_SHORT)
						.show();
			else
				Toast.makeText(context, "SMS was not sent!",
						Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
	}
}
