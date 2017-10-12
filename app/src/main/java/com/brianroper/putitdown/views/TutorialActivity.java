package com.brianroper.putitdown.views;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.brianroper.putitdown.R;
import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntro2Fragment;

public class TutorialActivity extends AppIntro2 {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Slide 1
        addSlide(AppIntro2Fragment.newInstance("Setting Goals",
                "Setting goals will reduce the amount of time you are " +
                        "influenced to unlock your phone while driving",
                R.drawable.redcar,getResources().getColor(R.color.colorPrimary)));

        //Slide 2
        addSlide(AppIntro2Fragment.newInstance("Setting Goals",
                "Setting goals will reduce the amount of time you are " +
                        "influenced to unlock your phone while driving",
                R.drawable.redcar,getResources().getColor(R.color.colorPrimary)));

        //Slide 3
        addSlide(AppIntro2Fragment.newInstance("Setting Goals",
                "Setting goals will reduce the amount of time you are " +
                        "influenced to unlock your phone while driving",
                R.drawable.redcar,getResources().getColor(R.color.colorPrimary)));

        //Slide 4
        addSlide(AppIntro2Fragment.newInstance("Setting Goals",
                "Setting goals will reduce the amount of time you are " +
                        "influenced to unlock your phone while driving",
                R.drawable.redcar,getResources().getColor(R.color.colorPrimary)));

        //Slide 5
        addSlide(AppIntro2Fragment.newInstance("Setting Goals",
                "Setting goals will reduce the amount of time you are " +
                        "influenced to unlock your phone while driving",
                R.drawable.redcar,getResources().getColor(R.color.colorPrimary)));


        //Allows user to skip tutorial
        showSkipButton(true);
        setProgressButtonEnabled(true);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        finish();
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        finish();
    }
}
