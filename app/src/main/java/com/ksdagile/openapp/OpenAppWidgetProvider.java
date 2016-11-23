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

    public static final String WIDGET_IDS_KEY ="OA_WIDGET_IDS";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AppWidgetProvider", "OnReceive");
        if (intent.hasExtra(WIDGET_IDS_KEY)) {
            int[] ids = intent.getExtras().getIntArray(WIDGET_IDS_KEY);
            this.onUpdate(context, AppWidgetManager.getInstance(context), ids);
        } else super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        Log.d("AppWidgetProvider", "onUpdate");
        // Get all ids
        ComponentName thisWidget =
                new ComponentName(context, OpenAppWidgetProvider.class);
        GateSettings settings = GateSettings.GetInstance(context);

        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        boolean isToggled = false;
        for (int widgetId : allWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.widget_layout);
            Bitmap bm = null;
            boolean isRunning = settings.GetIsRunning();
            if (isRunning) {
                bm =  BitmapFactory.decodeResource(context.getResources(), R.drawable.off_toggle);
            } else {
                bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.on_toggle);
            }
            if (!isToggled) {
                if (isRunning) {
                    OpenAppService.startActionStop(context);
                } else {
                    OpenAppService.startActionStart(context);
                }
                isToggled = true;
                isRunning = !isRunning;
                settings.SetIsRunning(isRunning);
            }
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