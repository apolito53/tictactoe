package com.example.anthony.tictactoe;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver {
    MainActivity activity = null;
    final IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
    public SMSReceiver(Context context) {
        activity = (MainActivity) context;
        context.registerReceiver(this, intentFilter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        SmsMessage currentMessage = null;

        if (bundle != null) {
            final Object[] pdusObj = (Object[]) bundle.get("pdus");

            if (pdusObj != null) {
                for (int i = 0; i < pdusObj.length; i++) {
                    String format = bundle.getString("format");
                    currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i], format);
                }
            }

            if (currentMessage != null) {
                String senderNum = currentMessage.getDisplayOriginatingAddress();
                String message = currentMessage.getDisplayMessageBody();
                activity.receiveMessage(senderNum, message);
            }
        }
    }
}
