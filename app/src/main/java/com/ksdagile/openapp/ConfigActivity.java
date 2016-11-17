package com.ksdagile.openapp;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import static android.R.attr.button;

public class ConfigActivity extends AppCompatActivity {

    GateSettings settings;
    EditText phoneNumber;
    Context context;
    int appWidgetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        context = this;

        // Get the App Widget ID from the Intent that launched the Activity
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        settings = new GateSettings(this);

        phoneNumber = (EditText) findViewById(R.id.editTextPhone);
        phoneNumber.setText(settings.GetPhone());

        Button button = (Button) findViewById(R.id.buttonLocateGate);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (phoneNumber.equals("")){
                    Toast.makeText(
                            context,
                            getResources().getText(R.string.phone_required).toString(),
                            Toast.LENGTH_LONG
                    ).show();
                } else {
                    SavePhoneChooseGate();
                }

            }
        });
    }

    private void SavePhoneChooseGate() {
        // TODO: Save phone to settings, run gate location activity

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        //Update the App Widget with a RemoteViews layout by calling updateAppWidget(int, RemoteViews):
        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.widget_layout);
        appWidgetManager.updateAppWidget(appWidgetId, views);

        //Create the return Intent, set it with the Activity result, and finish the Activity:
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);

        finish();
    }

}
