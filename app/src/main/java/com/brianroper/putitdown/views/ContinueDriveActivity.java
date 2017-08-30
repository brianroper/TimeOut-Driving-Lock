package com.brianroper.putitdown.views;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

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
     */
    public void continueDrivingSession(){
        DrivingView drivingView = new DrivingView(getApplicationContext());
        drivingView.startDriving();
        finish();
    }
}
