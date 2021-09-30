package com.connectedliving.closer.push;

import android.content.Context;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.connectedliving.closer.robot.impl.ActionParser;

public class ConnectionHandler extends Worker {
    public ConnectionHandler(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public ListenableWorker.Result doWork() {
        if (Looper.myLooper() == null) Looper.prepare();
        String commandData = getInputData().getString("msg");
        if (commandData != null && !commandData.isEmpty()) {
            ActionParser.doAction(commandData);
        }
        try {
            ClConnection.getInstance().updateConnection();
        } catch (Exception e) {
            Result.failure();
        }
        return Result.success();
    }

}