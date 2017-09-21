package com.brianroper.putitdown.model.driving;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brianroper.putitdown.R;
import com.brianroper.putitdown.utils.Constants;
import com.brianroper.putitdown.model.realmObjects.DrivingEventLog;
import com.brianroper.putitdown.model.events.DrivingMessage;
import com.brianroper.putitdown.utils.Utils;
import com.brianroper.putitdown.views.ContinueDriveActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;
import java.util.Random;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.internal.Util;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by brianroper on 5/2/17.
 */

public class DrivingLockScreen {

    private Context mContext;
    private RelativeLayout mRelativeLayout;
    private ImageButton mOverflowButton;
    private RelativeLayout mOverflowUnlockLayout;
    private RelativeLayout mOverflowPassengerLayout;
    private ImageView mCarImageView;
    private RelativeLayout mPassengerDialogLayout;
    private ImageView mPassengerDialogInfoButton;
    private TextView mPassengerDialogConfirmButton;
    private TextView mPassengerDialogCancelButton;
    private FrameLayout mInvisibleClickView;
    private TextView mPassengerDialogHint;

    private SharedPreferences mSharedPreferences;

    public DrivingLockScreen(Context context) {
        mContext = context;
    }

    /**
     * draws screen overlay
     */
    public void startDriving(){
        if(mRelativeLayout != null){
            return;
        }

        mRelativeLayout = new RelativeLayout(mContext);

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_driving, mRelativeLayout);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            layoutParams.flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        } else{
            layoutParams.flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            layoutParams.flags = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }

        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        layoutParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

        mRelativeLayout.setSystemUiVisibility(uiOptions);

        WindowManager windowManager = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);
        windowManager.addView(mRelativeLayout, layoutParams);

        initializeViews(mRelativeLayout);
        setBounceInterpolator();
    }

    /**
     * removes the screen overlay
     */
    public void stopDriving(){
        if(mRelativeLayout == null){
            return;
        }

        WindowManager windowManager = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        mRelativeLayout.setSystemUiVisibility(uiOptions);
        windowManager.removeView(mRelativeLayout);
        mRelativeLayout = null;

        Constants constants = new Constants();
        EventBus.getDefault().postSticky(new DrivingMessage(constants.DRIVING_EVENT_STOPPED));
    }

    /**
     * initialize views for the screen overlay
     */
    public void initializeViews(RelativeLayout root){
        mOverflowButton = (ImageButton) root.findViewById(R.id.overflow_button);
        mOverflowUnlockLayout = (RelativeLayout) root.findViewById(R.id.overflow_layout_unlock);
        mOverflowPassengerLayout = (RelativeLayout) root.findViewById(R.id.overflow_layout_passenger);
        mCarImageView = (ImageView) root.findViewById(R.id.car_image);
        mPassengerDialogLayout = (RelativeLayout) root.findViewById(R.id.passenger_dialog);
        mPassengerDialogCancelButton = (TextView) root.findViewById(R.id.passenger_dialog_cancel);
        mPassengerDialogConfirmButton = (TextView) root.findViewById(R.id.passenger_dialog_confirm);
        mPassengerDialogInfoButton = (ImageView) root.findViewById(R.id.passenger_dialog_info);
        mInvisibleClickView = (FrameLayout) root.findViewById(R.id.invisible_click_view);
        mPassengerDialogHint = (TextView) root.findViewById(R.id.passenger_dialog_hint);

        mOverflowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOverflow();
            }
        });

        mOverflowUnlockLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFactNotification();
                addFailedDrivingEvent();
                Utils.enableDeviceRinger(mContext);
                stopDriving();
                Constants constants = new Constants();
                postDrivingEventStatus(constants.UNLOCK_STATUS_TRUE);
            }
        });

        mOverflowPassengerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPassengerDialogLayout.setVisibility(View.VISIBLE);
                mInvisibleClickView.setVisibility(View.VISIBLE);
            }
        });

        mPassengerDialogConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFailedDrivingEvent();
                Utils.enableDeviceRinger(mContext);
                stopDriving();
                Constants constants = new Constants();
                postDrivingEventStatus(constants.UNLOCK_STATUS_FALSE);
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                sharedPreferences.edit().putBoolean(mContext.getString(R.string.passenger_mode_key), true);
                mPassengerDialogLayout.setVisibility(View.GONE);
                mInvisibleClickView.setVisibility(View.GONE);
            }
        });

        mPassengerDialogCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPassengerDialogLayout.setVisibility(View.GONE);
                mInvisibleClickView.setVisibility(View.GONE);
            }
        });

        mPassengerDialogInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mPassengerDialogHint.getVisibility() == View.GONE){
                    mPassengerDialogHint.setVisibility(View.VISIBLE);
                }
                if(mPassengerDialogHint.getVisibility() == View.VISIBLE){
                    mPassengerDialogHint.setVisibility(View.GONE);
                }
            }
        });

        mInvisibleClickView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mInvisibleClickView.getVisibility() == View.VISIBLE){
                    mInvisibleClickView.setVisibility(View.GONE);
                    mPassengerDialogLayout.setVisibility(View.GONE);
                }
            }
        });

        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOverflowUnlockLayout.getVisibility() == View.GONE){
                    return;
                }
                if(mOverflowUnlockLayout.getVisibility() == View.VISIBLE){
                    hideOverflow();
                }
            }
        });
    }

    /**
     * sets the overflow menu text view to visible
     */
    public void showOverflow(){
        mOverflowUnlockLayout.setVisibility(View.VISIBLE);
        animateOverflowTextViews();
        mOverflowPassengerLayout.setVisibility(View.VISIBLE);
    }

    /**
     * sets the overflow menu text view to gone
     */
    public void hideOverflow(){
        mOverflowUnlockLayout.setVisibility(View.GONE);
        mOverflowPassengerLayout.setVisibility(View.GONE);
    }

    /**
     * handles the bounce animation for the car image view
     */
    public void setBounceInterpolator(){
        final ObjectAnimator objectAnimator =
                ObjectAnimator.ofFloat(mCarImageView, "translationY", 0, 50, 0);
        objectAnimator.setInterpolator(new BounceInterpolator());
        objectAnimator.setDuration(1000);
        objectAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        objectAnimator.start();
    }

    /**
     * adds the successful driving event data to the local storage
     */
    private void addFailedDrivingEvent(){
        try{
            final Calendar calendar = Calendar.getInstance();
            Realm realm;
            Realm.init(mContext);
            RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                    .deleteRealmIfMigrationNeeded()
                    .build();
            realm = Realm.getInstance(realmConfiguration);
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    DrivingEventLog drivingEventLog = realm.createObject(
                            DrivingEventLog.class, Utils.returnDateAsDate().getTime() + "");
                    drivingEventLog.setTime(Utils.returnTime(calendar));
                    drivingEventLog.setDate(Utils.returnDateAsDate());
                    drivingEventLog.setSuccessful(false);
                    realm.copyToRealmOrUpdate(drivingEventLog);
                }
            });
            realm.close();
        }catch (Exception e){e.printStackTrace();}
    }

    /**
     * builds and sends notification containing texting and driving facts
     */
    public void sendFactNotification(){

        Random random = new Random();
        int randomIndex = random.nextInt(mContext.getResources().getStringArray(R.array.timeout_facts).length);

        //creates pending intent that notification action will perform
        Intent drivingIntent = new Intent(mContext, ContinueDriveActivity.class);
        drivingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, drivingIntent, 0);

        //builds the basic notification using the array stored in strings.xml
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_trip_failed)
                .setContentTitle(mContext.getResources().getString(R.string.notification_failed_title))
                .setContentText(mContext.getResources().getStringArray(R.array.timeout_facts)[randomIndex])
                .addAction(R.drawable.redcar,
                        mContext.getResources().getString(R.string.notification_failed_button),
                        pendingIntent);

        //shows notification text on the status bar when received
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setDefaults(Notification.DEFAULT_VIBRATE);

        //allows for the full content of longer facts to be displayed in the notification
        NotificationCompat.BigTextStyle bigTextStyle =
                new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle("Dont text and drive!");
        bigTextStyle.bigText(mContext.getResources().getStringArray(R.array.timeout_facts)[0]);
        builder.setStyle(bigTextStyle);

        //sends the notification
        NotificationManager manager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(001, builder.build());
    }

    /**
     * broadcast a message that driving is in progress or stopped
     */
    public void postDrivingEventStatus(final String isDriving){
        EventBus.getDefault().postSticky(new DrivingMessage(isDriving));
    }

    public void animateOverflowTextViews(){
        ValueAnimator unlockAnimator = ValueAnimator.ofFloat(500f, 0f);
        unlockAnimator.setDuration(200);
        unlockAnimator.start();

        unlockAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator updatedAnimation) {
                // You can use the animated value in a property that uses the
                // same type as the animation. In this case, you can use the
                // float value in the translationX property.
                float animatedValue = (float)updatedAnimation.getAnimatedValue();
                mOverflowUnlockLayout.setTranslationX(animatedValue);
            }
        });

        ValueAnimator passengerAnimator = ValueAnimator.ofFloat(500f, 0f);
        passengerAnimator.setDuration(250);
        passengerAnimator.start();

        passengerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator updatedAnimation) {
                // You can use the animated value in a property that uses the
                // same type as the animation. In this case, you can use the
                // float value in the translationX property.
                float animatedValue = (float)updatedAnimation.getAnimatedValue();
                mOverflowPassengerLayout.setTranslationX(animatedValue);
            }
        });
    }
}
