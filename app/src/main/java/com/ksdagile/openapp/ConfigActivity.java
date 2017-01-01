package com.ksdagile.openapp;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.analytics.FirebaseAnalytics;

public class ConfigActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, View.OnClickListener {

    private static final int PICK_GATE_LOCK = 1;
    GateSettings settings;
    EditText phoneNumber;
    Context context;
    int appWidgetId;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    MapFragment mapFragment;

    enum ACTIVITY_STATE {INIT_STATE, LITERAL_IN, LOCATION}

    ;
    static ACTIVITY_STATE state = ACTIVITY_STATE.INIT_STATE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_config);
        context = this;

        settings = GateSettings.GetInstance(this);
        if (settings.GetIsSaved()) {
            Log.d(Constants.TAG, "Settings already configured");
        } else {
            Log.d(Constants.TAG, "Starting new configuration");
        }

        // Get the App Widget ID from the Intent that launched the Activity
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // Just in case:
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_CANCELED, resultValue);
        Log.d(Constants.TAG, "Set RESULT_CANCELED");

        Button button = (Button) findViewById(R.id.buttonSave);
        button.setOnClickListener(this);

        Log.d(Constants.TAG, "Set button handler");

        // Create an instance of GoogleAPIClient.
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }


        FragmentManager fragmentManager = getFragmentManager();
        if (state == ACTIVITY_STATE.INIT_STATE) {
            state = ACTIVITY_STATE.LITERAL_IN;
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Literal_Input literal_input = new Literal_Input();
            fragmentTransaction.add(R.id.fragment_place, literal_input);
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (state == ACTIVITY_STATE.LOCATION) {
            state = ACTIVITY_STATE.LITERAL_IN;
        } else if (state == ACTIVITY_STATE.LITERAL_IN) {
            state = ACTIVITY_STATE.INIT_STATE;
        }
    }

    // button press handling
    public void onClick(View v) {
        FragmentManager fragmentManager = getFragmentManager();
        if (state == ACTIVITY_STATE.LITERAL_IN) {
            CheckSavePhone();
        } else {
            SaveGateFinish();
        }
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
            state = ACTIVITY_STATE.LOCATION;
            SavePhoneChooseGate(phoneNum);
        }
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
        boolean isKeptLocation =
                settings.GetLatitude() <= GateSettings.MAX_LAT &&
                        settings.GetLongitude() <= GateSettings.MAX_LONG;
        if (lastLocation != null & !isKeptLocation) {
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
