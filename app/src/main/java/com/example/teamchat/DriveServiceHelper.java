package com.example.teamchat;

import com.google.api.services.drive.Drive;

public class DriveServiceHelper {
    private static DriveServiceHelper instance;
    private Drive mDriveService;

    private DriveServiceHelper() {
    }

    public static DriveServiceHelper getInstance() {
        if (instance == null) {
            instance = new DriveServiceHelper();
        }
        return instance;
    }

    public void setDriveService(Drive driveService) {
        mDriveService = driveService;
    }

    public Drive getDriveService() {
        return mDriveService;
    }
}
