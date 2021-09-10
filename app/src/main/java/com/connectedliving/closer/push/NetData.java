package com.connectedliving.closer.push;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class NetData {

    private static NetData instance;
    private String token;
    private TextView tokenStatus;
    private Context applicationContext;
    private Handler handler;

    public enum MSG_TYPE {
        CONNECTION_STATUS(1),
        COMMAND_STATUS(2),
        NETWORK_RESET(10);

        private final int value;

        MSG_TYPE(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public static NetData getInstance() {
        if (instance == null) {
            instance = new NetData();
        }
        return instance;
    }

    public boolean sendMessage(MSG_TYPE type) {
        return sendMessage(type, null);
    }

    public boolean sendMessage(MSG_TYPE type, String value) {
        Message msg = new Message();
        JSONObject json = new JSONObject();
        try {
            json.put("Type", type.getValue());
            if (value != null) {
                json.put("Message", value);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        msg.obj = json;
        handler.sendMessage(msg);
        return true;
    }

    public void setContext(Context context) {
        this.applicationContext = context;
    }

    public Context getApplicationContext() {
        return this.applicationContext;
    }

    public void setTokenStatus(TextView view) {
        this.tokenStatus = view;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public Handler getHandler() {
        return handler;
    }

    public String getToken() {
        return this.token;
    }
}
