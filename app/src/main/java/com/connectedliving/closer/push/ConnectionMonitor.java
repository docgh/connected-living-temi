package com.connectedliving.closer.push;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Operation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

/**
 * Class to start a network connection and monitor.
 * If connection closes for a non-blocked reason, tries to reconnect after small delay
 */
public class ConnectionMonitor {

    private static final String WORK_TAG = "Connection";
    private final int DELAY = 30 * 1000;  // Delay between re-connect attempts

    public void connect(Context context, LifecycleOwner activity) {
        connect(context, activity, false);
    }

    public void connect(Context context, LifecycleOwner activity, boolean force) {
        OneTimeWorkRequest connectionRequest = new OneTimeWorkRequest.Builder(ConnectionHandler.class).addTag(WORK_TAG).build();
        Operation future =
                WorkManager.getInstance(context).beginUniqueWork(WORK_TAG, ExistingWorkPolicy.KEEP, connectionRequest).enqueue();
        future.getResult().addListener(new Runnable() {
            @Override
            public void run() {
                finished();
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
                                    NetData.getInstance().sendMessage(NetData.MSG_TYPE.NETWORK_RESET);
                                } else {
                                    Log.d("Connection Monitor", "Possible collision");
                                }
                            }
                        }, DELAY);
                    }

                }
        );

    }

    private void finished() {
        Log.d("Monitor", "Finished");

    }

    class CurrentThreadExecutor implements Executor {
        public void execute(@NonNull Runnable r) {
            r.run();
        }
    }
}
