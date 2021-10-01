package com.connectedliving.closer.push;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.connectedliving.closer.CLService;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class CLFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "CL-FB";

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d("NEW_TOKEN", s);
        NetData data = NetData.getInstance();
        data.setToken(s);
    }

    public static String getToken(Context context) {
        return context.getSharedPreferences("_", Context.MODE_PRIVATE).getString("fb", "empty");
    }

    private void scheduleJob() {
        Log.d(TAG, "EXECUTING SCHEDULED JOB");
    }

    private void handleNow() {
        Log.d(TAG, "Handle now");
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        String payload = "";
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            payload = remoteMessage.getData().get("msg");
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
        /*
        Data inputData = new Data.Builder()
                .putString("msg", payload)
                .build();
        WorkRequest connectionRequest = new OneTimeWorkRequest.Builder(ConnectionHandler.class).setInputData(inputData).build();
        WorkManager.getInstance(getApplicationContext()).enqueue(connectionRequest);
        */
        // Service likely killed/asleep.  Restart and send command
        Intent serviceIntent = new Intent(getApplicationContext(), CLService.class);
        getApplicationContext().stopService(serviceIntent);
        serviceIntent.putExtra("Command", payload);
        ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);

    }
}
