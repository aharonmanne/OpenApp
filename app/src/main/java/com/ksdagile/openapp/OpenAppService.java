package com.ksdagile.openapp;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.Calendar;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */

public class OpenAppService extends BroadcastReceiver implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String GATE_REQUEST_ID = "com.ksdagile.openapp.id.GATE_REQUEST";
    private static final String IS_CALL_MADE = "com.ksdagile.openapp.IS_CALL_MADE";

    private GoogleApiClient googleApiClient;
    private boolean isGoogleConnected;
    CountDownTimer timer;

    private long countdownMS;
    private Location lastLocation;
    private float distance2Gate;
    Handler serviceHandler;
    private boolean isCallMade;
    private Context context;
    GateSettings gateSettings;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        gateSettings = GateSettings.GetInstance(context);
        if (intent != null) {
            final String action = intent.getAction();
            Log.d(Constants.TAG, "Action: " + action);
            if (Constants.ACTION_START.equals(action)) {
                distance2Gate = Long.MAX_VALUE;
                handleActionStart();
                Log.d(Constants.TAG, "OpenApp Service Started");
            } else if (Constants.ACTION_STOP.equals(action)) {
                handleActionStop();
                Log.d(Constants.TAG, "OpenApp Service Stopped");
            } else if (Constants.CHECK_LOC.equals(action)) {
                if (gateSettings.GetIsRunning()) {
                    isCallMade = intent.getBooleanExtra(IS_CALL_MADE, false);
                    CheckLocation();
                }
            }
        }
    }


    private void CheckLocation() {
        isGoogleConnected = false;
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

        }
        googleApiClient.connect();
    }

    private void CallGatePhone() {
        GateDialer dial = new GateDialer(gateSettings, context);
        Log.d(Constants.TAG, "Calling Gate");
        dial.dial();
        isCallMade = true;
    }

    /**
     * Configure alarm for next check
     */
    private void handleActionStart() {
        long delayMilli = CalculateDelay();
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, this.getClass());
        i.setAction(Constants.CHECK_LOC);
        i.putExtra(IS_CALL_MADE, isCallMade);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        mgr.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + delayMilli, pi);
    }

    private long CalculateDelay() {
        long delay =
                (long) (distance2Gate / ((100 * 1000) / 60)); // 100 kph = 100*1000 mph = (100*1000)/60 mps
        if (delay < 60000)
            delay = 60000;
        if (delay > (60 * 60 * 1000))
            delay = 60 * 60 * 1000; // no more than an hour
        if (!isGoogleConnected || lastLocation == null)
            delay = 60000;
        return delay;
    }

    private void CalculateGateDistance() {
        try {
            if (lastLocation != null && IsRecent(lastLocation)) {
                float[] results = new float[1];
                Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(), gateSettings.GetLatitude(), gateSettings.GetLongitude(), results);
                distance2Gate = results[0];
                Log.d(Constants.TAG, "Distance to gate: " + Float.toString(distance2Gate));
            } else {
                distance2Gate = Float.MAX_VALUE;
            }

        } catch (SecurityException secEx) {
            secEx.printStackTrace();
        }
    }

    private boolean IsRecent(Location lastLocation) {
        boolean isRecent = false;
        if (lastLocation.getTime() > SystemClock.currentThreadTimeMillis() - 60000) {
            isRecent = true;
        }
        return isRecent;
    }


    /**
     * Stop location tracking.
     */
    private void handleActionStop() {

        gateSettings.SetIsRunning(false);
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, this.getClass());
        i.setAction(Constants.CHECK_LOC);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        mgr.cancel(pi);
    }

    /*
    * GoogleApiClient Support
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(Constants.TAG, "Connected to Google API");
        isGoogleConnected = true;
        try {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    googleApiClient);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        CalculateGateDistance();
        if (distance2Gate < gateSettings.GetActivationDistance()) {
            if (!isCallMade) {
                CallGatePhone();
            }
        } else {
            isCallMade = false;
        }

        handleActionStart(); // set up next alarm
    }

    private void RestartTimer() {
        timer = new CountDownTimer(countdownMS, countdownMS) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                serviceHandler.sendEmptyMessage(0);
            }
        };
        timer.start();
    }

    // countdown to next check assumes 100 kph travel = 100000/(60*60)
    private void CalcCountdown() {
        countdownMS = (long) (distance2Gate / (100000.0 / (60.0 * 60.0)));
        if (countdownMS < 5000)
            countdownMS = 5000;
    }

    @Override
    public void onConnectionSuspended(int i) {
        isGoogleConnected = false;
        Log.d(Constants.TAG, "Connection to Google suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        isGoogleConnected = false;
        Log.d(Constants.TAG, "Connection to Google failed");
    }
}
