package tr.edu.boun.swe599.littleredbutton.mail;

import java.util.HashSet;
import java.util.Set;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
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
		
		m.set_user(sharedPrefs.getString("pref_key_mail_user_name", null));
		m.set_pass(sharedPrefs.getString("pref_key_mail_password", null));
		m.set_from(sharedPrefs.getString("pref_key_username", null));
		
		m.set_subject("URGENT HELP NEEDED!"); // email subject
		m.set_body(sharedPrefs.getString("pref_key_message_text", null) + "\n" + coordinates); // email body

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