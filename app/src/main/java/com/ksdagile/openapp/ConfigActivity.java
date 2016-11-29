package com.ksdagile.openapp;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
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

    private static final int PICK_GATE_LOCK = 1;
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

        settings = GateSettings.GetInstance(this);
        if (settings.GetIsSaved()) {
            LeaveConfig();
            return;
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
                EditText phoneNumber = (EditText) findViewById(R.id.editTextPhone);
                String phoneNum = phoneNumber.getText().toString();
                // TODO: Check if showing phone or location fragment
                FragmentManager fragmentManager = getFragmentManager();
                if (fragmentManager.getBackStackEntryCount() == 0) {
                    CheckSavePhone(phoneNum);

                } else {

                }

            }

            private void CheckSavePhone(String phoneNum) {
                phoneNumber = (EditText) findViewById(R.id.editTextPhone);
                phoneNumber.setText(settings.GetPhone());
                if (phoneNum == null || phoneNum.isEmpty()) {
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
        Intent pickGateLoc = new Intent(this, GateLocationActivity.class);
        startActivityForResult(pickGateLoc, PICK_GATE_LOCK);
        // TODO: Save phone to settings, run gate location activity
        LeaveConfig();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == PICK_GATE_LOCK) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                LeaveConfig();
            }
        }
    }

    private void LeaveConfig() {

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        //Create the return Intent, set it with the Activity result, and finish the Activity:
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);

        AppWidgetManager man = AppWidgetManager.getInstance(context);
        int[] ids = man.getAppWidgetIds(
                new ComponentName(context, OpenAppWidgetProvider.class));
        Intent updateIntent = new Intent();
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(OpenAppWidgetProvider.WIDGET_IDS_KEY, ids);
        context.sendBroadcast(updateIntent);
        finish();
    }

}
