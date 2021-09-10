package com.connectedliving.closer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;

import com.connectedliving.closer.push.ClConnection;
import com.connectedliving.closer.push.ConnectionMonitor;
import com.connectedliving.closer.push.NetData;
import com.connectedliving.closer.robot.RobotService;
import com.connectedliving.closer.robot.impl.ActionParser;
import com.connectedliving.closer.robot.impl.TemiRobotService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "CL";
    private ConnectionMonitor monitor;

    private void setUpFirebase(Handler handler) {
        NetData netData = NetData.getInstance();
        netData.setContext(getApplicationContext());
        netData.setHandler(handler);
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
                        monitor.connect(getApplicationContext(), owner);
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        monitor = new ConnectionMonitor();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LifecycleOwner owner = this;
        RobotService service = new TemiRobotService(this);
        TextView name = (TextView) findViewById(R.id.RobotId);
        name.setText(service.getRobotId());
        ActionParser.setService(service);
        // Setup messages to main UI thread
        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                TextView tv = (TextView) findViewById(R.id.serverStatus);
                TextView command = (TextView) findViewById(R.id.CommandText);
                JSONObject msgJson = (JSONObject) msg.obj;
                try {
                    if (msgJson.getInt("Type") == 1) {
                        tv.setText(msgJson.getString("Message"));
                    }
                    if (msgJson.getInt("Type") == 2) {
                        command.setText(msgJson.getString("Message"));
                    }
                    if (msgJson.getInt("Type") == 10) {
                        monitor.connect(getApplicationContext(), owner);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        setUpFirebase(handler);
        Configuration config = new Configuration(this);
        setupUI(config);
        // Start without keyboard shown
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }

    /**
     * Update the display from configuration
     *
     * @param config
     */
    private void updateValues(Configuration config) {
        EditText facility = (EditText) findViewById(R.id.FacilityName);
        EditText displayName = (EditText) findViewById(R.id.RobotDisplayName);
        EditText robotId = (EditText) findViewById(R.id.RobotNumber);
        facility.setText(config.getFacility());
        displayName.setText(config.getDisplayName());
        robotId.setText(Integer.toString(config.getRobotId()));
    }

    /**
     * Save the configuration with latest values
     *
     * @param config
     */
    private void updateConfig(Configuration config) {
        Log.d("Updated Config", "updating");
        EditText facility = (EditText) findViewById(R.id.FacilityName);
        EditText displayName = (EditText) findViewById(R.id.RobotDisplayName);
        EditText robotId = (EditText) findViewById(R.id.RobotNumber);
        config.setDisplayName(displayName.getText().toString());
        config.setFacility(facility.getText().toString());
        config.setRobotId(Integer.parseInt(robotId.getText().toString()));
        config.save();
    }

    /**
     * Setup UI handlers
     */
    private void setupUI(Configuration config) {
        config.update();
        updateValues(config);
        Button connectButton = (Button) findViewById(R.id.ConnectButton);
        LifecycleOwner owner = this;
        connectButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ClConnection.getInstance().terminate();
                monitor.connect(getApplicationContext(), owner, true);
                return true;
            }
        });
        Button saveButton = (Button) findViewById(R.id.SaveButton);
        saveButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                updateConfig(config);
                ClConnection.getInstance().terminate();
                monitor.connect(getApplicationContext(), owner, true);
                return true;
            }
        });

    }
}