package com.ksdagile.openapp;

import android.content.Context;

/**
 * Created by user on 05/02/2017.
 */
public class DefaultExceptionHandler implements Thread.UncaughtExceptionHandler {
    Context context;
    public DefaultExceptionHandler(Context _context) {
        context = _context;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        Logger.GetInstance(context).LogError("Uncaught Exception: " + throwable.getMessage());
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
