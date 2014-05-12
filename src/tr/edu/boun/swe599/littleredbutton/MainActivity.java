package tr.edu.boun.swe599.littleredbutton;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import tr.edu.boun.swe599.littleredbutton.facebook.FacebookLoginButton;
import tr.edu.boun.swe599.littleredbutton.mail.AsyncMailSender;
import tr.edu.boun.swe599.littleredbutton.maps.ShowOnMapActivity;
import tr.edu.boun.swe599.littleredbutton.recipients.MySQLLiteHelper;
import tr.edu.boun.swe599.littleredbutton.recipients.Recipient;
import tr.edu.boun.swe599.littleredbutton.recipients.RecipientActivity;
import tr.edu.boun.swe599.littleredbutton.settings.SettingsActivity;
import tr.edu.boun.swe599.littleredbutton.sms.AsyncSmsSender;
import tr.edu.boun.swe599.littleredbutton.twitter.ConnectionDetector;
import tr.edu.boun.swe599.littleredbutton.twitter.Constants;
import tr.edu.boun.swe599.littleredbutton.twitter.TwitterSession;
import tr.edu.boun.swe599.littleredbutton.twitter.TwitterWorker;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphPlace;
import com.facebook.model.GraphUser;
import com.facebook.widget.FacebookDialog;

public class MainActivity extends Activity implements LocationListener {

	private static final List<String> PERMISSIONS = Arrays
			.asList("publish_actions");
	private List<String> recipientEmailList;
	private List<String> recipientPhoneNumberList;
	private List<GraphUser> tags;
	
	// flag for GPS status
	private boolean isGPSEnabled = false;
	// flag for network status
	private	boolean isNetworkEnabled = false;
	// flag for location gets
	private	boolean canGetLocation = false;
	private boolean pendingPublishReauthorization = false;
	private boolean canPresentShareDialog;
	
	private MySQLLiteHelper db;

	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
	// The minimum time between updates in milliseconds
	private static final long MIN_TIME_BW_UPDATES = 1000 * 50 * 1; // 50 seconds
	// Declaring a Location Manager
	public static final int REQUEST_CODE = 100;
	private static final int RESULT_SETTINGS = 152;
	private static final String PERMISSION = "publish_actions";
	private final String PENDING_ACTION_BUNDLE_KEY = "com.facebook.samples.hellofacebook:PendingAction";
	private String pictureFileName;
	
	protected LocationManager locationManager;

	private GraphUser user;
	
	private PendingAction pendingAction = PendingAction.NONE;
	private GraphPlace place;
	
	private Twitter twitter;
	private RequestToken requestToken;
	private TwitterSession twitterSession;
	private AuthenticationTask mAuthTask;
	private RetriveAcessTokenTask mAccessTokenTask;

	private enum PendingAction {
		NONE, POST_PHOTO
	}

	private UiLifecycleHelper uiHelper;
	
	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	private FacebookDialog.Callback dialogCallback = new FacebookDialog.Callback() {
		@Override
		public void onError(FacebookDialog.PendingCall pendingCall,
				Exception error, Bundle data) {
			Log.d("FacebookDialogCallback", String.format("Error: %s", error.toString()));
		}

		@Override
		public void onComplete(FacebookDialog.PendingCall pendingCall,
				Bundle data) {
			Log.d("FacebookDialogCallback", "Success!");
		}
	};

	private Button littleRedButton;
	private Button mapsButton;
	private Button recipientsButton;
	private FacebookLoginButton fbLoginButton;
	private Button loginTwitterButton;
	private TextView infoLabel;

	private Camera camera;
	
	final PictureCallback pcallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			try {
				// async task for storing the photo
				new SavePhotoTask(getApplicationContext(), data)
						.execute();
			} catch (Exception e) {
				// some exceptionhandling
			}
		}
	};	
	
	private OnClickListener littleRedButtonListener = new OnClickListener() {
		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		@Override
		public void onClick(View v) {
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			boolean trackMe = sharedPrefs.getBoolean("pref_key_track_me", false);
			
			if(trackMe) {
				Thread t = new Thread(new Runnable() {
					@Override
	                public void run() {
	                    trackHim();
	                }	
				});
				t.start();
			}
			else
				camera.takePicture(null, null, pcallback);
		}
	};
	
	private void trackHim()
	{
		do {
			camera.takePicture(null, null, pcallback);
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while (true);
	}

	private void startNotifications() {

		Toast.makeText(this, "Starting to send notifications...",
				Toast.LENGTH_SHORT).show();

		recipientEmailList.clear();
		recipientPhoneNumberList.clear();

		List<Recipient> recipientlist = db.getAllRecipient();
		for (Recipient recipient : recipientlist) {
			recipientEmailList.add(recipient.getRecipientEmailAddress());
			recipientPhoneNumberList.add(recipient.getRecipientPhoneNumber());
		}

		Session session = Session.getActiveSession();
		boolean facebookInUse = (session != null && session.isOpened());
		boolean twitterInUse = twitterSession.isTwitterLoggedInAlready();

		if (facebookInUse) {
			Log.d("FB", "startnotifications");
			performPublish(PendingAction.POST_PHOTO, false);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		if (twitterInUse) {
			TwitterWorker tw = new TwitterWorker(MainActivity.this);
			tw.sendTweet(getPostBody(), getPictureFileName());
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		new AsyncMailSender(MainActivity.this, getCoordinatesString(),
				getPictureFileName(), recipientEmailList).execute();

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		new AsyncSmsSender(MainActivity.this, getCoordinatesString(),
				recipientPhoneNumberList).execute();
	}

	private OnClickListener recipientsButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getApplicationContext(),
					RecipientActivity.class);
			startActivity(intent);
		}
	};

	private OnClickListener mapsButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getApplicationContext(),
					ShowOnMapActivity.class);
			startActivity(intent);
		}
	};

	private OnClickListener loginTwitterButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			loginToTwitter();
		}
	};

	public void loginToTwitter() {
		if (mAuthTask != null)
			return;
		if (!twitterSession.isTwitterLoggedInAlready()) {
			mAuthTask = new AuthenticationTask();
			mAuthTask.execute((Void) null);
		} else {
			Toast.makeText(this, "Logged out from Twitter", Toast.LENGTH_SHORT)
					.show();
			twitterSession.logout();
			updateUI();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		db = new MySQLLiteHelper(this);

		recipientEmailList = new ArrayList<String>();
		recipientPhoneNumberList = new ArrayList<String>();

		// get shared preferences
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		// first time run?
		if (pref.getBoolean("firstTimeRun", true)) {
			// start the preferences activity
			startActivity(new Intent(getBaseContext(), SettingsActivity.class));
			// get the preferences editor
			SharedPreferences.Editor editor = pref.edit();
			// avoid for next run
			editor.putBoolean("firstTimeRun", false);
			editor.commit();
		}

		// do we have a camera?
		if (!getPackageManager()
				.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			Toast.makeText(this, "No camera on this device", Toast.LENGTH_LONG)
					.show();
		} else {

			try {
				SurfaceView sw = new SurfaceView(getApplicationContext());
				SurfaceHolder mySurfaceHolder = sw.getHolder();
				camera = Camera.open();
				camera.setPreviewDisplay(mySurfaceHolder);
				camera.startPreview();
			} catch (Exception w) {
			}
		}

		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			String name = savedInstanceState
					.getString(PENDING_ACTION_BUNDLE_KEY);
			pendingAction = PendingAction.valueOf(name);
		}

		setContentView(R.layout.activity_main);

		littleRedButton = (Button) findViewById(R.id.littleRedButton);
		littleRedButton.setOnClickListener(littleRedButtonListener);

		mapsButton = (Button) findViewById(R.id.mapsButton);
		mapsButton.setOnClickListener(mapsButtonListener);

		recipientsButton = (Button) findViewById(R.id.recipientsButton);
		recipientsButton.setOnClickListener(recipientsButtonListener);

		loginTwitterButton = (Button) findViewById(R.id.loginTwitterButton);
		loginTwitterButton.setOnClickListener(loginTwitterButtonListener);

		fbLoginButton = (FacebookLoginButton) findViewById(R.id.loginFacebookButton);
		fbLoginButton
				.setUserInfoChangedCallback(new FacebookLoginButton.UserInfoChangedCallback() {
					@Override
					public void onUserInfoFetched(GraphUser user) {
						MainActivity.this.user = user;
						updateUI();
						handlePendingAction();
					}
				});

		infoLabel = (TextView) findViewById(R.id.infoLabel);

		// Can we present the share dialog for regular links?
		canPresentShareDialog = FacebookDialog.canPresentShareDialog(this,
				FacebookDialog.ShareDialogFeature.SHARE_DIALOG);
		
		twitterSession = new TwitterSession(this);

		// Check if Internet present
		if (!ConnectionDetector.isConnectingToInternet(this)) {
			// Internet Connection is not present
			Toast.makeText(MainActivity.this,
					"Please check your Internet connection!", Toast.LENGTH_LONG)
					.show();
			return;
		}

		// Check if twitter keys are set
		if (TextUtils.isEmpty(Constants.TWITTER_CONSUMER_KEY)
				|| TextUtils.isEmpty(Constants.TWITTER_CONSUMER_SECRET)) {
			// Internet Connection is not present
			Toast.makeText(MainActivity.this, "Check Twitter oAuth tokens",
					Toast.LENGTH_LONG).show();
			// stop executing code by return
			return;
		}

		Location location = getLocation();
		if (location != null)
			infoLabel.setText("Coordinates:\nLat: "
					+ String.valueOf(location.getLatitude()) + "\nLon: "
					+ String.valueOf(location.getLongitude()));
	}

	@Override
	protected void onResume() {
		super.onResume();
		uiHelper.onResume();
		updateUI();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);

		outState.putString(PENDING_ACTION_BUNDLE_KEY, pendingAction.name());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.d("Main Activity", "Method calldes=>");
		Uri uri = intent.getData();
		if (mAccessTokenTask != null)
			return;
		if (!twitterSession.isTwitterLoggedInAlready()) {
			if (uri != null && uri.getScheme().equals(Constants.TWITTER_SCHEME)) {
				// oAuth verifier
				Log.d("Main Activity", "callback: "
						+ uri.getScheme().toString());
				String verifier = uri
						.getQueryParameter(Constants.URL_TWITTER_OAUTH_VERIFIER);
				mAccessTokenTask = new RetriveAcessTokenTask();
				mAccessTokenTask.execute(verifier);
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d("Main Activity", "onActivityResult=>");
		if (requestCode == RESULT_SETTINGS)
			return;

		uiHelper.onActivityResult(requestCode, resultCode, data, dialogCallback);

		Session session = Session.getActiveSession();
		if (session != null) {
			// Check for publish permissions
			List<String> permissions = session.getPermissions();
			if (!isSubsetOf(PERMISSIONS, permissions)) {
				pendingPublishReauthorization = true;
				Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(
						this, PERMISSIONS);
				session.requestNewPublishPermissions(newPermissionsRequest);
				return;
			}
		}
	}

	private boolean isSubsetOf(Collection<String> subset,
			Collection<String> superset) {
		for (String string : subset) {
			if (!superset.contains(string)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	private void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		if (pendingAction != PendingAction.NONE
				&& (exception instanceof FacebookOperationCanceledException || exception instanceof FacebookAuthorizationException)) {
			new AlertDialog.Builder(MainActivity.this).setTitle("Cancelled")
					.setMessage("Permission not granted")
					.setPositiveButton("OK", null).show();
			pendingAction = PendingAction.NONE;
		} else if (state == SessionState.OPENED_TOKEN_UPDATED) {
			handlePendingAction();
		}
		updateUI();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent i = new Intent(this, SettingsActivity.class);
			startActivityForResult(i, RESULT_SETTINGS);
			break;
		}
		return true;
	}

	public Location getLocation() {
		Location location = null;
		try {
			locationManager = (LocationManager) this
					.getSystemService(LOCATION_SERVICE);

			// getting GPS status
			isGPSEnabled = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
			// getting network status
			isNetworkEnabled = locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (!isGPSEnabled && !isNetworkEnabled) {
				// no network provider is enabled
			} else {
				this.canGetLocation = true;
				if (isNetworkEnabled) {
					locationManager.requestLocationUpdates(
							LocationManager.NETWORK_PROVIDER,
							MIN_TIME_BW_UPDATES,
							MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
					Log.d("Network", "Network Enabled");
					if (locationManager != null) {
						location = locationManager
								.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					}
				}
				// if GPS Enabled get lat/long using GPS Services
				if (isGPSEnabled) {
					if (location == null) {
						locationManager.requestLocationUpdates(
								LocationManager.GPS_PROVIDER,
								MIN_TIME_BW_UPDATES,
								MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
						Log.d("GPS", "GPS Enabled");
						if (locationManager != null) {
							location = locationManager
									.getLastKnownLocation(LocationManager.GPS_PROVIDER);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return location;
	}

	public void stopUsingGPS() {
		if (locationManager != null) {
			locationManager.removeUpdates(this);
		}
	}

	public boolean canGetLocation() {
		return this.canGetLocation;
	}

	public void showSettingsAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

		// Setting Dialog Title
		alertDialog.setTitle("GPS settings");
		// Setting Dialog Message
		alertDialog
				.setMessage("GPS is not enabled. Do you want to go to settings menu?");
		// On pressing Settings button
		alertDialog.setPositiveButton("Settings",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(
								Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						startActivity(intent);
					}
				});

		// on pressing cancel button
		alertDialog.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

		// Showing Alert Message
		alertDialog.show();
	}

	@Override
	public void onLocationChanged(Location location) {
		if (location != null)
			infoLabel.setText("Coordinates:\nLat: "
					+ String.valueOf(location.getLatitude()) + "\nLon: "
					+ String.valueOf(location.getLongitude()));
		Toast.makeText(
				this,
				"Lat: " + String.valueOf(location.getLatitude()) + " Lon: "
						+ String.valueOf(location.getLatitude()),
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	public String getCoordinatesString() {
		Location location = getLocation();
		if (location != null)
			return "http://maps.google.com/?q=" + location.getLatitude() + ","
					+ location.getLongitude();
		else
			return "Unkown!";
	}

	public void updateUI() {
		if (twitterSession.isTwitterLoggedInAlready())
			loginTwitterButton.setText("Disable Twitter");
		else
			loginTwitterButton.setText("Enable Twitter");
	}

	@SuppressWarnings("incomplete-switch")
	private void handlePendingAction() {
		Log.d("FB", "handlependingaction");
		PendingAction previouslyPendingAction = pendingAction;
		// These actions may re-set pendingAction if they are still pending, but
		// we assume they
		// will succeed.
		pendingAction = PendingAction.NONE;

		switch (previouslyPendingAction) {
		case POST_PHOTO:
			postPhoto();
			break;
		}
	}

	private void showPublishResult(String message, GraphObject result,
			FacebookRequestError error) {
		if (error == null)
			Toast.makeText(this, "Posted to Facebook successfully!",
					Toast.LENGTH_SHORT).show();
		else
			Toast.makeText(this, error.getErrorMessage(), Toast.LENGTH_LONG)
					.show();
	}

	private void postPhoto() {
		Log.d("FB", "postphoto");
		Bitmap image = BitmapFactory.decodeFile(getPictureFileName());
		if (hasPublishPermission()) {
			Request request = null;

			request = Request.newUploadPhotoRequest(Session.getActiveSession(),
					image, new Request.Callback() {
						@Override
						public void onCompleted(Response response) {
							showPublishResult("Post photo",
									response.getGraphObject(),
									response.getError());
						}
					});

			Bundle parameters = request.getParameters();

			parameters.putString("message", getPostBody());
			request.setParameters(parameters);
			Toast.makeText(MainActivity.this,
					"Posting to Facebook", Toast.LENGTH_SHORT)
					.show();
			request.executeAsync();
			Log.d("FB", "executeasync");
		} else {
			pendingAction = PendingAction.POST_PHOTO;
		}
	}

	private String getPostBody() {
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		String postBody = sharedPrefs.getString("pref_key_message_text", null);
		if (postBody == null || postBody.equals(""))
			postBody = "\n";
		else
			postBody += "\n";
		postBody += "I'm at " + getCoordinatesString();

		return postBody;
	}

	private boolean hasPublishPermission() {
		Session session = Session.getActiveSession();
		return session != null
				&& session.getPermissions().contains("publish_actions");
	}

	private void performPublish(PendingAction action, boolean allowNoSession) {
		Log.d("FB", "performpublish");
		Session session = Session.getActiveSession();
		if (session != null) {
			pendingAction = action;
			if (hasPublishPermission()) {
				// We can do the action right away.
				Log.d("FB", "haspublishpermssion");
				handlePendingAction();
				return;
			} else if (session.isOpened()) {
				Log.d("FB", "requestnewpermission");
				// We need to get new permissions, then complete the action when
				// we get called back.
				session.requestNewPublishPermissions(new Session.NewPermissionsRequest(
						this, PERMISSION));
				return;
			}
		}

		if (allowNoSession) {
			pendingAction = action;
			handlePendingAction();
		}
	}

	String getPictureFileName() {
		return pictureFileName;
	}

	void setPictureFileName(String pictureFileName) {
		this.pictureFileName = pictureFileName;
	}

	public class SavePhotoTask extends AsyncTask<Boolean, Void, File> implements
			PictureCallback {

		Context ctx;
		byte[] data;

		public SavePhotoTask(Context ctx, byte[] data) {
			this.ctx = ctx;
			this.data = data;
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected File doInBackground(Boolean... params) {
			try {
				camera.takePicture(null, null, this);
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(File result) {
			startNotifications();
		}

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			File pictureFileDir = getDir();
			if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {
				Toast.makeText(this.ctx,
						"Can't create directory to save image.",
						Toast.LENGTH_LONG).show();
				return;
			}

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
			String date = dateFormat.format(new Date());
			String photoFile = "Picture_" + date + ".jpg";

			setPictureFileName(pictureFileDir.getPath() + File.separator
					+ photoFile);

			try {
				File pictureFile = new File(getPictureFileName());

				try {
					FileOutputStream fos = new FileOutputStream(pictureFile);
					fos.write(data);
					fos.close();
					Toast.makeText(this.ctx, "Picture has been taken!",
							Toast.LENGTH_SHORT).show();
				} catch (Exception error) {
					Toast.makeText(this.ctx, "Image could not be saved.",
							Toast.LENGTH_LONG).show();
				}
			} catch (final Exception e) {}
		}

		private File getDir() {
			File sdDir = Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
			return new File(sdDir, "LittleRedButton");
		}
	}

	public class RetriveAcessTokenTask extends
			AsyncTask<String, Integer, Boolean> {
		ProgressDialog pDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(MainActivity.this);
			pDialog.setMessage("Loading Twitter...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				AccessToken accessToken = twitter.getOAuthAccessToken(
						requestToken, params[0]);
				if (accessToken != null) {
					twitterSession.saveSession(accessToken, twitter);
					String username = twitterSession.getUserName();
					Log.d("Main Activity", "Twitter username=>" + username);
					return true;
				} else {
					twitterSession.logout();
					return false;
				}

			} catch (TwitterException e) {
				e.printStackTrace();
				Log.e("Main Activity", "TwitterException=>" + e.getMessage());
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			mAccessTokenTask = null;
			pDialog.cancel();

			if (result)
				;
			else
				Toast.makeText(MainActivity.this, "Login again",
						Toast.LENGTH_SHORT).show();

			updateUI();
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			mAccessTokenTask = null;
			pDialog.cancel();
		}
	}

	public class AuthenticationTask extends AsyncTask<Void, Void, Boolean> {
		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {

			super.onPreExecute();
			dialog = new ProgressDialog(MainActivity.this);
			dialog.setMessage("Loading Twitter Login...");
			dialog.show();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			ConfigurationBuilder builder = new ConfigurationBuilder();
			builder.setOAuthConsumerKey(Constants.TWITTER_CONSUMER_KEY);
			builder.setOAuthConsumerSecret(Constants.TWITTER_CONSUMER_SECRET);
			builder.setUseSSL(true);
			Configuration configuration = builder.build();
			TwitterFactory factory = new TwitterFactory(configuration);
			twitter = factory.getInstance();
			try {
				requestToken = twitter
						.getOAuthRequestToken(Constants.TWITTER_CALLBACK_URL);
				Log.d("Main Activity",
						"requestToken=>" + requestToken.toString());
				if (requestToken != null) {
					return true;
				}
			} catch (TwitterException e) {
				e.printStackTrace();
				Log.e("Main Activity", "TwitterException=>" + e.getMessage());
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			mAuthTask = null;
			dialog.cancel();
			if (!result)
				Toast.makeText(MainActivity.this, "Retry", Toast.LENGTH_SHORT)
						.show();
			else {
				Log.d("Main Activity",
						"authenticationURL=>"
								+ Uri.parse(requestToken.getAuthenticationURL()
										.replace("http://", "https://")));
				Intent i = new Intent(Intent.ACTION_VIEW,
						Uri.parse(requestToken.getAuthenticationURL().replace(
								"http://", "https://")));
				startActivity(i);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			mAuthTask = null;
			dialog.cancel();
		}
	}
}
