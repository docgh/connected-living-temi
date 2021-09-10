package com.connectedliving.closer.robot;

public interface RobotService {

    /**
     * Get the name of the robot
     *
     * @return String of the robot name
     */
    String getRobotName();

    /**
     * Get a unique robot Id
     *
     * @return unique RobotId
     */
    String getRobotId();

    /**
     * Perform a robot action
     *
     * @param action The action
     * @param args   Arguments if any
     */
    void performAction(RobotAction action, Object... args);

    /**
     * Gets data about the robot to be sent during registration
     *
     * @return JSON String containing data
     */
    String getRobotData();


}
