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

package tr.edu.boun.swe599.littleredbutton.recipients;

import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLLiteHelper extends SQLiteOpenHelper {
	// Handles the db operations of recipient db table
	private static final String TABLE_RECIPIENT = "recipient";
	private static final String KEY_RECIPIENTNAME = "recipientname";
	private static final String KEY_RECIPIENTEMAILADDRESS = "recipientemailaddress";
	private static final String KEY_RECIPIENTPHONENUMBER = "recipientphonenumber";

	private static final String[] COLUMNS = { KEY_RECIPIENTNAME,
			KEY_RECIPIENTEMAILADDRESS, KEY_RECIPIENTPHONENUMBER };

	// Database Version
	private static final int DATABASE_VERSION = 1;
	// Database Name
	private static final String DATABASE_NAME = "RecipientDB";

	public MySQLLiteHelper (Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// create recipient table on db
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_RECIPIENT_TABLE = "CREATE TABLE recipient ( "
				+ "recipientname TEXT PRIMARY KEY, "
				+ "recipientemailaddress TEXT, "
				+ "recipientphonenumber TEXT )";
		db.execSQL(CREATE_RECIPIENT_TABLE);
	}

	// if the application is updated drops the db
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS recipient");
		this.onCreate(db);
	}

	public void addRecipient(Recipient recipient) {
		Log.d("addRecipient", recipient.toString());

		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_RECIPIENTNAME, recipient.getRecipientName()); 
		values.put(KEY_RECIPIENTEMAILADDRESS,
				recipient.getRecipientEmailAddress());
		values.put(KEY_RECIPIENTPHONENUMBER,
				recipient.getRecipientPhoneNumber());

		db.insert(TABLE_RECIPIENT, // table
				null,
				values); // key/value -> keys = column names/ values = column
							// values
		db.close();
	}

	public Recipient getRecipient(String recipientName) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_RECIPIENT, // a. table
				COLUMNS, // b. column names
				" recipientname = ?", // c. selections
				new String[] { String.valueOf(recipientName) }, // d. selections
				null, // e. group by
				null, // f. having
				null, // g. order by
				null); // h. limit
		if (cursor != null)
			cursor.moveToFirst();

		// construct returning recipient object
		Recipient recipient = new Recipient();
		recipient.setRecipientName(cursor.getString(0));
		recipient.setRecipientEmailAddress(cursor.getString(1));
		recipient.setRecipientPhoneNumber(cursor.getString(2));

		Log.d("getRecipient(" + recipientName + ")", recipientName.toString());
		return recipient;
	}

	// returns all recipients in db table
	public List<Recipient> getAllRecipient() {
		List<Recipient> recipients = new LinkedList<Recipient>();

		String query = "SELECT  * FROM " + TABLE_RECIPIENT;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);
		Recipient recipient = null;
		if (cursor.moveToFirst()) {
			do {
				recipient = new Recipient();
				recipient.setRecipientName(cursor.getString(0));
				recipient.setRecipientEmailAddress(cursor.getString(1));
				recipient.setRecipientPhoneNumber(cursor.getString(2));

				recipients.add(recipient);
			} while (cursor.moveToNext());
		}

		Log.d("getAllRecipients", recipients.toString());
		return recipients;
	}

	// update a recipient with its new values
	public int updateRecipient(Recipient recipient) {

		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("recipientemailaddress",
				recipient.getRecipientEmailAddress()); 
		values.put("recipientphonenumber", recipient.getRecipientPhoneNumber()); 

		int i = db.update(TABLE_RECIPIENT, // table
				values, // column/value
				KEY_RECIPIENTNAME + " = ?", // selections
				new String[] { String.valueOf(recipient.getRecipientName()) }); // selection
		db.close();

		return i;
	}

	// deletes a recipient from the table
	public void deleteRecipient(Recipient recipient) {

		SQLiteDatabase db = this.getWritableDatabase();

		db.delete(TABLE_RECIPIENT, // table name
				KEY_RECIPIENTNAME + " = ?", // selections
				new String[] { String.valueOf(recipient.getRecipientName()) }); // selections
		db.close();

		Log.d("deleteRecipient", recipient.toString());
	}
}
