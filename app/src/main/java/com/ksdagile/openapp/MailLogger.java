package com.ksdagile.openapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by user on 05/02/2017.
 */
public class MailLogger extends Logger {
    String debugMessage;
    protected MailLogger(Context _context) {
        super();
        context = _context;
    }

    @Override
    public void LogError(String message) {
        debugMessage += message;
        sendMail();
        Log.e(Constants.TAG, message);
    }

    @Override
    public void LogInfo(String message) {
        Calendar calander = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

        debugMessage += simpleDateFormat.format(calander.getTime()) + "\t";
        debugMessage += message;
        int count = debugMessage.length() - debugMessage.replace("\n", "").length();
        if (count >= 10) {
            sendMail();
        } else {
            debugMessage += "\n";
        }
        Log.d(Constants.TAG, message);
    }

    public void sendMail() {
        Uri uri = Uri.parse("mailto:");
        Intent mailIntent = new Intent(Intent.ACTION_SENDTO, uri);
        mailIntent
                .putExtra(Intent.EXTRA_SUBJECT, context.getResources().getString(R.string.displayManageSubject))
                .putExtra(Intent.EXTRA_EMAIL, "aharon.manne@gmail.com")
                .putExtra(Intent.EXTRA_TEXT, debugMessage);
        context.startActivity(mailIntent);
        debugMessage = "";
    }
}
