package tr.edu.boun.swe599.littleredbutton.recipients;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tr.edu.boun.swe599.littleredbutton.R;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class RecipientActivity extends Activity {

	static final int PICK_CONTACT = 1;
	private TableLayout tableLayout;
	
	private MySQLLiteHelper db;
	
	private List<String> recipientNameList;
	
	private OnClickListener mainButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			finish();
		}
	};

	private OnClickListener allContactsButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(RecipientActivity.this,
					AddRecipientActivity.class);
			startActivity(intent);
		}
	};

	private OnClickListener addRecipientButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(Intent.ACTION_PICK,
					ContactsContract.Contacts.CONTENT_URI);
			startActivityForResult(intent, PICK_CONTACT);
		}
	};

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		db = new MySQLLiteHelper(this);
		recipientNameList = new ArrayList<String>();
		
		setContentView(R.layout.activity_recipient);

		Button mainButton = (Button)findViewById(R.id.mainButton);
		mainButton.setOnClickListener(mainButtonListener);

		Button allContactsButton = (Button)findViewById(R.id.allContactsButton);
		allContactsButton.setOnClickListener(allContactsButtonListener);

		Button addRecipientButton = (Button)findViewById(R.id.addRecipientButton);
		addRecipientButton.setOnClickListener(addRecipientButtonListener);

		tableLayout = (TableLayout) findViewById(R.id.tableLayout);
		
		for (Recipient recipient : db.getAllRecipient()) {
			recipientNameList.add(recipient.getRecipientName());
		}
		
		for(int i=0; i<recipientNameList.size(); i++)  {
	        final TableRow tableRow = new TableRow(this);
	        tableRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
	        String name = recipientNameList.get(i);
	        
	        final TextView text = new TextView(this);
	        text.setText(name);
	        text.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

	        final Button button = new Button(this);
	        button.setText("Delete");
	        button.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
	        button.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v) {
	               final TableRow parent = (TableRow) v.getParent();
	               TextView TextView = (TextView) parent.getChildAt(0);
	      
	               db.deleteRecipient(db.getRecipient(TextView.getText().toString()));
	               tableLayout.removeView(parent);
	            }
	        });

	        tableRow.addView(text);
	        tableRow.addView(button);

	        tableLayout.addView(tableRow);
	    }
		
		recipientNameList.clear();
		

//		setContentView(R.layout.activity_recipient);
//
//		Button mainButton = (Button) findViewById(R.id.mainButton);
//		mainButton.setOnClickListener(mainButtonListener);
//
//		Button allContactsButton = (Button) findViewById(R.id.allContactsButton);
//		allContactsButton.setOnClickListener(allContactsButtonListener);
//
//		Button addRecipientButton = (Button) findViewById(R.id.addRecipientButton);
//		addRecipientButton.setOnClickListener(addRecipientButtonListener);
//
//		tableLayout = (TableLayout) findViewById(R.id.tableLayout);
//
//		SharedPreferences prefs;
//		
//		prefs = this.getSharedPreferences("tr.edu.boun.swe599.littleredbutton",
//				Context.MODE_PRIVATE);
//
//		String recipientNameSet = "tr.edu.boun.swe599.littleredbutton.recipientName";
//		String recipientPhoneSet = "tr.edu.boun.swe599.littleredbutton.recipientPhoneSet";
//		String recipientEmailSet = "tr.edu.boun.swe599.littleredbutton.recipientEmailSet";
//
//		Set<String> nameSet;
//		Set<String> phoneSet;
//		Set<String> emailSet;
//		
//		nameSet = prefs.getStringSet(recipientNameSet, new HashSet<String>());
//		phoneSet = prefs.getStringSet(recipientPhoneSet, new HashSet<String>());
//		emailSet = prefs.getStringSet(recipientEmailSet, new HashSet<String>());
//
//		Object[] nameArray = nameSet.toArray();
//		for (int i = 0; i < nameArray.length; i++) {
//			// Creation row
//			final TableRow tableRow = new TableRow(this);
//			tableRow.setLayoutParams(new TableLayout.LayoutParams(
//					TableLayout.LayoutParams.WRAP_CONTENT,
//					TableLayout.LayoutParams.WRAP_CONTENT));
//
//			String name = (String) nameArray[i];
//
//			// Creation textView
//			final TextView text = new TextView(this);
//			text.setText(name);
//			text.setLayoutParams(new TableRow.LayoutParams(
//					TableRow.LayoutParams.WRAP_CONTENT,
//					TableRow.LayoutParams.WRAP_CONTENT));
//
//			// Creation button
//			final Button button = new Button(this);
//			button.setText("Delete");
//			button.setLayoutParams(new TableRow.LayoutParams(
//					TableRow.LayoutParams.WRAP_CONTENT,
//					TableRow.LayoutParams.WRAP_CONTENT));
//			button.setOnClickListener(new View.OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					final TableRow parent = (TableRow) v.getParent();
//					tableLayout.removeView(parent);
//				}
//			});
//
//			tableRow.addView(text);
//			tableRow.addView(button);
//			tableLayout.addView(tableRow);
//		}
	}

	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);
		
		Recipient recipient = new Recipient();
		
		Uri contact = data.getData();
		ContentResolver cr = getContentResolver();
		Cursor c = managedQuery(contact, null, null, null, null);
		c.moveToFirst();

		String id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));

		recipient.setRecipientName( c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
		
		String phone= null;
		if (Integer.parseInt(c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
			Cursor pCur = cr.query(Phone.CONTENT_URI,null,Phone.CONTACT_ID +" = ?", new String[]{id}, null);
			pCur.moveToNext();
			phone = pCur.getString(pCur.getColumnIndex(Phone.NUMBER));
			recipient.setRecipientPhoneNumber(phone);
		}

		Cursor eCur = cr.query(Email.CONTENT_URI,null,Email.CONTACT_ID +" = ?", new String[]{id}, null);
		eCur.moveToNext();
		String email = eCur.getString(eCur.getColumnIndex(Email.ADDRESS));
		recipient.setRecipientEmailAddress(email);
		
		recipientNameList.clear();
		for(Recipient recp : db.getAllRecipient())
		{
			recipientNameList.add(recp.getRecipientName());
		}

		if(!recipientNameList.contains(recipient.getRecipientName()))
		{
			TableRow tableRow = new TableRow(this);
			tableRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
	
			final TextView text = new TextView(this);
			text.setText(recipient.getRecipientName());
			text.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
			
			db.addRecipient(recipient);
			
			final Button button = new Button(this);
			button.setText("Delete");
			button.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
			button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final TableRow parent = (TableRow) v.getParent();
					TextView TextView = (TextView) parent.getChildAt(0);
					db.deleteRecipient(db.getRecipient(TextView.getText().toString()));
					
					tableLayout.removeView(parent);
				}
			});
	
			tableRow.addView(text);
			tableRow.addView(button);
	
			tableLayout.addView(tableRow);
		}
		
		recipientNameList.clear();

//		Uri contact = data.getData();
//		ContentResolver cr = getContentResolver();
//
//		Cursor c = managedQuery(contact, null, null, null, null);
//		c.moveToFirst();
//		String id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
//
//		String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
//		String phone = null;
//
//		if (Integer.parseInt(c.getString(c
//				.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
//			Cursor pCur = cr.query(Phone.CONTENT_URI, null, Phone.CONTACT_ID
//					+ " = ?", new String[] { id }, null);
//
//			pCur.moveToNext();
//			phone = pCur.getString(pCur.getColumnIndex(Phone.NUMBER));
//		}
//
//		Cursor eCur = cr.query(Email.CONTENT_URI, null, Email.CONTACT_ID
//				+ " = ?", new String[] { id }, null);
//		eCur.moveToNext();
//		String email = eCur.getString(eCur.getColumnIndex(Email.ADDRESS));
//
//		// Creation row
//		TableRow tableRow = new TableRow(this);
//		tableRow.setLayoutParams(new TableLayout.LayoutParams(
//				TableLayout.LayoutParams.WRAP_CONTENT,
//				TableLayout.LayoutParams.WRAP_CONTENT));
//
//		// Creation textView
//		final TextView text = new TextView(this);
//		text.setText(name);
//		text.setLayoutParams(new TableRow.LayoutParams(
//				TableRow.LayoutParams.WRAP_CONTENT,
//				TableRow.LayoutParams.WRAP_CONTENT));
//
//		Set<String> nameSet, nameSet2save;
//		Set<String> phoneSet, phoneSet2save;
//		Set<String> emailSet, emailSet2save;
//		
//		String recipientNameSet = "tr.edu.boun.swe599.littleredbutton.recipientName";
//		String recipientPhoneSet = "tr.edu.boun.swe599.littleredbutton.recipientPhoneSet";
//		String recipientEmailSet = "tr.edu.boun.swe599.littleredbutton.recipientEmailSet";
//		
//		SharedPreferences prefs;
//		
//		prefs = this.getSharedPreferences("tr.edu.boun.swe599.littleredbutton",
//				Context.MODE_PRIVATE);
//		
//		nameSet = prefs.getStringSet(recipientNameSet, new HashSet<String>());
//		nameSet2save = new HashSet<String>(nameSet);
//		phoneSet = prefs.getStringSet(recipientPhoneSet, new HashSet<String>());
//		phoneSet2save = new HashSet<String>(phoneSet);
//		emailSet = prefs.getStringSet(recipientEmailSet, new HashSet<String>());
//		emailSet2save = new HashSet<String>(emailSet);
//		
//		nameSet2save.add(name);
//		phoneSet2save.add(phone);
//		emailSet2save.add(email);
//
//		SharedPreferences.Editor editor = prefs.edit();
//
//		editor.putStringSet(recipientNameSet, nameSet2save).commit();
//		editor.putStringSet(recipientEmailSet, emailSet2save).commit();
//		editor.putStringSet(recipientPhoneSet, phoneSet2save).commit();
//
//		// Creation button
//		final Button button = new Button(this);
//		button.setText("Delete");
//		button.setLayoutParams(new TableRow.LayoutParams(
//				TableRow.LayoutParams.WRAP_CONTENT,
//				TableRow.LayoutParams.WRAP_CONTENT));
//		button.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				final TableRow parent = (TableRow) v.getParent();
//				tableLayout.removeView(parent);
//			}
//		});
//
//		tableRow.addView(text);
//		tableRow.addView(button);
//
//		tableLayout.addView(tableRow);
	}
}
