package com.ksdagile.openapp;

/**
 * Created by user on 12/12/2016.
 */


import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class GateDialer {

    enum DialingStates {INIT, STARTED, CALLING, FINISHED};
    static final int PERMISSION_REQUEST_CODE = 1;
    static final String TAG = "GateDialer";
    GateSettings settings;
    Context context;

    public GateDialer(GateSettings _settings, Context _cntx) {
        settings = _settings;
        context = _cntx;
    }

    public void dial() {
        if (getCallState() != TelephonyManager.CALL_STATE_IDLE) {
            Resources res = context.getResources();
            String cannotDialString = res.getString(R.string.cannot_dial);
            Logger.GetInstance(context).LogInfo(cannotDialString);
            return;
        }
        Uri number = Uri.parse("tel:" + settings.GetPhone());
        Intent callIntent = new Intent(Intent.ACTION_CALL, number);
        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            Logger.GetInstance(context).LogInfo("No phone call permission");
        } else {
            MakeCall(callIntent);
        }

    }

    private void MakeCall(Intent callIntent) {
        Logger.GetInstance(context).LogInfo( "Starting call activity");
        context.startActivity(callIntent);
        Logger.GetInstance(context).LogInfo( "Call intent sent");
        try {
            while (getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                Thread.sleep(1000);
                // poll every second
            } // started call
            Logger.GetInstance(context).LogInfo( "Call Started");
            while (getCallState() != TelephonyManager.CALL_STATE_IDLE) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            Logger.GetInstance(context).LogInfo("Call Completed");
        }
    }

    private int getCallState() {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int callStatus = tm.getCallState();
        return callStatus;
    }
}
