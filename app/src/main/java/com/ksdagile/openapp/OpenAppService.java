package com.ksdagile.openapp;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.widget.Toast;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class OpenAppService extends IntentService {
    private static final String ACTION_START = "com.ksdagile.openapp.action.START";
    private static final String ACTION_STOP = "com.ksdagile.openapp.action.STOP";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.ksdagile.openapp.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.ksdagile.openapp.extra.PARAM2";

    public OpenAppService() {
        super("OpenAppService");
    }

    /**
     * Starts this service to perform action START with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionStart(Context context) {
        Intent intent = new Intent(context, OpenAppService.class);
        intent.setAction(ACTION_START);
        context.startService(intent);
        Toast.makeText(context, "Starting OpenApp Service", Toast.LENGTH_LONG).show();
    }

    /**
     * Starts this service to perform action Stop with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionStop(Context context) {
        Intent intent = new Intent(context, OpenAppService.class);
        intent.setAction(ACTION_STOP);
        Toast.makeText(context, "Stopping OpenApp Service", Toast.LENGTH_LONG).show();
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START.equals(action)) {
                handleActionStart();
            } else if (ACTION_STOP.equals(action)) {
                handleActionStop();
            }
        }
    }

    /**
     * Set up location tracking and configure open gate trigger.
     */
    private void handleActionStart() {

    }

    /**
     * Stop location tracking.
     */
    private void handleActionStop() {
    }
}
