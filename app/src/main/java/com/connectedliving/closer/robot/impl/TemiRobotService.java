package com.connectedliving.closer.robot.impl;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.connectedliving.closer.push.ClConnection;
import com.connectedliving.closer.robot.AbstractRobotService;
import com.connectedliving.closer.robot.RobotAction;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.robotemi.sdk.BatteryData;
import com.robotemi.sdk.Robot;

import java.util.Date;
import java.util.List;

public class TemiRobotService extends AbstractRobotService {

    Context context;
    TemiCamera cam;
    MovementThread move = null;

    public TemiRobotService(Context context) {
        instance = this;
        this.context = context;
        cam = new TemiCamera(context);
    }

    @Override
    public String getRobotName() {
        return "Test Robot";
    }

    @Override
    public String getRobotId() {
        return Robot.getInstance().getSerialNumber();
    }

    private boolean hasIntArg(Object... args) {
        return (args != null && args.length > 0 && args[0] instanceof Integer);
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
        switch (action) {
            case CAMERA_UP:
                if (hasIntArg(args)) {
                    Robot.getInstance().tiltBy((Integer) args[0]);
                }
                break;
            case CAMERA_DOWN:
                if (hasIntArg(args)) {
                    Robot.getInstance().tiltBy((Integer) args[0] * -1);
                }
                break;
            case CAMERA_RIGHT:
                if (hasIntArg(args)) {
                    Robot.getInstance().turnBy((Integer) args[0] * -1, 1);
                }
                break;
            case CAMERA_LEFT:
                if (hasIntArg(args)) {
                    Robot.getInstance().turnBy((Integer) args[0], 1);
                }
                break;
            case CAMERA_PICTURE:
                cam.getPicture();
                break;
            case MOVE_FORWARD:
                stopMovement();
                move(1, 0);
                break;
            case MOVE_BACK:
                stopMovement();
                move(-1, 0);
                break;
            case ROTATE_LEFT:
                stopMovement();
                move(0, 1);
                break;
            case ROTATE_RIGHT:
                stopMovement();
                move(0, -1);
                break;
            case MOVE_STOP:
                stopMovement();
                break;
            case GOTO_LOCATION:
                if (args != null && args.length > 0 && args[0] instanceof String) {
                    Robot.getInstance().goTo((String) args[0]);
                }
                break;
            case STATUS:
                try {
                    ClConnection.getInstance().sendRobotStatus(getRobotData());
                } catch (Exception e) {
                    Log.d("Temi", e.toString());
                }
                break;
            default:
                // Send not found?
                break;
        }
    }

    @Override
    public String getRobotData() {
        JsonObject json = new JsonObject();
        List<String> locations = Robot.getInstance().getLocations();
        JsonArray locArray = new JsonArray();
        for (String loc : locations) {
            locArray.add(loc);
        }
        json.add("Locations", locArray);
        JsonObject battery = new JsonObject();
        BatteryData bat = Robot.getInstance().getBatteryData();
        battery.addProperty("level", bat.getBatteryPercentage());
        battery.addProperty("charging", bat.isCharging());
        json.add("Battery", battery);
        return json.toString();
    }

    /**
     * Stop movement of TEMI, kill movement thread
     */
    private void stopMovement() {
        if (move != null) {
            move.interrupt();
            move = null;
        }
        Robot.getInstance().stopMovement();
    }

    /**
     * Start movement thread, movement in x or y direction
     *
     * @param direction
     */
    private void move(float direction, float rotation) {
        if (move == null) {
            move = new MovementThread(direction, rotation);
            move.start();
        }
    }

    class MovementThread extends Thread {

        private static final long maxDuration = 5000;

        float direction;
        float rotation;

        public MovementThread(float direction, float rotation) {
            this.direction = direction;
            this.rotation = rotation;
        }

        public void run() {
            Long present = new Date().getTime();

            while ((new Date().getTime() - present) < maxDuration) {
                Robot.getInstance().skidJoy(direction, rotation);
                Log.d("movement", Float.toString(direction));
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Log.d("movement", "STOP");
                    present = 0L;
                }
            }
        }
    }


}
