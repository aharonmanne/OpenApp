package com.ksdagile.openapp;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Semaphore;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class AlarmOpenGateService extends IntentService {

    private static final float MIN_DISTANCE = 100;
    private static final long MIN_INTERVAL = 30 * 1000;
    boolean isCalled;
    float distance2gate;
    GateSettings settings;
    Context context;
    private static final String IS_CALLED = "com.ksdagile.openapp.is_called";

    public AlarmOpenGateService() {
        super("AlarmOpenGateService");
    }

    private static Thread serviceThread;

    private static AlarmManager alarmMgr;
    static private PendingIntent alarmIntent;

    LocationManager locationManager;
    LocationListener listener =
            new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    CalculateDistance2Gate();
                    if (distance2gate <= settings.GetActivationDistance()) {
                        GateDialer dial = new GateDialer(GateSettings.GetInstance(context), context);
                        dial.dial();
                        isCalled = true;
                    } else {
                        isCalled = false;
                    }
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {
                    Log.d(Constants.TAG, "Status: " + s);
                }

                @Override
                public void onProviderEnabled(String s) {
                    Log.d(Constants.TAG, "Provider Enabled: " + s);
                }

                @Override
                public void onProviderDisabled(String s) {
                    Log.d(Constants.TAG, "Provider Disabled: " + s);
                }
            };


    @Override
    protected void onHandleIntent(Intent intent) {
        isCalled = false;
        context = getApplicationContext();
        settings = GateSettings.GetInstance(context);
        if (intent.hasExtra(IS_CALLED)) {
            isCalled = intent.getBooleanExtra(IS_CALLED, false);
        }
        if (intent != null) {
            final String action = intent.getAction();
            Log.d(Constants.TAG, "Intent action: " + action);
            if (Constants.ACTION_START.equals(action)) {
                handleActionStart();
            } else if (Constants.ACTION_STOP.equals(action)) {
                handleActionStop();
            } else if (Constants.ACTION_CHECK.equals(action)) {
                handleActionCheck();
            }
        }
    }
    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionStart() {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(GPS_PROVIDER, MIN_INTERVAL, MIN_DISTANCE, listener);
            locationManager.requestLocationUpdates(NETWORK_PROVIDER, MIN_INTERVAL, MIN_DISTANCE, listener);
        } catch (SecurityException secEx) {
            secEx.printStackTrace();
        }
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        CalculateDistance2Gate();
        if (distance2gate <= settings.GetActivationDistance()) {
            GateDialer dial = new GateDialer(settings, this);
            dial.dial();
            isCalled = true;
        } else {
            isCalled = false;
        }
        SetNextAlarm();
    }

    private void SetNextAlarm() {
        long alarmDelayMS = 60000;
        if (distance2gate < Float.MAX_VALUE) {
            alarmDelayMS = (long) (distance2gate / (100000.0 / (60.0 * 60.0))); // assume 100 kph
            if (alarmDelayMS < 60000)
                alarmDelayMS = 60000;
        }


        Intent intent = new Intent(context, AlarmOpenGateService.class);
        intent.setAction(Constants.ACTION_CHECK);
        intent.putExtra(IS_CALLED, isCalled);

        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmMgr.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + alarmDelayMS,
                    alarmIntent);
        } else  {
            alarmMgr.set(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + alarmDelayMS,
                    alarmIntent);
        }

    }

    void CalculateDistance2Gate() {
        distance2gate = Float.MAX_VALUE;
        try {
            Location gpsLocation = locationManager.getLastKnownLocation(GPS_PROVIDER);
            Location netLocation = locationManager.getLastKnownLocation(NETWORK_PROVIDER);
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, -1);

            Location gateLocation = new Location(GPS_PROVIDER);
            gateLocation.setLatitude(settings.GetLatitude());
            gateLocation.setLongitude(settings.GetLongitude());
            if (calendar.before(gpsLocation.getTime())) {
                distance2gate = gpsLocation.distanceTo(gateLocation);
            } else if (calendar.before(netLocation.getTime())) {
                distance2gate = netLocation.distanceTo(gateLocation);
            }
        } catch (SecurityException secEx) {
            secEx.printStackTrace();
        }

    }

    private void handleActionCheck() {
        if (distance2gate <= settings.GetActivationDistance()) {
            GateDialer dial = new GateDialer(settings, this);
            dial.dial();
            isCalled = true;
        } else {
            isCalled = false;
        }
        SetNextAlarm();
    }


    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionStop() {
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.cancel(alarmIntent);
    }
}
