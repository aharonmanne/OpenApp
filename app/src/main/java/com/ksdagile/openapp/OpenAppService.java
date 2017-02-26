package com.ksdagile.openapp;

import android.app.IntentService;
import android.app.PendingIntent;
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
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.google.android.gms.location.Geofence.NEVER_EXPIRE;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */

public class OpenAppService extends IntentService implements ResultCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String ACTION_START = "com.ksdagile.openapp.action.START";
    private static final String ACTION_STOP = "com.ksdagile.openapp.action.STOP";
    private static final String GATE_REQUEST_ID = "com.ksdagile.openap.id.GATE_REQUEST";

    Geofence gateGeofence;
    private PendingIntent gateGeofencePendingIntent;

    private GoogleApiClient googleApiClient;
    private boolean isGoogleConnected;
    CountDownTimer timer;

    private OpenAppService thisService = this;
    private long countdownMS;
    private Location LastLocation;
    private static GateSettings gateSettings;
    private float distance2Gate;
    Handler serviceHandler;
    private boolean isCallMade;

    enum DISTANCE2GATE {NEAR_GATE, FAR_GATE}

    ;
    private DISTANCE2GATE distance2GateState;

    public OpenAppService() {
        super("OpenAppService");
    }

    /**
     * Starts this service to perform action START with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void StartGateService(Context context) {
        gateSettings = GateSettings.GetInstance(context);
        if (gateSettings.GetLicenseStatus() == Constants.LICENSE_ALLOWED) {
            Intent intent = new Intent(context, OpenAppService.class);
            intent.setAction(ACTION_START);
            context.startService(intent);
            Toast.makeText(context, context.getResources().getText(R.string.starting), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, context.getResources().getText(R.string.no_license), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Starts this service to perform action Stop with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void StopGateService(Context context) {
        Intent intent = new Intent(context, OpenAppService.class);
        intent.setAction(ACTION_STOP);
        Toast.makeText(context, context.getResources().getText(R.string.stopping), Toast.LENGTH_LONG).show();
        context.startService(intent);
        gateSettings = GateSettings.GetInstance(context);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START.equals(action)) {
                handleActionStart();
                Log.d(Constants.TAG, "OpenApp Service Started");
            } else if (ACTION_STOP.equals(action)) {
                handleActionStop();
                Log.d(Constants.TAG, "OpenApp Service Stopped");
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
     * Set up location tracking and configure open gate trigger.
     */
    private void handleActionStart() {

        serviceHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                CalculateGateDistance();
                if (distance2GateState == DISTANCE2GATE.NEAR_GATE) {
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
                    distance2GateState = DISTANCE2GATE.NEAR_GATE;
                } else {
                    distance2GateState = DISTANCE2GATE.FAR_GATE;
                }
            }
        } catch (SecurityException secEx) {
            secEx.printStackTrace();
        }
    }


    /**
     * Stop location tracking.
     */
    private void handleActionStop() {

        if (googleApiClient != null) {

            if (timer != null)
                timer.cancel();
        }
    }


    @Override
    public void onResult(@NonNull Result result) {

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
