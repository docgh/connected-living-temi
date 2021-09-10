package com.connectedliving.closer.robot.impl;

import android.util.Log;

import com.connectedliving.closer.robot.RobotAction;
import com.connectedliving.closer.robot.RobotService;
import com.google.gson.JsonObject;

public class TestRobotService implements RobotService {
    @Override
    public String getRobotName() {
        return "Test Robot";
    }

    @Override
    public String getRobotId() {
        return "1234";
    }

    @Override
    public void performAction(RobotAction action, Object... args) {

        Log.d("Action", "Perform " + action.getValue());
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof String) {
                    Log.d("arguments", (String) args[i]);
                }
                if (args[i] instanceof Integer) {
                    Log.d("arguments", Integer.toString((Integer) args[i]));
                }
            }
        }

    }

    @Override
    public String getRobotData() {
        return new JsonObject().toString();
    }
}
