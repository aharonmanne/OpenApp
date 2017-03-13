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

public class OpenAppService extends IntentService{

    private static final String GATE_REQUEST_ID = "com.ksdagile.openap.id.GATE_REQUEST";

    Geofence gateGeofence;
    private PendingIntent gateGeofencePendingIntent;


    private OpenAppService thisService = this;
    private long countdownMS;
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
            intent.setAction(Constants.ACTION_START);
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
        intent.setAction(Constants.ACTION_STOP);
        Toast.makeText(context, context.getResources().getText(R.string.stopping), Toast.LENGTH_LONG).show();
        context.startService(intent);
        gateSettings = GateSettings.GetInstance(context);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (Constants.ACTION_START.equals(action)) {
                handleActionStart();
            } else if (Constants.ACTION_STOP.equals(action)) {
                handleActionStop();
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

        Intent intent = null;
        if (gateSettings.GetIsUseAlarm()) {
            intent = new Intent(getApplicationContext(), AlarmOpenGateService.class);
        } else {
            intent = new Intent(getApplicationContext(), APIOpenGateService.class);
        }

        intent.setAction(Constants.ACTION_START);
        startService(intent);
    }

    /**
     * Stop location tracking.
     */
    private void handleActionStop() {
        Intent intent = null;
        if (gateSettings.GetIsUseAlarm()) {
            intent = new Intent(getApplicationContext(), AlarmOpenGateService.class);
        } else {
            intent = new Intent(getApplicationContext(), APIOpenGateService.class);
        }

        intent.setAction(Constants.ACTION_STOP);
        startService(intent);


    }

}
