package com.brianroper.putitdown.views;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DialogTitle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.brianroper.putitdown.R;
import com.brianroper.putitdown.model.events.PreferenceMessage;

import org.greenrobot.eventbus.EventBus;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

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

    @BindView(R.id.passenger_switch)
    SwitchCompat mPassengerSwitch;

    @BindView(R.id.unlock_time_textView)
    TextView mLockOutTimeTextView;
    @BindView(R.id.drive_option_textView)
    TextView mDriveModeTextView;
    @BindView(R.id.settings_version_code)
    TextView mVersionCode;

    private SharedPreferences mSharedPreferences;
    private final String[] mDriveModes = {"Strict", "Normal", "Lenient"};
    private final String[] mTimes = {"15s", "30s", "45s"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ButterKnife.bind(this);

        returnDefaultSharedPreferences();
        handleUIUtilities();
    }

    /**
     * LIFE CYCLE METHODS
     */
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * END OF LIFE CYCLE METHODS
     */

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
        setPassengerSwitchDefaultPosition();
        setLockOutTimeOption();
        setDrivingModeOption();
        setVersionCode();
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

    /**
     * sets the default position of the passenger switch
     */
    public void setPassengerSwitchDefaultPosition(){
        boolean passengerMode = mSharedPreferences.getBoolean(getString(R.string.passenger_mode_key), false);

        if(passengerMode){
            mPassengerSwitch.setChecked(true);
        }
        else if(!passengerMode){
            mPassengerSwitch.setChecked(false);
        }
    }

    /**
     * sets the default value for the driving option
     */
    public void setDrivingModeOption(){
        int option = mSharedPreferences.getInt("driveModeOption", 1); // default option is normal

        if (option == 0) {
            mDriveModeTextView.setText(mDriveModes[0]);
        } else if (option == 1) {
            mDriveModeTextView.setText(mDriveModes[1]);
        } else if (option == 2) {
            mDriveModeTextView.setText(mDriveModes[2]);
        }
    }

    /**
     * sets the default time for locking out of device
     */
    public void setLockOutTimeOption(){
        int option = mSharedPreferences.getInt("lockOutTime", 1); // default option is 30,000ms

        if (option == 0) {
            mLockOutTimeTextView.setText(mTimes[0]);
        } else if (option == 1) {
            mLockOutTimeTextView.setText(mTimes[1]);
        } else if (option == 2) {
            mLockOutTimeTextView.setText(mTimes[2]);
        }
    }

    /**
     * gets the version name from the gradle
     */
    public void setVersionCode(){
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            mVersionCode.setText("v" + version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * END OF UI UTILITIES
     */

    /**
     * UI LISTENERS
     */

    @OnClick(R.id.surface_tutorial)
    public void setSurfaceTutorialListener(){
        Intent tutorialIntent= new Intent(getApplicationContext(), TutorialActivity.class);
        startActivity(tutorialIntent);
    }

    @OnClick(R.id.surface_drive_mode)
    public void setSurfaceDriveModeListener(){
        new AlertDialog.Builder(this)
                .setTitle("Adjust Driving Mode")
                .setSingleChoiceItems(mDriveModes, mSharedPreferences.getInt("driveModeOption", 1), null) //default value set to 4mph = normal driving mode
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int button) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int button) {
                        dialog.dismiss();
                        int selectedOption = ((AlertDialog)dialog).getListView().getCheckedItemPosition();

                        mSharedPreferences
                                .edit()
                                .putInt("driveModeOption", selectedOption)
                                .apply();

                        mDriveModeTextView.setText(mDriveModes[selectedOption]);
                        EventBus.getDefault().postSticky(new PreferenceMessage("Changed"));
                    }
                })
                .show();
    }

    @OnClick(R.id.surface_unlock_mode)
    public void setSurfaceLockModeListener(){
        new AlertDialog.Builder(this)
                .setTitle("Adjust Unlock Timer")
                .setSingleChoiceItems(mTimes, mSharedPreferences.getInt("lockOutTime", 1), null) //default value set to 30000ms = 30s
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int button) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int button) {
                        dialog.dismiss();
                        int selectedOption = ((AlertDialog)dialog).getListView().getCheckedItemPosition();

                        mSharedPreferences
                                .edit()
                                .putInt("lockOutTime", selectedOption)
                                .apply();

                        mLockOutTimeTextView.setText(mTimes[selectedOption]);
                        EventBus.getDefault().postSticky(new PreferenceMessage("Changed"));
                    }
                })
                .show();
    }

    @OnClick(R.id.surface_feedback)
    public void setSurfaceFeedbackListener(){
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto: team@timeoutinitiative.com"));
        startActivity(Intent.createChooser(emailIntent, "Give Feedback"));
    }

    @OnClick(R.id.surface_play_store)
    public void setSurfacePlayStoreListner(){

        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }

        //TODO: create intent to open app in play store for rating
    }

    /**
     * check changed listener for the passenger mode switch
     */
    @OnCheckedChanged(R.id.passenger_switch)
    public void setSurfacePassengerSwitchListener(){
        if(mPassengerSwitch.isChecked()){
            enablePassengerMode();
        }
        else if(!mPassengerSwitch.isChecked()){
            disablePassengerMode();
        }
    }

    /**
     * END OF UI LISTENERS
     */

    public void returnDefaultSharedPreferences(){
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    /**
     * PREFERENCES
     */

    /**
     * updates passenger shared preferences value
     */

    private void enablePassengerMode(){
        mSharedPreferences
                .edit()
                .putBoolean(getString(R.string.passenger_mode_key), true)
                .apply();

        //notifies dashboard of ui change since it is still alive in background
        EventBus.getDefault().postSticky(new PreferenceMessage("true"));
        Toast.makeText(getApplicationContext(), "Passenger mode enabled", Toast.LENGTH_LONG).show();
    }

    /**
     * updates passenger shared preferences value
     */
    private void disablePassengerMode(){
        mSharedPreferences
                .edit()
                .putBoolean(getString(R.string.passenger_mode_key), false)
                .apply();

        //notifies dashboard of ui change since it is still alive in background
        EventBus.getDefault().postSticky(new PreferenceMessage("false"));
        Toast.makeText(getApplicationContext(), "Passenger mode disabled", Toast.LENGTH_LONG).show();
    }

    /**
     * END OF PREFERENCES
     */
}
