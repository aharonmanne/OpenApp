package com.ksdagile.openapp;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * Created by user on 08/11/2016.
 */

public class OpenAppWidgetProvider extends AppWidgetProvider {
    private static final String ACTION_CLICK = "ACTION_CLICK";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        // Get all ids
        ComponentName thisWidget = new ComponentName(context,
                OpenAppWidgetProvider.class);
        GateSettings settings = new GateSettings(context);


        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.widget_layout);
            Bitmap bm = null;
            boolean isRunning = settings.GetIsRunning();
            if (isRunning) {
                bm =  BitmapFactory.decodeResource(context.getResources(), R.drawable.on_toggle);
            } else {
                bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.off_toggle);
            }
            settings.SetIsRunning(!isRunning);
            remoteViews.setImageViewBitmap(R.id.toggle, bm);

            //Toast.makeText(context,"Tapped OpenApp", Toast.LENGTH_LONG).show();
            // Register an onClickListener
            Intent intent = new Intent(context, OpenAppWidgetProvider.class);

            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.toggle, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);

        }
    }
}