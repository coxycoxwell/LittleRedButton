package tr.edu.boun.swe599.littleredbutton.mail;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class AsyncMailSender extends AsyncTask<Void, Void, Boolean> {
	Context context;
	String coordinates;
	String pictureFileName;
	List <String> emailList;
	
	public AsyncMailSender (Context context, String coordinates, String pictureFileName, List <String> emailList) {
		this.context = context;
		this.coordinates = coordinates;
		this.pictureFileName = pictureFileName;
		this.emailList = emailList;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		Toast.makeText(context,
				"Sending e-mail", Toast.LENGTH_SHORT)
				.show();
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		if(emailList.size() == 0)
			return false;
		
		MailSender m = new MailSender();

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);		
		String mailUser = sharedPrefs.getString("pref_key_mail_user_name", null);
		if(mailUser == null || mailUser.equals(""))
			mailUser = "redlittlebutton@gmail.com";
		m.set_user(mailUser);
		
		String mailPassword = sharedPrefs.getString("pref_key_mail_password", null);
		if(mailPassword == null || mailPassword.equals(""))
			mailPassword = "QazWsxEdc";
		m.set_pass(mailPassword);
		
		String mailFrom = sharedPrefs.getString("pref_key_username", null);
		if(mailFrom == null || mailFrom.equals(""))
			mailFrom = "Little Red Button Member";
		m.set_from(mailFrom);
		
		m.set_subject("URGENT HELP NEEDED!"); // email subject
		
		String mailBody = sharedPrefs.getString("pref_key_message_text", null);
		if(mailBody == null || mailBody.equals("")) 
			mailBody = "Hi!\n";
		else
			mailBody += "\n\nHi!\n";
		mailBody += mailFrom + " may need your help!\n";
		mailBody += "At: " + coordinates + "\n";
		mailBody += "You may find, if it is available, a view of the scene in the attachment that he/she is currently at.\n";
		mailBody += "\nLittleRedButton Team";
		m.set_body(mailBody); // email body
		
		m.setTo(emailList.toArray(new String[emailList.size()]));
		
		try {
			m.addAttachment(pictureFileName);

			if (m.send()) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			Log.e("MailApp", "Could not send email", e);
			return false;
		}
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		if(result)
			Toast.makeText(context,
					"e-mail was sent successfully!", Toast.LENGTH_SHORT)
					.show();
		else {
			if(emailList.size() == 0)
				Toast.makeText(context,
						"No e-mail recipients!", Toast.LENGTH_SHORT)
						.show();
			else
				Toast.makeText(context, "e-mail was not sent!",
						Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
	}
}