package tr.edu.boun.swe599.littleredbutton.mail;

import java.util.HashSet;
import java.util.Set;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class AsyncMailSender extends AsyncTask<Void, Void, Boolean> {
	Context context;
	ProgressDialog dialog;
	String coordinates;
	String pictureFileName;
	
	public AsyncMailSender (Context context, String coordinates, String pictureFileName) {
		this.context = context;
		this.coordinates = coordinates;
		this.pictureFileName = pictureFileName;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		dialog = new ProgressDialog(context);
		dialog.setMessage("Sending mail...");
		dialog.show();
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		Set<String> emailSet;
		
		MailSender m = new MailSender(context);

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences prefs = context.getSharedPreferences(
				"tr.edu.boun.swe599.littleredbutton", Context.MODE_PRIVATE);
				  
		String recipientEmailSet = "tr.edu.boun.swe599.littleredbutton.recipientEmailSet";
				  
		emailSet = prefs.getStringSet(recipientEmailSet, new HashSet<String>());
		
		String mailUser = sharedPrefs.getString("pref_key_mail_user_name", null);
		if(mailUser == null || mailUser.equals(""))
			mailUser = "redlittlebutton@gmail.com";
		m.set_user(mailUser);
		
		String mailPassword = sharedPrefs.getString("pref_key_mail_password", null);
		if(mailPassword == null || mailPassword.equals(""))
			mailPassword = "QazWsxEdc";
		m.set_pass(mailPassword);
		
		String mailFrom = sharedPrefs.getString("pref_key_username", null);
		TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String mailPhoneNumber = tMgr.getLine1Number();
		
		if(mailFrom == null || mailFrom.equals(""))
			mailFrom = "Little Red Button Member";
		m.set_from(mailFrom);
		
		m.set_subject("URGENT HELP NEEDED!"); // email subject
		
		String mailBody = sharedPrefs.getString("pref_key_message_text", null);
		if(mailBody == null || mailBody.equals("")) 
			mailBody = "Hi!\n";
		else
			mailBody += "\n\nHi!\n";
		mailBody += mailFrom + " (Phone Number: " + mailPhoneNumber + ") may need your help!\n";
		mailBody += "His/Her current " + coordinates + "\n";
		mailBody += "You may find, if it is available, a view of the scene in the attachment that he/she is currently at\n";
		mailBody += "\nLittleRedButton Team";
		
		m.set_body(mailBody); // email body

		//String[] toArr = { "ggurkas@live.com", "ggurkas@yahoo.com" };
		m.setTo(emailSet.toArray(new String[emailSet.size()]));
		//m.setTo(toArr);
		
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
		dialog.cancel();
		if(result)
			Toast.makeText(context,
					"Email was sent successfully.", Toast.LENGTH_LONG)
					.show();
		else
			Toast.makeText(context, "Email was not sent.",
					Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		dialog.cancel();
	}
}