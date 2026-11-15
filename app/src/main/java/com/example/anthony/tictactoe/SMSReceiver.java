package com.example.anthony.tictactoe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsMessage;
import java.util.Locale;

public class SMSReceiver extends BroadcastReceiver {
    static final String GAME_COMMAND = "com.example.anthony.tictactoe.GAME_COMMAND";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) return;
        SmsMessage[] parts = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        if (parts == null || parts.length != 1 || parts[0] == null) return;
        String number = parts[0].getOriginatingAddress();
        String body = parts[0].getMessageBody();
        if (number == null || body == null) return;
        String command = body.trim().toLowerCase(Locale.ROOT);
        if (!command.matches("initiate|accepted|rejected|ready|exit|btn[1-9]")) return;
        Intent gameEvent = new Intent(GAME_COMMAND).putExtra("number", number).putExtra("command", command);
        LocalBroadcastManager.getInstance(context).sendBroadcastSync(gameEvent);
    }
}
