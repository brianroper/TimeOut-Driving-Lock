package com.brianroper.putitdown.views;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Window;
import android.view.WindowManager;

import com.brianroper.putitdown.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends AppCompatActivity {

    @BindView(R.id.settings_toolbar)
    Toolbar mSettingsToolbar;

    @BindView(R.id.surface_tutorial)
    CardView mSurfaceTutorial;
    @BindView(R.id.surface_feedback)
    CardView mSurfaceFeedback;
    @BindView(R.id.surface_play_store)
    CardView mSurfacePlayStore;
    @BindView(R.id.surface_unlock_mode)
    CardView mSurfaceLockMode;
    @BindView(R.id.surface_drive_mode)
    CardView mSurfaceDriveMode;
    @BindView(R.id.surface_switch)
    CardView mSurfacePassenger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ButterKnife.bind(this);

        handleUIUtilities();
    }

    /**
     * UI UTILITIES
     */

    /**
     * handles all ui utilities for this activity
     */
    public void handleUIUtilities(){
        handleToolbarBehavior(mSettingsToolbar);
        handleStatusBarColor();
        handleCardViewBackgroundColors();
    }

    /**
     * handles toolbar behavior
     */
    public void handleToolbarBehavior(Toolbar toolbar){
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * sets the color of the status bar
     */
    public void handleStatusBarColor(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    /**
     * handles cardview background colors
     */
    public void handleCardViewBackgroundColors(){
        mSurfaceDriveMode.setCardBackgroundColor(getResources().getColor(R.color.white));
        mSurfaceFeedback.setCardBackgroundColor(getResources().getColor(R.color.white));
        mSurfaceLockMode.setCardBackgroundColor(getResources().getColor(R.color.white));
        mSurfacePassenger.setCardBackgroundColor(getResources().getColor(R.color.white));
        mSurfacePlayStore.setCardBackgroundColor(getResources().getColor(R.color.white));
        mSurfaceTutorial.setCardBackgroundColor(getResources().getColor(R.color.white));
    }
}
