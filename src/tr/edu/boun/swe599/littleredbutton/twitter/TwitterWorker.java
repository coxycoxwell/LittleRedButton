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

import java.io.File;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import tr.edu.boun.swe599.littleredbutton.twitter.ConnectionDetector;
import tr.edu.boun.swe599.littleredbutton.twitter.Constants;

public class TwitterWorker {
	public static final String TAG = "TwitterWorker";
	private TwitterSession session;
	private UpdateTwitterStatusTask mStatusTask;
	private Context context;

	public TwitterWorker(Context context) {
		this.context = context;
		session = new TwitterSession(context);
	}

	public void sendTweet(String message, String pictureToBeSent) {
		if (mStatusTask != null) {
			return;
		}

		if (ConnectionDetector.isConnectingToInternet(context)) {
			mStatusTask = new UpdateTwitterStatusTask();
			mStatusTask.execute(message, pictureToBeSent);
		} else
			Toast.makeText(context, "Twitter not active. Enable Twitter first.", Toast.LENGTH_SHORT)
					.show();
	}

	// Async class used to update user's twitter status
	class UpdateTwitterStatusTask extends AsyncTask<String, String, Boolean> {
		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Toast.makeText(context,
					"Sending tweet", Toast.LENGTH_SHORT)
					.show();
		}

		protected Boolean doInBackground(String... args) {
			Log.d("Tweet Text", "> " + args[0]);
			Log.d("Tweet Pic", "> " + args[1]);
			String status = args[0];
			String fileName = args[1];
			try {
				ConfigurationBuilder builder = new ConfigurationBuilder();
				builder.setOAuthConsumerKey(Constants.TWITTER_CONSUMER_KEY);
				builder.setOAuthConsumerSecret(Constants.TWITTER_CONSUMER_SECRET);
				builder.setUseSSL(true);
				// Access Token
				String access_token = session.getDefaultAccessToken();
				// Access Token Secret
				String access_token_secret = session.getDefaultSecret();
				AccessToken accessToken = new AccessToken(access_token,
						access_token_secret);
				Twitter twitter = new TwitterFactory(builder.build())
						.getInstance(accessToken);
				// Update status
				StatusUpdate st = new StatusUpdate(status);
				st.setMedia(new File(fileName));
				twitter4j.Status response = twitter.updateStatus(st);

				Log.d(TAG, "Response Text=>" + response.getText());
				if (!TextUtils.isEmpty(response.getText()))
					return true;

			} catch (TwitterException e) {
				// Error in updating status
				Log.e(TAG, "TwitterException=>" + e.getMessage());
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean results) {
			// dismiss the dialog
			mStatusTask = null;
			if (results) {
				Toast.makeText(context, "Tweeted successfully!",
						Toast.LENGTH_SHORT).show();
			} else
				Toast.makeText(context, "Retry", Toast.LENGTH_SHORT).show();
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			mStatusTask = null;
		}
	}
}