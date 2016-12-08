package com.ksdagile.openapp;

import android.app.Activity;
import android.content.Context;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by user on 13/09/2016.
 */

public class GateSettings {

    private static GateSettings Instance = null;

    public static final String PREFS_NAME = "OpenAppPrefs";
    public static final Double MAX_LAT = 90.;
    public static final Double MAX_LONG = 180.;

    private String SettingsJSON;
    private Activity parentActivity;
    private String Phone;
    private double Latitude;
    private double Longitude;
    private boolean IsRunning;
    private boolean IsSaved;
    private static String SettingsFile = "Settings.json";
    private static String PHONE_NAME = "phone";
    private static String LAT_NAME = "latitude";
    private static String LONG_NAME = "longitude";
    private static String IS_RUNNING = "is_running";
    private static String IS_SAVED = "is_saved";
    private Context context;
    private InputStream InputStream;
    private OutputStream OutputStream;

    public static synchronized GateSettings GetInstance(Context _context) {
        if (Instance == null) {
            Instance = new GateSettings(_context);
        }
        return Instance;
    }

    private GateSettings(Context _context) {
        String settingsJSON;
        context = _context;
        boolean isReadFail = false;
        try {
            InputStream = context.openFileInput(SettingsFile);
            JsonReader reader = new JsonReader(new InputStreamReader(InputStream, "UTF-8"));

            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals(PHONE_NAME)) {
                    Phone = reader.nextString();
                } else if (name.equals(LAT_NAME)) {
                    Latitude = reader.nextDouble();
                } else if (name.equals((LONG_NAME))) {
                    Longitude = reader.nextDouble();
                } else if (name.equals(IS_RUNNING)) {
                    IsRunning = reader.nextBoolean();
                } else if (name.equals(IS_SAVED)) {
                    IsSaved = reader.nextBoolean();
                }
            }
            reader.endObject();
            InputStream.close();
            OutputStream = context.openFileOutput(SettingsFile, Context.MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
            isReadFail = true;
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
            isReadFail = true;
        }
        if (isReadFail) {
            Init();
        }
    }

    private void Init() {
        Phone = "";
        Latitude = MAX_LAT + 1;
        Longitude = MAX_LONG + 1;
        IsRunning = false;
        IsSaved = false;
    }

    private void Save() {
        try {
            IsSaved = true;
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(OutputStream, "UTF-8"));
            writer.setIndent("  ");
            writer.beginObject();
            writer.name(PHONE_NAME).value(Phone);
            writer.name(LAT_NAME).value(Latitude);
            writer.name(LONG_NAME).value(Longitude);
            writer.name(IS_RUNNING).value(IsRunning);
            writer.name(IS_SAVED).value(IsSaved);
            writer.endObject();
            writer.close();


        } catch (Exception ex) {
            Log.d("Settings", ex.getLocalizedMessage());
        }

    }

    private void LoadValues(String settingsJSON) {
    }

    public String GetPhone() {
        return Phone;
    }

    public void SetPhone(String _phone) {
        Phone = _phone;
        Save();
    }

    public double GetLongitude() {
        return Longitude;
    }

    public void SetLongitude(double _longitude) {
        Longitude = _longitude;
        Save();
    }

    public double GetLatitude() {
        return Latitude;
    }

    public void SetLatitude(double _latitude) {
        Latitude = _latitude;
        Save();
    }

    public boolean GetIsRunning() {
        return IsRunning;
    }

    public void SetIsRunning(boolean _isRunning) {
        IsRunning = _isRunning;
        Save();
    }

    public boolean GetIsSaved() {
        return IsSaved;
    }
}
