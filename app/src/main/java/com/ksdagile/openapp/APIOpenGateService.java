package com.ksdagile.openapp;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.LocationServices;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class APIOpenGateService extends IntentService  implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    enum DISTANCE2GATE {NEAR_GATE, FAR_GATE};
    private static GateSettings gateSettings;
    private float distance2Gate;
    Handler serviceHandler;
    private boolean isCallMade;
    private GoogleApiClient googleApiClient;
    private boolean isGoogleConnected;
    private Location LastLocation;
    CountDownTimer timer;

    private long countdownMS;

    private OpenAppService.DISTANCE2GATE distance2GateState;
    public APIOpenGateService() {
        super("APIOpenGateService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            gateSettings = GateSettings.GetInstance(this);
            final String action = intent.getAction();
            if (Constants.ACTION_START.equals(action)) {
                handleActionStart();
            } else if (Constants.ACTION_STOP.equals(action)) {
                handleActionStop();
            } else { // Handle Geofence events
                GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
                if (geofencingEvent.hasError()) {
                    String errorMessage =
                            String.format("GeofenceError %d", geofencingEvent.getErrorCode());
                    Log.e(Constants.TAG, errorMessage);
                    return;
                }

                // Get the transition type.
                int geofenceTransition = geofencingEvent.getGeofenceTransition();

                // Test that the reported transition was of interest.
                if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

                    // Get the geofences that were triggered. A single event can trigger
                    // multiple geofences.
                    //List triggeringGeofences = geofencingEvent.getTriggeringGeofences();

                    // Start phone call
                    CallGatePhone();
                    Log.d(Constants.TAG, "Opened gate");
                    // Send notification and log the transition details.
                    //sendNotification(geofenceTransitionDetails);
                    // Log.i(TAG, geofenceTransitionDetails);
                } else {
                    // Log the error.
                    Log.e(Constants.TAG, getResources().getString(R.string.geofence_transition_invalid_type) + ": " + geofencingEvent.toString());
                }
            }
        }
    }

        private void CallGatePhone() {
            GateDialer dial = new GateDialer(gateSettings, this);
            Log.d(Constants.TAG, "Calling Gate");
            dial.dial();
        }
    /**
     *
     *
     */
    private void handleActionStart() {
        serviceHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                CalculateGateDistance();
                if (distance2GateState == OpenAppService.DISTANCE2GATE.NEAR_GATE) {
                    if (!isCallMade)
                        CallGatePhone();
                    countdownMS = 5000; // run 5 second timer to check status
                    isCallMade = true;
                } else {
                    CalcCountdown();
                    isCallMade = false;
                }
                RestartTimer();
            }
        };

        // Create an instance of GoogleAPIClient.
        if (googleApiClient == null) {
            isGoogleConnected = false;
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        googleApiClient.connect();
    }

    private void CalculateGateDistance() {
        try {

            LastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    googleApiClient);
            if (LastLocation != null) {
                float[] results = new float[1];
                Location.distanceBetween(LastLocation.getLatitude(), LastLocation.getLongitude(), gateSettings.GetLatitude(), gateSettings.GetLongitude(), results);
                distance2Gate = results[0];
                Log.d(Constants.TAG, "Distance to gate: " + Float.toString(distance2Gate));
                if (gateSettings.GetActivationDistance() > distance2Gate) {
                    distance2GateState = OpenAppService.DISTANCE2GATE.NEAR_GATE;
                } else {
                    distance2GateState = OpenAppService.DISTANCE2GATE.FAR_GATE;
                }
            }
        } catch (SecurityException secEx) {
            secEx.printStackTrace();
        }
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


    /**
     *
     */
    private void handleActionStop() {
        if (googleApiClient != null) {

            if (timer != null)
                timer.cancel();
        }
    }

    /*
    * GoogleApiClient Support
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        isGoogleConnected = true;
        serviceHandler.sendEmptyMessage(0);
        Log.d(Constants.TAG, "Connected to Google API");
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
