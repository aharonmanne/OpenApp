package com.ksdagile.openapp;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * Created by user on 08/11/2016.
 */


public class OpenAppWidgetProvider extends AppWidgetProvider {
    private static final String ACTION_CLICK = "ACTION_CLICK";
    public static final String WIDGET_IDS_KEY = "OA_WIDGET_IDS";
    private static final String IsToggleName = "IS_TOGGLE";
    private static final long ONE_MINUTE = 60*1000;
    private boolean isToggle;
    Context context;
    private AlarmManager alarmMgr;


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AppWidgetProvider", "OnReceive");
        isToggle = false;
        this.context = context;
        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        if (intent.hasExtra(IsToggleName)) {
            isToggle = intent.getBooleanExtra(IsToggleName, false);
        }
        if (intent.hasExtra(WIDGET_IDS_KEY)) {
            int[] ids = intent.getExtras().getIntArray(WIDGET_IDS_KEY);
            this.onUpdate(context, AppWidgetManager.getInstance(context), ids);

        } else {
            super.onReceive(context, intent);
        }
    }

    // @Override
    // public void  onAppWidgetOptionsChanged (Context context,
    //                                             AppWidgetManager appWidgetManager,
    //                                             int appWidgetId,
    //                                             Bundle newOptions) {
    //     super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    // }
    // TODO: don't toggle state if this is the first call to onUpdate after configuration

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        Log.d("AppWidgetProvider", "onUpdate");
       // Get all ids
        ComponentName thisWidget =
                new ComponentName(context, OpenAppWidgetProvider.class);
        GateSettings settings = GateSettings.GetInstance(context);
        RemoteViews remoteViews =
                new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.gate_icon);

        if (!settings.GetIsSaved()) {
            Log.d(Constants.TAG, "No Config, starting Activity");
            Intent intent = new Intent(context, ConfigActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

        } else {
            if (isToggle) {
                boolean isRunning = settings.GetIsRunning();
                if (isRunning) {
                    StopAlarms();
                    bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.service_off);
                } else {
                    StartAlarms();
                    bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.service_on);
                }
                settings.SetIsRunning(!isRunning);
            }

        }
        remoteViews.setImageViewBitmap(R.id.toggle, bm);
        // set click event in all  cases
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        for (int widgetId : allWidgetIds) {

            Intent intent = new Intent(context, OpenAppWidgetProvider.class);

            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            intent.putExtra(IsToggleName, true);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.toggle, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }

    }

    private void StartAlarms() {
        Toast.makeText(context, context.getResources().getText(R.string.starting), Toast.LENGTH_LONG).show();
        Intent intent = new Intent(context, OpenAppService.class);
        intent.setAction(Constants.ACTION_START);
        context.sendBroadcast(intent);
    }

    private void StopAlarms() {
        Toast.makeText(context, context.getResources().getText(R.string.stopping), Toast.LENGTH_LONG).show();
        Intent intent = new Intent(context, OpenAppService.class);
        intent.setAction(Constants.ACTION_STOP);
        context.sendBroadcast(intent);
    }

    private boolean IsGPSon(Context context) {
        final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return (manager.isProviderEnabled(LocationManager.GPS_PROVIDER));
    }
}