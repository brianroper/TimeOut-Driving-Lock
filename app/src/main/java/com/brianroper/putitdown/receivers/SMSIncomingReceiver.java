package com.brianroper.putitdown.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.ObbInfo;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;

import com.brianroper.putitdown.R;

import java.util.List;

/**
 * Created by brianroper on 9/3/17.
 */

public class SMSIncomingReceiver extends BroadcastReceiver {

    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle bundle = intent.getExtras();

        try{
            if(bundle != null){
                Object[] pdus = (Object[]) bundle.get("pdus");

                for (int i = 0; i < pdus.length; i++) {
                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();

                    sendAutoReply(phoneNumber);
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * send auto reply to number of incoming message
     */
    public void sendAutoReply(String phoneNumber){
        SmsManager sms = SmsManager.getDefault();

        List<String> messages = sms.divideMessage(mContext.getResources().getString(R.string.auto_reply_text));

    }

    /**
     *
     SmsManager sms = SmsManager.getDefault();
     // if message length is too long messages are divided
     List<String> messages = sms.divideMessage(message);
     for (String msg : messages) {

     PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT"), 0);
     PendingIntent deliveredIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED"), 0);
     sms.sendTextMessage(phone, null, msg, sentIntent, deliveredIntent);

     }
     */
}
