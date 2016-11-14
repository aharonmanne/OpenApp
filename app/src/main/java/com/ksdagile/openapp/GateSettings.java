package com.ksdagile.openapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import com.ksdagile.openapp.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Set;

/**
 * Created by user on 13/09/2016.
 */

public class GateSettings {

    public static final String PREFS_NAME = "OpenAppPrefs";

    private String SettingsJSON;
    private Activity parentActivity;
    private String Phone;
    private double Latitude;
    private double Longitude;
    private boolean IsRunning;
    private static String SettingsFile = "Settings.json";
    private static String PHONE_NAME = "phone";
    private static String LAT_NAME = "latitude";
    private static String LONG_NAME = "longitude";
    private static String IS_RUNNING = "is_running";
    private Context context;

    public GateSettings(Context _context) {
        String settingsJSON;
        context  = _context;
        boolean isReadFail = false;
        try {
            InputStream inputStream = context.openFileInput(SettingsFile);
            JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));

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
                }
            }
            reader.endObject();
            inputStream.close();
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
            isReadFail = true;
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
            isReadFail = true;
        }
        if (isReadFail) {
            Init();
            Save();
        }
    }

    private void Init() {
        Phone = "";
        Latitude = 0;
        Longitude = 0;
        IsRunning = false;
    }

    private void Save() {
        try {
            OutputStream outputStream = context.openFileOutput(SettingsFile, Context.MODE_PRIVATE);
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.setIndent("  ");
            writer.beginObject();
            writer.name(PHONE_NAME).value(Phone);
            writer.name(LAT_NAME).value(Latitude);
            writer.name(LONG_NAME).value(Longitude);
            writer.name(IS_RUNNING).value(IsRunning);
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

    public Double GetLongitude() {
        return Longitude;
    }

    public void SetLongitude(Float _longitude) {
        Longitude = _longitude;
        Save();
    }

    public Double GetLatitude() {
        return Latitude;
    }

    public void SetLatitude(Float _latitude) {
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

}
