package com.connectedliving.closer.robot;

public enum RobotAction {
    // Robot movement
    MOVE_FORWARD("move_forward"),
    MOVE_BACK("move_back"),
    MOVE_STOP("move_stop"),
    ROTATE_RIGHT("rotate_right"),
    ROTATE_LEFT("rotate_left"),

    GOTO_LOCATION("goto_location"),

    // Camera movement
    CAMERA_UP("camera_up"),
    CAMERA_DOWN("camera_down"),
    CAMERA_LEFT("camera_left"),
    CAMERA_RIGHT("camera_right"),

    // Camera
    CAMERA_PICTURE("camera_picture"),

    // Admin / Status
    STATUS("status"),

    ;

    private final String value;

    RobotAction(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Compare action to this enum value.  Return true if same
     *
     * @param action Action to compare
     * @return
     */
    public boolean handles(String action) {
        if (action == null) return false;
        return value.equals(action.toLowerCase());
    }
}
