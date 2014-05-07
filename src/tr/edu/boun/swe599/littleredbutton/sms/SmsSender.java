package tr.edu.boun.swe599.littleredbutton.sms;

import java.util.ArrayList;

import android.telephony.SmsManager;

public class SmsSender {
	public boolean sendSMS(String[] phoneNumber, String message) {
		try {
			SmsManager sms = SmsManager.getDefault();
			ArrayList<String> mSMSMessage = sms.divideMessage(message);

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
