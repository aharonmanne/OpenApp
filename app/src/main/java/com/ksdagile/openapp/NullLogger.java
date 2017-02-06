package com.ksdagile.openapp;

import android.content.Context;

/**
 * Created by user on 05/02/2017.
 */
public class NullLogger extends Logger {
    public NullLogger(Context context) {
    }

    @Override
    public void LogError(String message) {

    }

    @Override
    public void LogInfo(String message) {

    }
}
