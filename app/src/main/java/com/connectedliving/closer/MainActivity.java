package com.connectedliving.closer;

import android.content.Intent;
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
import androidx.core.content.ContextCompat;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LifecycleOwner owner = this;
        Configuration config = new Configuration(this);
        setupUI(config);
        // Start without keyboard shown
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Intent serviceIntent = new Intent(getApplicationContext(), CLService.class);
        ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
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
                ConnectionMonitor.getInstance().connect(getApplicationContext(), owner, true);
                return true;
            }
        });
        Button saveButton = (Button) findViewById(R.id.SaveButton);
        saveButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                updateConfig(config);
                return true;
            }
        });
        Button startService = (Button) findViewById(R.id.StartService);
        startService.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Intent serviceIntent = new Intent(getApplicationContext(), CLService.class);
                serviceIntent.putExtra("Testing", "test text");
                ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
                return true;
            }
        });
        Button stopService = (Button) findViewById(R.id.StopService);
        stopService.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Intent serviceIntent = new Intent(getApplicationContext(), CLService.class);
                stopService(serviceIntent);
                return true;
            }
        });

    }
}