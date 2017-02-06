        package com.ksdagile.openapp;

        import android.app.IntentService;
        import android.app.PendingIntent;
        import android.content.Intent;
        import android.content.Context;
        import android.os.Bundle;
        import android.os.Handler;
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

// TODO: Why does service start automatically after configuration finished
// TODO:
public class OpenAppService extends IntentService implements ResultCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String ACTION_START = "com.ksdagile.openapp.action.START";
    private static final String ACTION_STOP = "com.ksdagile.openapp.action.STOP";
    private static final String GATE_REQUEST_ID = "com.ksdagile.openap.id.GATE_REQUEST";

    Geofence gateGeofence;
    private PendingIntent gateGeofencePendingIntent;

    private GoogleApiClient googleApiClient;
    private boolean isGoogleConnected;
    Timer timer;
    TimerTask timerTask;
    //we are going to use a handler to be able to run in our TimerTask
    final Handler handler = new Handler();
    private OpenAppService thisService = this;
    public OpenAppService() {
        super("OpenAppService");
    }
    Context context;

    /**
     * Starts this service to perform action START with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionStart(Context context) {
        GateSettings statSettings = GateSettings.GetInstance(context);
        if (statSettings.GetLicenseStatus() == Constants.LICENSE_ALLOWED) {
            Intent intent = new Intent(context, OpenAppService.class);
            intent.setAction(ACTION_START);
            context.startService(intent);
            Toast.makeText(context, context.getResources().getText(R.string.starting), Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(context, context.getResources().getText(R.string.no_license), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Starts this service to perform action Stop with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionStop(Context context) {
        Intent intent = new Intent(context, OpenAppService.class);
        intent.setAction(ACTION_STOP);
        Toast.makeText(context, context.getResources().getText(R.string.stopping), Toast.LENGTH_LONG).show();
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            context = this;
            final String action = intent.getAction();
            if (ACTION_START.equals(action)) {
                handleActionStart();
                Logger.GetInstance(this).LogInfo("OpenApp Service Started");
            } else if (ACTION_STOP.equals(action)) {
                handleActionStop();
                Logger.GetInstance(this).LogInfo("OpenApp Service Stopped");
            } else { // Handle Geofence events
                GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
                if (geofencingEvent.hasError()) {
                    String errorMessage =
                            String.format("GeofenceError %d", geofencingEvent.getErrorCode());
                    Logger.GetInstance(context).LogError(errorMessage);
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
                    Logger.GetInstance(context).LogInfo("Opened gate");
                    // Send notification and log the transition details.
                    //sendNotification(geofenceTransitionDetails);
                    // Log.i(TAG, geofenceTransitionDetails);
                } else {
                    // Log the error.
                    Logger.GetInstance(context).LogError(getResources().getString(R.string.geofence_transition_invalid_type) +": " + geofencingEvent.toString() );

                }

            }
        }
    }

    private void CallGatePhone() {
        GateSettings settings = GateSettings.GetInstance(this);
        GateDialer dial = new GateDialer(settings, this);
        Logger.GetInstance(context).LogInfo("Calling Gate");
        dial.dial();
    }

    /**
     * Set up location tracking and configure open gate trigger.
     */
    private void handleActionStart() {
        GateSettings settings = GateSettings.GetInstance(getApplicationContext());
        Geofence.Builder builder = new Geofence.Builder();
        gateGeofence =
                builder
                        // Set the request ID of the geofence. This is a string to identify this
                        // geofence.
                        .setRequestId(GATE_REQUEST_ID)
                        .setCircularRegion(
                                settings.GetLatitude(),
                                settings.GetLongitude(),
                                Constants.GEOFENCE_RADIUS_METERS
                        )
                        .setExpirationDuration(NEVER_EXPIRE)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                        .build();
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

        //set a new Timer
        timer = new Timer();
        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask, 500, 1000); //


    }

    private void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            if (isGoogleConnected) {
                                LocationServices.GeofencingApi.addGeofences(
                                        googleApiClient,
                                        getGeofencingRequest(),
                                        getGeofencePendingIntent()
                                ).setResultCallback(thisService);
                                Logger.GetInstance(context).LogInfo("Added Geofence");
                                timerTask.cancel();
                            } else {
                                Logger.GetInstance(context).LogInfo("Waiting for google services connection");
                            }
                        } catch (SecurityException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        };
    }

    /**
     * Stop location tracking.
     */
    private void handleActionStop() {

        if (googleApiClient != null) {
            LocationServices.GeofencingApi.removeGeofences(
                    googleApiClient,
                    // This is the same pending intent that was used in addGeofences().
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
            LocationServices.GeofencingApi.removeGeofences(
                    googleApiClient,
                    // This is the same pending intent that was used in addGeofences().
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
            if (timerTask != null)
                timerTask.cancel();
        }
    }

    /*
    * Geofence Support
    */

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofence(gateGeofence);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (gateGeofencePendingIntent != null) {
            return gateGeofencePendingIntent;
        }
        Intent intent = new Intent(this, this.getClass());
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
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
        Logger.GetInstance(context).LogInfo("Connected to Google API");
    }

    @Override
    public void onConnectionSuspended(int i) {
        isGoogleConnected = false;
        Logger.GetInstance(context).LogInfo("Connection to Google suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        isGoogleConnected = false;
        Logger.GetInstance(context).LogInfo("Connection to Google failed");
    }
}
