package com.connectedliving.closer.robot;

public abstract class AbstractRobotService implements RobotService {

    protected static RobotService instance;

    public static RobotService getInstance() {
        if (instance == null) {
            // throw error
        }
        return instance;
    }

}
