package com.ksdagile.openapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;

/**
 * Created by user on 05/02/2017.
 */

public abstract class Logger {
    Context context;
    private static Logger Instance = null;
    private String debugMessage;

    public static synchronized Logger GetInstance(Context _context) {
        boolean isDebuggable = (_context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        if (Instance == null) {
            if (isDebuggable) {
                Instance = new MailLogger(_context);
            } else {
                Instance = new NullLogger(_context);
            }
        }
        return Instance;
    }

    public abstract void LogError(String message);
    public abstract void LogInfo(String message);


}
