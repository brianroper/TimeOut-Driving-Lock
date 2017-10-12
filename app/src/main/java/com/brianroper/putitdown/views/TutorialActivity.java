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

        //Slide 1 -- Setting Goals
        addSlide(AppIntro2Fragment.newInstance(getString(R.string.setting_goals_title),
                getString(R.string.setting_goals_message),
                R.drawable.redcar,getResources().getColor(R.color.colorPrimary)));

        //Slide 2 -- Explaining dashboard (top portion)
        addSlide(AppIntro2Fragment.newInstance(getString(R.string.explain_dashboard_title),
                getString(R.string.explain_dashboard_message),
                R.drawable.redcar,getResources().getColor(R.color.colorPrimary)));

        //Slide 3 -- passenger mode
        addSlide(AppIntro2Fragment.newInstance(getString(R.string.passenger_mode_title),
                getString(R.string.passenger_mode_message),
                R.drawable.redcar,getResources().getColor(R.color.colorPrimary)));

        //Slide 4 -- drive difficulty setting
        addSlide(AppIntro2Fragment.newInstance(getString(R.string.drive_difficulty_title),
                getString(R.string.drive_difficulty_message),
                R.drawable.redcar,getResources().getColor(R.color.colorPrimary)));

        //Slide 5 -- unlock reset setting 
        addSlide(AppIntro2Fragment.newInstance(getString(R.string.unlock_reset_title),
                getString(R.string.unlock_reset_message),
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
