package com.brianroper.putitdown.views;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.brianroper.putitdown.R;
import com.brianroper.putitdown.model.events.PermissionsMessage;
import com.brianroper.putitdown.utils.Constants;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntro2Fragment;
import com.github.paolorotolo.appintro.AppIntroFragment;

import org.greenrobot.eventbus.EventBus;

import static com.brianroper.putitdown.views.DashboardActivity.MY_PERMISSIONS_REQUEST_OVERLAY;

/**
 * Created by brianroper on 9/13/17.
 */

public class IntroActivity extends AppIntro2 {

    private int mCurrentSlidePosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(AppIntro2Fragment.newInstance("Welcome",
                "TimeOut will help you put an end to texting and driving",
                R.drawable.redcar,
                getResources().getColor(R.color.colorPrimary)));

        addSlide(AppIntro2Fragment.newInstance("Before we start",
                "TimeOut needs permission to " +
                        "access some of your device sensors. " +
                        "Without them the app won't function properly",
                R.drawable.redcar,
                getResources().getColor(R.color.colorAccent)));

        addSlide(AppIntro2Fragment.newInstance("Location",
                "TimeOut uses location to determine if your vehicle is moving.",
                R.drawable.redcar,
                getResources().getColor(R.color.slide3Color)));

        askForPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 3);


        addSlide(AppIntro2Fragment.newInstance("App Overlay",
                "TimeOut needs permission to draw over other apps. " +
                        "This is a key part of our lockout technology",
                R.drawable.redcar,
                getResources().getColor(R.color.slide4Color)));

        addSlide(AppIntro2Fragment.newInstance("One Left",
                "TimeOut needs access to the do not disturb permission. " +
                        "This helps us keep your eyes on the road and not your phone",
                R.drawable.redcar,
                getResources().getColor(R.color.slide5Color)));

        addSlide(AppIntro2Fragment.newInstance("Start Driving",
                "Time works silently in the background to ensure your always driving safetly. " +
                        "To get start, just start driving!",
                R.drawable.redcar,
                getResources().getColor(R.color.slide6Color)));

        addSlide(AppIntro2Fragment.newInstance("One Last Thing",
                "To stay up to date on your safe driving progress. Click on the trip log to view a detailed breakdown.",
                R.drawable.redcar,
                getResources().getColor(R.color.colorPrimaryDark)));

        showSkipButton(false);
        setProgressButtonEnabled(true);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        finish();
        super.onDonePressed(currentFragment);
        Constants constants = new Constants();
        EventBus.getDefault().postSticky(new PermissionsMessage(constants.PERMISSION_EVENT_GRANTED));
    }

    @Override
    protected void onPageSelected(int position) {
        super.onPageSelected(position);
        mCurrentSlidePosition = position;
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        if(mCurrentSlidePosition == 4){
            checkDrawOverlayPermission();
        }
        if(mCurrentSlidePosition == 5){
            checkDoNotDisturbPermission();
        }
    }

    /**
     * checks for overlay permission
     */
    public void checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, MY_PERMISSIONS_REQUEST_OVERLAY);
            }
        }
    }

    /**
     * asks for do not disturb permission
     */
    public void checkDoNotDisturbPermission(){
        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(
                    android.provider.Settings
                            .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }
    }
}
