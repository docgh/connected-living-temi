package com.connectedliving.closer.push;

import android.net.Uri;
import android.util.Log;

import com.connectedliving.closer.Configuration;
import com.connectedliving.closer.robot.AbstractRobotService;
import com.connectedliving.closer.robot.impl.ActionParser;
import com.loopj.android.http.HttpGet;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.StatusLine;
import cz.msebera.android.httpclient.client.config.RequestConfig;
import cz.msebera.android.httpclient.client.methods.HttpPut;
import cz.msebera.android.httpclient.client.methods.HttpUriRequest;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;

public class ClConnection {

    static ClConnection instance;

    // TODO Configurable options
    private static final String ConfiguredHost = "local.docgreg.com";
    private static final String path = "/";
    private static final int PORT = 8080;
    private static java.net.URL url;
    private static java.net.URL queryUrl;

    CloseableHttpClient client;
    int TIMEOUT = 10 * 60 * 1000;

    public ClConnection() throws MalformedURLException {
        url = new URL("http://" + ConfiguredHost + ":" + PORT + path);
        queryUrl = new URL(url.toString() + "/query");
    }

    private void sendStatus(String text) {
        NetData.getInstance().sendMessage(NetData.MSG_TYPE.CONNECTION_STATUS, text);
    }

    /**
     * Return instance of this connection manager
     *
     * @return
     */
    public static ClConnection getInstance() {
        if (instance == null) {
            try {
                instance = new ClConnection();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    public void terminate() {
        try {
            if (client != null) client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        client = null;
    }

    public boolean connected() {
        return (client != null);
    }

    /**
     * Get URI Builder with domain added
     *
     * @return
     */
    public static Uri.Builder getBuilder() {
        return new Uri.Builder().scheme("http").authority(ConfiguredHost + ":" + PORT);
    }

    private String checkSetCookies(HttpResponse resp) {
        Header[] cookieHeaders = resp.getHeaders("Set-Cookie");
        if ((cookieHeaders != null) && (cookieHeaders.length > 0)) {
            StringBuilder cookieString = new StringBuilder();
            for (Header cookie : cookieHeaders) {
                cookieString.append(cookie.getValue());
                cookieString.append(";");
            }
            return cookieString.toString();
        }
        return null;
    }

    /**
     * Setup and perform Get request for next server command
     *
     * @param serverId Server ID to contact
     * @param cookies  Cookies for the request
     */
    private void getNextCommand(String serverId, String cookies) throws Exception {
        if (client == null) {
            Log.e("CL", "Null http client");
            return;
        }
        Configuration configuration = Configuration.getInstance();
        String uri = getBuilder()
                .appendPath("query")
                .appendQueryParameter("token", NetData.getInstance().getToken())
                .appendQueryParameter("facility", configuration.getFacility())
                .appendQueryParameter("robot", configuration.getDisplayName())
                .appendQueryParameter("robotId", Integer.toString(configuration.getRobotId()))
                .build().toString();
        HttpUriRequest req = new HttpGet(uri);
        if (cookies != null) req.addHeader("Cookie", cookies);
        try {
            HttpResponse resp = client.execute(req);
            StatusLine statusLine = resp.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                String newCookies = checkSetCookies(resp);
                if (newCookies != null) cookies = newCookies;  // Update cookies if needed
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                resp.getEntity().writeTo(out);
                String commandString = out.toString();
                out.close();
                if (commandString != null && commandString.contains("{")) {
                    try {
                        JSONObject respJSON = new JSONObject(commandString);

                        Log.d("net", commandString);
                        ActionParser.doAction(commandString);
                        getNextCommand(serverId, cookies);
                    } catch (JSONException e) {
                        Log.e("CL Setup", "Error sending command", e);
                    }
                }
            } else {
                Log.d("CL Setup", "Failed to get query");
                //Closes the connection.
                resp.getEntity().getContent().close();
            }
        } catch (IOException ex) {
            Log.d("CLConnection", ex.toString());
        }
        try {
            if (client != null) client.close();
        } catch (IOException e) {
            Log.d("CLConnection", e.toString());
        }
        client = null;
        sendStatus("Not Connected");
    }

    /**
     * Return the robot status, locations
     *
     * @return JSON string
     */
    private String getRobotData() {
        return AbstractRobotService.getInstance().getRobotData();
    }

    public void sendRobotStatus(String status) throws Exception {
        if (client == null) {
            return;
        }
        Configuration configuration = Configuration.getInstance();
        Log.d("CL ", "Sending robot status");
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(TIMEOUT)
                .setConnectionRequestTimeout(TIMEOUT)
                .setSocketTimeout(TIMEOUT).build();
        client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
        String uri = getBuilder()
                .appendPath("status")
                .appendQueryParameter("token", NetData.getInstance().getToken())
                .appendQueryParameter("facility", configuration.getFacility())
                .appendQueryParameter("robot", configuration.getDisplayName())
                .appendQueryParameter("robotId", Integer.toString(configuration.getRobotId()))
                .build().toString();
        HttpPut req = new HttpPut(uri);
        req.setEntity(new StringEntity(getRobotData()));
        req.setHeader("Content-type", "application/json");
        try {
            HttpResponse resp = client.execute(req);
            StatusLine statusLine = resp.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                Log.d("CLConnection", "status sent");
            }
        } catch (IOException ex) {
            if (client != null) {
                try {
                    client.close();
                    client = null;
                } catch (IOException e) {

                }
                Log.d("CLConnection", ex.toString());
            }
        }
    }

    /**
     * Update the connection.  Create new HTTP connection, register with server
     */
    public void updateConnection() throws Exception {
        Configuration configuration = Configuration.getInstance();
        Log.d("CL ", "Sending setup");
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(TIMEOUT)
                .setConnectionRequestTimeout(TIMEOUT)
                .setSocketTimeout(TIMEOUT).build();
        client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
        String uri = getBuilder()
                .appendPath("register")
                .appendQueryParameter("token", NetData.getInstance().getToken())
                .appendQueryParameter("facility", configuration.getFacility())
                .appendQueryParameter("robot", configuration.getDisplayName())
                .appendQueryParameter("robotId", Integer.toString(configuration.getRobotId()))
                .build().toString();
        HttpPut req = new HttpPut(uri);
        req.setEntity(new StringEntity(getRobotData()));
        req.setHeader("Content-type", "application/json");
        try {
            HttpResponse resp = client.execute(req);
            StatusLine statusLine = resp.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                resp.getEntity().writeTo(out);
                String responseString = out.toString();
                out.close();
                if (responseString != null && responseString.contains("{")) {
                    try {
                        JSONObject respJSON = new JSONObject(responseString);
                        // If we are provided with serverId, save for header
                        if (respJSON.has("serverId")) {
                            String serverId = respJSON.getString("serverId");
                            sendStatus("Connected");
                            getNextCommand(serverId, checkSetCookies(resp));
                            return;
                        }
                    } catch (JSONException e) {
                        Log.e("CL Setup", "Error setting cookie", e);
                    }
                }
                // Bad response
                Log.d("CL Connection", "No server info");
            } else {
                Log.d("CL Setup", "Failed to get connection with server");
                //Closes the connection.
                resp.getEntity().getContent().close();
            }
            client.close();

        } catch (IOException ex) {
            if (client != null) {
                try {
                    client.close();
                } catch (IOException e) {

                }
                Log.d("CLConnection", ex.toString());
            }
        }
        client = null;
        sendStatus("Not connected");

    }
}
