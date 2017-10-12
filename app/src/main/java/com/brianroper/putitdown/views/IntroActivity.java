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

        addSlide(AppIntro2Fragment.newInstance(getString(R.string.welcome_title),
                getString(R.string.welcome_message),
                R.drawable.redcar,
                getResources().getColor(R.color.colorPrimary)));

        addSlide(AppIntro2Fragment.newInstance(getString(R.string.before_we_start_title),
                getString(R.string.before_we_start_message),
                R.drawable.redcar,
                getResources().getColor(R.color.colorAccent)));

        addSlide(AppIntro2Fragment.newInstance(getString(R.string.location_title),
                getString(R.string.location_message),
                R.drawable.redcar,
                getResources().getColor(R.color.slide3Color)));

        askForPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 3);


        addSlide(AppIntro2Fragment.newInstance(getString(R.string.app_overlay_title),
                getString(R.string.app_overlay_message),
                R.drawable.redcar,
                getResources().getColor(R.color.slide4Color)));

        addSlide(AppIntro2Fragment.newInstance(getString(R.string.one_left_title),
                getString(R.string.one_left_message),
                R.drawable.redcar,
                getResources().getColor(R.color.slide5Color)));

        addSlide(AppIntro2Fragment.newInstance(getString(R.string.start_driving_title),
                getString(R.string.start_driving_message),
                R.drawable.redcar,
                getResources().getColor(R.color.slide6Color)));

        addSlide(AppIntro2Fragment.newInstance(getString(R.string.one_last_thing_title),
                getString(R.string.one_last_thing_message),
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
