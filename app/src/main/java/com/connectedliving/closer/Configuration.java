package com.connectedliving.closer;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class Configuration {

    Activity activity;
    private static Configuration instance;
    private String facility;
    private int robotId;
    private String displayName;

    public Configuration(Activity activity) {
        this.activity = activity;
        instance = this;
    }

    public static Configuration getInstance() throws Exception {
        if (instance == null) {
            throw new Exception("Configuration not yet initialized");
        }
        return instance;
    }

    /**
     * Update from storage
     */
    public void update() {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        this.robotId = sharedPref.getInt("RobotId", 0);
        this.displayName = sharedPref.getString("DisplayName", "");
        this.facility = sharedPref.getString("Facility", "");
    }

    /**
     * Perform save
     */
    public void save() {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("Facility", this.facility);
        editor.putString("DisplayName", this.displayName);
        editor.putInt("RobotId", this.robotId);
        editor.commit();
    }

    // Common getter/setters

    public void setFacility(String facility) {
        this.facility = facility;
    }

    public String getFacility() {
        return facility;
    }

    public void setDisplayName(String name) {
        this.displayName = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setRobotId(int id) {
        this.robotId = id;
    }

    public int getRobotId() {
        return robotId;
    }


}
