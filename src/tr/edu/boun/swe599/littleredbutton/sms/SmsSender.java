package tr.edu.boun.swe599.littleredbutton.sms;

import java.util.ArrayList;

import tr.edu.boun.swe599.littleredbutton.sms.SmsSentReceiver;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

public class SmsSender {
	
	private Context context;
	
	public SmsSender(Context context) {
		this.context = context;
	}
	
	public boolean sendSMS(String [] phoneNumber, String message) {
	    ArrayList<PendingIntent> sentPendingIntents = new ArrayList<PendingIntent>();
	    PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
	            new Intent(context, SmsSentReceiver.class), 0);
	    try {
	        SmsManager sms = SmsManager.getDefault();
	        ArrayList<String> mSMSMessage = sms.divideMessage(message);
	        
	        for (int i = 0; i < mSMSMessage.size(); i++)
        		sentPendingIntents.add(i, sentPI);
            
	        for (int i = 0; i < phoneNumber.length; i++) { 
	        	sms.sendMultipartTextMessage(phoneNumber[i], null, mSMSMessage,
	        			sentPendingIntents, null);
	        }
	        
	        return true;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	}
}
