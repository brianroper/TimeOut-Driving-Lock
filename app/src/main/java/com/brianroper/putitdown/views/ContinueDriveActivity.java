package com.brianroper.putitdown.views;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.brianroper.putitdown.R;

public class ContinueDriveActivity extends AppCompatActivity {

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
