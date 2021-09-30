package com.connectedliving.closer.push;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Operation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.sql.Connection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

/**
 * Class to start a network connection and monitor.
 * If connection closes for a non-blocked reason, tries to reconnect after small delay
 */
public class ConnectionMonitor {

    private static ConnectionMonitor instance;
    private static final String WORK_TAG = "Connection";
    private final int DELAY = 30 * 1000;  // Delay between re-connect attempts

    public ConnectionMonitor() {
        this.instance = this;
    }

    public static ConnectionMonitor getInstance() {
        if (instance == null) {
            instance = new ConnectionMonitor();
        }
        return instance;
    }

    public void connect(Context context, LifecycleOwner activity) {
        connect(context, activity, false);
    }

    public void connect(Context context, LifecycleOwner activity, boolean force) {
        Data inputData = new Data.Builder()
                .putString("msg", "")
                .build();
        connect(context, activity, force, inputData);
    }

    public void connect(Context context, LifecycleOwner activity, boolean force, Data inputData) {
        OneTimeWorkRequest connectionRequest = new OneTimeWorkRequest.Builder(ConnectionHandler.class).setInputData(inputData).addTag(WORK_TAG).build();
        Operation future =
                WorkManager.getInstance(context).beginUniqueWork(WORK_TAG, ExistingWorkPolicy.REPLACE, connectionRequest).enqueue();
        future.getResult().addListener(new Runnable() {
            @Override
            public void run() {
                // Need?
            }
        }, new CurrentThreadExecutor());
        WorkManager.getInstance(context).getWorkInfoByIdLiveData(connectionRequest.getId()).observe(activity, workInfo -> {
                    if (workInfo == null || workInfo.getState() == null) {
                        return;
                    }
                    Log.d("Connection Monitor", workInfo.getState().toString());
                    // Wait 30 seconds and re-connect
                    if (workInfo.getState() == WorkInfo.State.SUCCEEDED || workInfo.getState() == WorkInfo.State.FAILED) {
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                // Make sure we haven't reconnected from another thread or interaction
                                Log.d("Connection Monitor", "reconnecting");
                                if (force || !ClConnection.getInstance().connected()) {
                                    connect(context, activity, false);
                                } else {
                                    Log.d("Connection Monitor", "Possible collision");
                                }
                            }
                        }, DELAY);
                    }

                }
        );

    }

    class CurrentThreadExecutor implements Executor {
        public void execute(@NonNull Runnable r) {
            r.run();
        }
    }
}
