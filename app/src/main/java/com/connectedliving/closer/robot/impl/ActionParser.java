package com.connectedliving.closer.robot.impl;

import android.os.Message;
import android.util.Log;

import com.connectedliving.closer.push.NetData;
import com.connectedliving.closer.robot.RobotAction;
import com.connectedliving.closer.robot.RobotService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ActionParser {

    private static RobotService service;

    public static void setService(RobotService _service) {
        service = _service;
    }

    private static void sendCommand(String text) {
        try {
            // Ignore for now
            if (text != null) {
                return;
            }
            Message msg = new Message();
            JSONObject json = new JSONObject();
            json.put("Type", 2);
            json.put("Message", "Command received: " + text);
            msg.obj = json;
            NetData.getInstance().getHandler().sendMessage(msg);
            new Timer().schedule(new TimerTask() {

                @Override
                public void run() {
                    Message msg = new Message();
                    try {
                        json.put("Message", "");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    msg.obj = json;
                    NetData.getInstance().getHandler().sendMessage(msg);
                }
            }, 2000);
        } catch (JSONException e) {
            // Throws?
        }
    }

    public static void doAction(String action) {
        if (action == null) return;
        if (action.contains("{") && action.contains("}")) {
            try {
                JSONObject json = new JSONObject(action);
                String command = json.has("command") ? json.getString("command") : "";
                RobotAction robotAction = null;
                for (RobotAction actions : RobotAction.values()) {
                    if (actions.handles(command)) {
                        robotAction = actions;
                    }
                }
                if (robotAction == null) {
                    Log.d("Parser", "Unable to find command");
                    // TODO THROW EXCEPTION ?
                    return;
                }
                if (json.has("arguments")) {
                    JSONArray args = json.getJSONArray("arguments");
                    ArrayList arguments = new ArrayList();
                    for (int i = 0; i < args.length(); i++) {
                        arguments.add(args.get(i));
                    }
                    service.performAction(robotAction, arguments.toArray());
                } else {
                    service.performAction(robotAction, null);
                }

                Log.d("Parser", "Found command for " + robotAction.getValue());
                sendCommand(robotAction.getValue());
            } catch (JSONException e) {
                Log.e("Error parsing json", e.toString());
            }


        }
    }
}
