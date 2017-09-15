package com.brianroper.putitdown.views;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.brianroper.putitdown.R;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntro2Fragment;
import com.github.paolorotolo.appintro.AppIntroFragment;

/**
 * Created by brianroper on 9/13/17.
 */

public class IntroActivity extends AppIntro2 {

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

        askForPermissions(new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW}, 4);

        addSlide(AppIntro2Fragment.newInstance("One Left",
                "TimeOut needs access to the do not disturb permission. " +
                        "This helps us keep your eyes on the road and not your phone",
                R.drawable.redcar,
                getResources().getColor(R.color.slide5Color)));


        askForPermissions(new String[]{Manifest.permission.MODIFY_AUDIO_SETTINGS}, 5);

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
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
    }
}
