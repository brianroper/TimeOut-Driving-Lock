package com.brianroper.putitdown.views;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.brianroper.putitdown.model.driving.DrivingLockScreen;

public class ContinueDriveActivity extends AppCompatActivity {

    /**
     * this is just a placeholder activity that we manipulate to restart the driving lock
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        continueDrivingSession();
    }

    /**
     * continues the driving event
     * passes a boolean to startDriving to see if is night time
     */
    public void continueDrivingSession(){
        DrivingLockScreen drivingLockScreen = new DrivingLockScreen(getApplicationContext());
        drivingLockScreen.startDriving(drivingLockScreen.getIsNight());
        finish();
    }
}
