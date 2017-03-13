package com.ksdagile.openapp;

import android.*;
import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.Policy;
import com.google.android.vending.licensing.ServerManagedPolicy;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, View.OnClickListener, Literal_Input.OnFragmentInteractionListener {

    private static final int PICK_GATE_LOCK = 1;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int MY_PERMISSIONS_REQUEST_MAKE_CALL = 2;
    private static final int MY_PERMISSIONS_READ_CONTACTS = 3;
    GateSettings settings;
    EditText phoneNumber;
    Context context;
    int appWidgetId;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    MapFragment mapFragment;

    private LicenseCheckerCallback mLicenseCheckerCallback;
    private LicenseChecker mChecker;

    Map<String, String> contactNameID;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Config Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    enum ACTIVITY_STATE {INIT_STATE, LITERAL_IN, LOCATION}

    ;
    static ACTIVITY_STATE state = ACTIVITY_STATE.INIT_STATE;
    // A handler on the UI thread.
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_config);
        context = this;
        contactNameID = new HashMap<String, String>();

        // Construct the LicenseCheckerCallback. The library calls this when done.
        mLicenseCheckerCallback = new MyLicenseCheckerCallback();

        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        // Construct the LicenseChecker with a Policy.
        mChecker = new LicenseChecker(
                this, new ServerManagedPolicy(this,
                new AESObfuscator(Constants.SALT, getPackageName(), deviceId)),
                Constants.BASE64_PUBLIC_KEY  // Your public licensing key.
        );

        mChecker.checkAccess(mLicenseCheckerCallback);

        settings = GateSettings.GetInstance(this);
        if (settings.GetIsSaved()) {
            Log.d(Constants.TAG, "Settings already configured");
        } else {
            Log.d(Constants.TAG, "Starting new configuration");
        }
        settings.SetIsNewWidget(true);

        mHandler = new Handler();

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
            // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
            // See https://g.co/AppIndexing/AndroidStudio for more information.
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(AppIndex.API).build();
            googleApiClient.connect();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mChecker.onDestroy();
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

        if (GateSettings.GetInstance(context).GetLatitude() > GateSettings.MAX_LAT ||
                GateSettings.GetInstance(context).GetLongitude() > GateSettings.MAX_LONG)
            saveError = context.getResources().getString(R.string.no_location);
        if (GateSettings.GetInstance(context).GetPhone() == null)
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
        EditText activationDistanceView = (EditText) findViewById(R.id.editTextDistance);
        RadioButton rbOn = (RadioButton) findViewById(R.id.radioButtonUseAlarm);
        RadioButton rbOff = (RadioButton) findViewById(R.id.radioButtonBorrowLoc);
        Integer activationDistance = null;
        try {
            activationDistance = Integer.decode(activationDistanceView.getText().toString());
        } catch (NumberFormatException nfEx) {
            nfEx.printStackTrace();
        }

        if (phoneNum == null || phoneNum.isEmpty()) {
            Toast.makeText(
                    context,
                    getResources().getText(R.string.phone_required).toString(),
                    Toast.LENGTH_LONG
            ).show();
        } else if (activationDistance == null) {
            Toast.makeText(
                    context,
                    getResources().getText(R.string.distance_hint).toString(),
                    Toast.LENGTH_LONG
            ).show();
        } else {
            String re = "[\\d]*"; // numbers only
            Pattern p = Pattern.compile(re);
            Matcher m = p.matcher(phoneNum);
            if (!m.matches()) {
                phoneNum = GetPhoneFromName(contactNameID.get(phoneNum));
            }
            state = ACTIVITY_STATE.LOCATION;
            SavePhoneChooseGate(phoneNum, activationDistance, rbOn.isChecked() && !rbOff.isChecked());
        }
    }

    private String GetPhoneFromName(String id) {
        String phoneNum = "";
        ContentResolver cr = getContentResolver();
        Uri contactData = ContactsContract.Data.CONTENT_URI;
        Cursor cursor = cr.query(contactData, null, null, null, null);

        try {
            cursor.moveToFirst();
            if (Integer.parseInt(cursor.getString(
                    cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                Cursor pCur =
                        cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone._ID + " = " + id, null, null);
                while (pCur.moveToNext()) {
                    //String number = pCur.getString(pCur.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    phoneNum = pCur.getString(pCur.getColumnIndex("data1"));
                    break; // ? we want only 1 value
                }
                pCur.close();
            }
        } catch (SQLiteException ex) {
            Log.d(Constants.TAG, ex.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return phoneNum;
    }

    private void SavePhoneChooseGate(String phoneNum, Integer activationDistance, boolean isUseAlarm) {
        try {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            MapViewFragment mapFragment = new MapViewFragment();
            fragmentTransaction.replace(R.id.fragment_place, mapFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            GateSettings.GetInstance(context).SetPhone(phoneNum);
            GateSettings.GetInstance(context).SetActivationDistance(activationDistance);
            GateSettings.GetInstance(context).SetUseAlarm(isUseAlarm);

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
        //Intent resultValue = new Intent();
        //resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        //setResult(RESULT_OK, resultValue);
        finish();
    }

    @Override
    protected void onStart() {
        if (GateSettings.GetInstance(context).GetLicenseStatus() != Constants.LICENSE_ALLOWED) {
            googleApiClient.connect();
        }
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.start(googleApiClient, getIndexApiAction());
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_CONTACTS)) {
            Log.d(Constants.TAG, "Need to show rationale?");
            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.

        } else {

            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    MY_PERMISSIONS_READ_CONTACTS);

            // MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION is an
            // app-defined int constant. The callback method gets the
            // result of the request.

            return;
        }

        // Add Gate Phone input fragment
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
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();// ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(googleApiClient, getIndexApiAction());
        state = ACTIVITY_STATE.INIT_STATE;
    }

    // fragment iterface


    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    // Google API interface
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.d(Constants.TAG, "Need to show rationale?");
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.

                return;
            }
        } else {

            lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    googleApiClient);
            boolean isKeptLocation =
                    GateSettings.GetInstance(context).GetLatitude() <= GateSettings.MAX_LAT &&
                            GateSettings.GetInstance(context).GetLongitude() <= GateSettings.MAX_LONG;
            if (lastLocation != null & !isKeptLocation) {
                GateSettings.GetInstance(context).SetLatitude(lastLocation.getLatitude());
                GateSettings.GetInstance(context).SetLongitude(lastLocation.getLongitude());
            }
            CheckPhonePermission();
        }

    }

    private void CheckPhonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CALL_PHONE)) {
                Log.d(Constants.TAG, "Need to show rationale?");
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CALL_PHONE},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.

                return;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    try {
                        lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                                googleApiClient);
                        boolean isKeptLocation =
                                GateSettings.GetInstance(context).GetLatitude() <= GateSettings.MAX_LAT &&
                                        GateSettings.GetInstance(context).GetLongitude() <= GateSettings.MAX_LONG;
                        if (lastLocation != null & !isKeptLocation) {
                            GateSettings.GetInstance(context).SetLatitude(lastLocation.getLatitude());
                            GateSettings.GetInstance(context).SetLongitude(lastLocation.getLongitude());
                        }
                    } catch (SecurityException secEx) {
                        Log.d(Constants.TAG, "Security Exception: " + secEx.getMessage());
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d(Constants.TAG, "Permission denied");
                }
                CheckPhonePermission();
                return;
            }
            case MY_PERMISSIONS_REQUEST_MAKE_CALL: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(Constants.TAG, "Phone Permission Granted");
                } else {
                    Log.d(Constants.TAG, "Phone Permission Denied");
                }

            }
            case MY_PERMISSIONS_READ_CONTACTS: {
                // Add Gate Phone input fragment
                FragmentManager fragmentManager = getFragmentManager();
                if (state == ACTIVITY_STATE.INIT_STATE) {
                    state = ACTIVITY_STATE.LITERAL_IN;
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    Literal_Input literal_input = new Literal_Input();
                    fragmentTransaction.add(R.id.fragment_place, literal_input);
                    fragmentTransaction.commit();
                }
            }
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
        double latitude = GateSettings.GetInstance(context).GetLatitude();
        double longitude = GateSettings.GetInstance(context).GetLongitude();
        String gateTitle = getResources().getString(R.string.title_activity_gate_location);
        if (latitude > GateSettings.GetInstance(context).MAX_LAT)
            latitude = 0;
        if (longitude > GateSettings.GetInstance(context).MAX_LONG)
            longitude = 0;
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .title(gateTitle));

    }

    // Licensing support
    private void displayResult(final String result) {
        mHandler.post(new Runnable() {
            public void run() {
                //mStatusText.setText(result);
                //setProgressBarIndeterminateVisibility(false);
                //mCheckLicenseButton.setEnabled(true);
            }
        });
    }

    private void displayDialog(final boolean showRetry) {
        mHandler.post(new Runnable() {
            public void run() {
                //setProgressBarIndeterminateVisibility(false);
                //showDialog(showRetry ? 1 : 0);
                //mCheckLicenseButton.setEnabled(true);
            }
        });
    }

private class MyLicenseCheckerCallback implements LicenseCheckerCallback {
    public void allow(int policyReason) {
        GateSettings.GetInstance(context).SetLicenseStatus(Constants.LICENSE_ALLOWED);
        if (isFinishing()) {
            // Don't update UI if Activity is finishing.
            return;
        }
        // Should allow user access.
        //displayResult(getString(R.string.allow));
    }

    public void dontAllow(int policyReason) {
        GateSettings.GetInstance(context).SetLicenseStatus(Constants.LICENSE_REJECTED);
        if (isFinishing()) {
            // Don't update UI if Activity is finishing.
            return;
        }
        //displayResult(getString(R.string.dont_allow));
        // Should not allow access. In most cases, the app should assume
        // the user has access unless it encounters this. If it does,
        // the app should inform the user of their unlicensed ways
        // and then either shut down the app or limit the user to a
        // restricted set of features.
        // In this example, we show a dialog that takes the user to Market.
        // If the reason for the lack of license is that the service is
        // unavailable or there is another problem, we display a
        // retry button on the dialog and a different message.
        displayDialog(policyReason == Policy.RETRY);
    }

    public void applicationError(int errorCode) {
        if (isFinishing()) {
            // Don't update UI if Activity is finishing.
            return;
        }
        // This is a polite way of saying the developer made a mistake
        // while setting up or calling the license checker library.
        // Please examine the error code and fix the error.
        //String result = String.format(getString(R.string.application_error), errorCode);
        //displayResult(result);
    }
}


}