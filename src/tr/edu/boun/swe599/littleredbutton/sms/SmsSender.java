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

import java.util.ArrayList;

import android.telephony.SmsManager;

public class SmsSender {
	// The worker class which multiparts and sends the SMS
	public boolean sendSMS(String[] phoneNumber, String message) {
		try {
			SmsManager sms = SmsManager.getDefault();
			// Divide SMS into pieces if needed
			ArrayList<String> mSMSMessage = sms.divideMessage(message);

			// for each phoneNumber send the message
			for (int i = 0; i < phoneNumber.length; i++)
				sms.sendMultipartTextMessage(phoneNumber[i], null, mSMSMessage,
						null, null);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
