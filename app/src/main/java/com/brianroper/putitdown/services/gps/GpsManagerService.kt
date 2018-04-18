package com.brianroper.putitdown.services.gps

import android.annotation.TargetApi
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.brianroper.putitdown.R
import com.brianroper.putitdown.model.driving.DrivingActionHelper
import com.brianroper.putitdown.model.events.DrivingMessage
import com.brianroper.putitdown.services.driving.DrivingLockService
import com.brianroper.putitdown.utils.Constants
import io.nlopez.smartlocation.OnLocationUpdatedListener
import io.nlopez.smartlocation.SmartLocation
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class GpsManagerService: Service() {

    private var mDrivingService: Intent? = null
    private var mIsPassengerMode = false
    private var mIsDriving = false
    private var mIsUnlocked = false

    private val DRIVING_LOCKOUT_RETRY_TIME = 5000
    private var DRIVING_STOPPED_DOUBLE_CHECK_TIME = 30000
    private var TARGET_LOCKOUT_SPEED = 5
    private val TARGET_STOPPED_SPEED = 0
    private var mCurrentSpeed = 0f

    private val mDrivingModeSpeeds = intArrayOf(2, 4, 6)
    private val mLockOutTimes = intArrayOf(15000, 30000, 45000)

    override fun onBind(intent: Intent?): IBinder {
        return Binder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        returnSharedPreferences()
        initializeDrivingService()
        registerSmartLocation()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterSmartLocation()
        Log.e("GpsManagerService: ", "SmartLocation unregistered")
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            handleForegroundServiceStart()
        }
    }

    private fun registerSmartLocation(){
        SmartLocation.with(this).location().start(object : OnLocationUpdatedListener {
            override fun onLocationUpdated(p0: Location?) {
                checkSpeed(p0)
            }
        })
        Log.e("GpsManagerService: ", "SmartLocation registered")
    }

    private fun checkSpeed(location: Location?){
        var speed: Float? = null
        if(location?.speed != null){
            speed = location!!.speed
            if (speed >= TARGET_LOCKOUT_SPEED){
                //if current speed is greater than 5 mph do something
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    handleAndroidOService(mDrivingService, true)
                } else {
                    startService(mDrivingService)
                }
                mIsDriving = true
                Log.e("GpsManagerService: ", "Device is traveling above 5mph")
            }
            if (mCurrentSpeed < TARGET_LOCKOUT_SPEED && mCurrentSpeed != TARGET_STOPPED_SPEED.toFloat()) {
                if (mIsDriving) {
                    stopService(mDrivingService)
                    Log.e("GpsManagerService: ", "Device is traveling below 5mph")
                }
            }
            if (mCurrentSpeed == TARGET_STOPPED_SPEED.toFloat()) {
                if (mIsDriving) {
                    //check after set seconds if speed is still 0. If so we log a successful driving session
                    mIsDriving = false
                    val handler = Handler()
                    handler.postDelayed({ handleStoppedDriving() }, DRIVING_STOPPED_DOUBLE_CHECK_TIME.toLong())
                    Log.e("GpsManagerService: ", "Device has stopped traveling")
                }
            }
        }
    }

    private fun unregisterSmartLocation(){
        SmartLocation.with(this).location().stop()
    }

    @TargetApi(26)
    private fun handleForegroundServiceStart() {
        startForeground(101, handlePersistentServiceNotification())
    }

    /**
     * returns the current shared preferences
     */
    fun returnSharedPreferences() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        mIsPassengerMode = sharedPreferences.getBoolean(getString(R.string.passenger_mode_key), false)

        val drivingMode = sharedPreferences.getInt("driveModeOption", 1) // default is normal driving mode = 4mph
        TARGET_LOCKOUT_SPEED = mDrivingModeSpeeds[drivingMode]

        val lockOutTime = sharedPreferences.getInt("lockOutTime", 1) // default is 30,000ms = 30s
        DRIVING_STOPPED_DOUBLE_CHECK_TIME = mLockOutTimes[lockOutTime]
    }

    /**
     * *** ANDROID O ***
     */

    /**
     * handles the new service system for android O
     * specifies that this code is targeted for API 26
     */
    @TargetApi(26)
    private fun handleAndroidOService(service: Intent?, action: Boolean) {
        if (action) {
            applicationContext.startForegroundService(service)
        } else if (!action) {
            stopForeground(true)
        }
        Log.i("AndroidVersion: ", "Oreo")
    }

    /**
     * handles persistent notification for Android O requirements,
     * having a "persistent" notification that is always visible to the user
     * allows the service to run since the app qualifies as in the foreground, since
     * a component of the app is visible.
     */
    private fun handlePersistentServiceNotification(): Notification {
        val builder = NotificationCompat.Builder(applicationContext)
                .setSmallIcon(R.drawable.redcar)
                .setContentTitle("TimeOut")
                .setContentText(
                        "TimeOut is monitoring your driving patterns")

        //shows notification text on the status bar when received
        builder.priority = NotificationCompat.PRIORITY_HIGH
        return builder.build()
    }

    /**
     * initializes the driving service
     */
    private fun initializeDrivingService() {
        mDrivingService = Intent(this, DrivingLockService::class.java)
    }

    /**
     * checks to see if the user has actually stopped driving
     */
    private fun handleStoppedDriving() {
        val drivingActionHelper: DrivingActionHelper = DrivingActionHelper(applicationContext)
        if (mCurrentSpeed == TARGET_STOPPED_SPEED.toFloat()) {
            drivingActionHelper.addSuccessfulDrivingEvent(true)
            drivingActionHelper.sendSuccessNotification()
            val constants = Constants()
            EventBus.getDefault().postSticky(DrivingMessage(constants.DRIVING_LOG_EVENT_SUCCESS))
        } else {
            mIsDriving = true
        }
    }

    private fun restartSession() {
        mIsUnlocked = true
        val handler = Handler()
        handler.postDelayed({
            mIsUnlocked = false
            val constants = Constants()
            EventBus.getDefault().postSticky(DrivingMessage(constants.DRIVING_LOG_EVENT_FAILED))
        }, DRIVING_LOCKOUT_RETRY_TIME.toLong())
    }

    /**
     * listens for a DrivingMessage from when it completes
     */
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onDrivingMessageEvent(drivingMessage: DrivingMessage) {
        val constants = Constants()
        if (drivingMessage.message === constants.UNLOCK_STATUS_FALSE) {

        }
        if (drivingMessage.message === constants.UNLOCK_STATUS_TRUE) {
            restartSession()
        }
    }
}