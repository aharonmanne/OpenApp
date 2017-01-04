package com.ksdagile.openapp;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class ConfigActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
         GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, Literal_Input.OnFragmentInteractionListener {

    private static final int PICK_GATE_LOCK = 1;
    GateSettings settings;
    EditText phoneNumber;
    Context context;
    int appWidgetId;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    MapFragment mapFragment;
    //Tracker configTracker;
    //FirebaseAnalytics firebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_config);
        context = this;

        // Obtain the FirebaseAnalytics instance.
        //firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle params = new Bundle();
        params.putString("start_config", "StartConfig");
        params.putString("full_text", "Started Configuration Activity");
        //firebaseAnalytics.logEvent("start_config", params);
        // Just in case:
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);

        settings = GateSettings.GetInstance(this);
        if (settings.GetIsSaved()) {
            Log.d(Constants.TAG, "Settings already configured");
            LeaveConfig();
            return;
        } else {
            Log.d(Constants.TAG, "Starting new configuration");
        }

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Literal_Input literal_input = new Literal_Input();
        fragmentTransaction.add(R.id.fragment_place, literal_input);
        fragmentTransaction.commit();


        // Get the App Widget ID from the Intent that launched the Activity
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        Button button = (Button) findViewById(R.id.buttonSave);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                if (fragmentManager.getBackStackEntryCount() == 0) {
                    CheckSavePhone();

                } else {
                    SaveGateFinish();
                }

                Log.i(Constants.TAG, "Setting screen name: Config");
                //configTracker.setScreenName("Image~Config");
                //configTracker.send(new HitBuilders.ScreenViewBuilder().build());
            }

            private void SaveGateFinish() {
                String saveError = "OK";

                if (settings.GetLatitude() > GateSettings.MAX_LAT ||
                        settings.GetLongitude() > GateSettings.MAX_LONG)
                    saveError = context.getResources().getString(R.string.no_location);
                if (settings.GetPhone() == null)
                    saveError = context.getResources().getString(R.string.no_phone);
                if (saveError == "OK")
                    LeaveConfig();
                else {
                    Toast.makeText(context, saveError, Toast.LENGTH_LONG).show();
                }
            }

            private void CheckSavePhone() {
                EditText phoneNumber = (EditText) findViewById(R.id.editTextPhone);
                String phoneNum = phoneNumber.getText().toString();

                if (phoneNum == null || phoneNum.isEmpty()) {
                    Toast.makeText(
                            context,
                            getResources().getText(R.string.phone_required).toString(),
                            Toast.LENGTH_LONG
                    ).show();
                } else {
                    SavePhoneChooseGate(phoneNum);
                }
            }
        });

        // Create an instance of GoogleAPIClient.
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
       // if (configTracker == null) {
       //     GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
       //     // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
       //     configTracker = analytics.newTracker("UA-33685516-1");
//
       // }
    }

    private void SavePhoneChooseGate(String phoneNum) {
        try {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            MapViewFragment mapFragment = new MapViewFragment();
            fragmentTransaction.replace(R.id.fragment_place, mapFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            settings.SetPhone(phoneNum);

        } catch (Exception ex) {
            Log.d("ConfigActivity", ex.getMessage());
        }
    }


    private void LeaveConfig() {

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        AppWidgetManager man = AppWidgetManager.getInstance(context);
        int[] ids = man.getAppWidgetIds(
                new ComponentName(context, OpenAppWidgetProvider.class));
        Intent updateIntent = new Intent();
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(OpenAppWidgetProvider.WIDGET_IDS_KEY, ids);
        context.sendBroadcast(updateIntent);

        //Create the return Intent, set it with the Activity result, and finish the Activity:
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    // Fragment Interface

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
    // Google Map Interface
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        if (lastLocation != null) {
            settings.SetLatitude(lastLocation.getLatitude());
            settings.SetLongitude(lastLocation.getLongitude());
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        double latitude = settings.GetLatitude();
        double longitude = settings.GetLongitude();
        String gateTitle = getResources().getString(R.string.title_activity_gate_location);
        if (latitude > GateSettings.MAX_LAT)
            latitude = 0;
        if (longitude > GateSettings.MAX_LONG)
            longitude = 0;
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .title(gateTitle));

    }

}
