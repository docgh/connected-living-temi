package com.connectedliving.closer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleService;

import com.connectedliving.closer.push.ConnectionMonitor;
import com.connectedliving.closer.push.NetData;
import com.connectedliving.closer.robot.RobotService;
import com.connectedliving.closer.robot.impl.ActionParser;
import com.connectedliving.closer.robot.impl.TemiRobotService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.listeners.OnRobotReadyListener;

public class CLService extends LifecycleService implements OnRobotReadyListener {

    private static final String TAG = "CLService";
    private String fbTag = "";
    private String startupCommand;

    private void setUpFirebase() {
        NetData netData = NetData.getInstance();
        netData.setContext(getApplicationContext());
        LifecycleOwner owner = this;
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String msg = task.getResult();

                        Log.d(TAG, msg);
                        netData.setToken(msg);
                        if (!fbTag.equals(msg)) {
                            fbTag = msg;
                            ConnectionMonitor.getInstance().connect(getApplicationContext(), owner);
                        }
                    }
                });
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Robot.getInstance().addOnRobotReadyListener(this);
        startupCommand = intent.getStringExtra("Command");
        return START_NOT_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return null;
    }

    @Override
    public void onRobotReady(boolean b) {
        Log.d(TAG, "Robot Ready, so starting");
        Configuration config = new Configuration(getApplicationContext());
        RobotService service = new TemiRobotService(this);
        ActionParser.setService(service);
        if (startupCommand != null && !startupCommand.isEmpty()) {
            ActionParser.doAction(startupCommand);
            startupCommand = null;
        }
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CLApp.CHANNEL_ID)
                .setContentTitle("Temi Service")
                .setContentText("Started")
                .setSmallIcon(R.mipmap.cl_r_launcher_round)
                .setContentIntent(pendingIntent)
                .build();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        startForeground(1, notification);
        Log.d(TAG, "Started");
        setUpFirebase();
    }
}
