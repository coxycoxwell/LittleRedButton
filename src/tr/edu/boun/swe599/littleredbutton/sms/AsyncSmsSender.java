package tr.edu.boun.swe599.littleredbutton.sms;

import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class AsyncSmsSender extends AsyncTask<Void, Void, Boolean> {

	Context context;
	ProgressDialog dialog;
	String coordinates;
	List <String> phoneNumberList;
	
	public AsyncSmsSender (Context context, String coordinates, List<String> phoneNumbers) {
		this.context = context;
		this.coordinates = coordinates;
		this.phoneNumberList = phoneNumbers;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		dialog = new ProgressDialog(context);
		dialog.setMessage("Sending SMS...");
		dialog.show();
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
		//Set<String> phoneSet;
		SmsSender s = new SmsSender(context);

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
//		SharedPreferences prefs = context.getSharedPreferences(
//				"tr.edu.boun.swe599.littleredbutton", Context.MODE_PRIVATE);
//				  
//		String recipientPhoneSet = "tr.edu.boun.swe599.littleredbutton.recipientPhoneSet";
//				  
//		phoneSet = prefs.getStringSet(recipientPhoneSet, new HashSet<String>());
		
		String memberName = sharedPrefs.getString("pref_key_username", null);
		if(memberName == null || memberName.equals(""))
			memberName = "Little Red Button Member";
		
		String smsBody = sharedPrefs.getString("pref_key_message_text", null);
		if(smsBody == null || smsBody.equals("")) 
			smsBody = "Hi!\n";
		else
			smsBody += "\n\nHi!\n";
		smsBody += memberName + " may need your help!\n";
		smsBody += "I need your help!\n";
		smsBody += "My current " + coordinates + "\n";
		
		try {
			//return s.sendSMS(phoneSet.toArray(new String[phoneSet.size()]), smsBody);
			return s.sendSMS(phoneNumberList.toArray(new String[phoneNumberList.size()]), smsBody);
		} catch (Exception e) {
			Log.e("SMSApp", "Could not send SMS", e);
			return false;
		}
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		dialog.cancel();
		if(result)
			Toast.makeText(context,
					"SMS was sent successfully.", Toast.LENGTH_LONG)
					.show();
		else
			Toast.makeText(context, "SMS was not sent.",
					Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		dialog.cancel();
	}
}
