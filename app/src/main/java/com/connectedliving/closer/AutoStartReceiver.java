package com.connectedliving.closer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class AutoStartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("RECEIVER", "STARTING!!!!");
        Intent serviceIntent = new Intent(context, CLService.class);
        ContextCompat.startForegroundService(context, serviceIntent);
    }
}
