package com.connectedliving.closer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.connectedliving.closer.push.ClConnection;
import com.connectedliving.closer.push.ConnectionMonitor;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "CL";
    private static final int REQUEST_CAMERA_PERMISSION = 200;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();
        LifecycleOwner owner = this;
        Configuration config = new Configuration(this);
        setupUI(config);
        // Start without keyboard shown
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Intent serviceIntent = new Intent(getApplicationContext(), CLService.class);
        ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
    }

    private void checkPermissions() {
        String packageName = getPackageName();
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + packageName));
            startActivity(intent);
        }
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
            return;
        }
    }

    /**
     * Update the display from configuration
     *
     * @param config
     */
    private void updateValues(Configuration config) {
        EditText facility = findViewById(R.id.FacilityName);
        EditText displayName = findViewById(R.id.RobotDisplayName);
        EditText robotId = findViewById(R.id.RobotNumber);
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
        EditText facility = findViewById(R.id.FacilityName);
        EditText displayName = findViewById(R.id.RobotDisplayName);
        EditText robotId = findViewById(R.id.RobotNumber);
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
        Button connectButton = findViewById(R.id.ConnectButton);
        LifecycleOwner owner = this;
        connectButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ClConnection.getInstance().terminate();
                ConnectionMonitor.getInstance().connect(getApplicationContext(), owner, true);
                return true;
            }
        });
        Button saveButton = findViewById(R.id.SaveButton);
        saveButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                updateConfig(config);
                return true;
            }
        });
        Button startService = findViewById(R.id.StartService);
        startService.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Intent serviceIntent = new Intent(getApplicationContext(), CLService.class);
                serviceIntent.putExtra("Testing", "test text");
                ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
                return true;
            }
        });
        Button stopService = findViewById(R.id.StopService);
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