package com.brianroper.putitdown.model.driving

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import com.brianroper.putitdown.R
import com.brianroper.putitdown.model.events.DrivingMessage
import com.brianroper.putitdown.model.realmObjects.DrivingEventLog
import com.brianroper.putitdown.utils.Utils
import com.brianroper.putitdown.views.DrivingLogActivity
import io.nlopez.smartlocation.SmartLocation
import io.realm.Realm
import io.realm.RealmConfiguration
import org.greenrobot.eventbus.EventBus
import java.util.*

class DrivingActionHelper() {

    var mContext: Context? = null

    constructor(context: Context) : this(){
        mContext = context
    }

    /**
     * adds the successful driving event data to the local storage
     */
    fun addSuccessfulDrivingEvent(isSuccessful: Boolean) {
        val calendar = Calendar.getInstance()
        val realm: Realm
        val contextInstance = mContext
        Realm.init(contextInstance)
        val realmConfiguration = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()
        realm = Realm.getInstance(realmConfiguration)
        realm.executeTransaction { realm ->
            val drivingEventLog = realm.createObject(DrivingEventLog::class.java, Utils.returnDateAsDate().time.toString() + "")
            drivingEventLog.time = Utils.returnTime(calendar)
            drivingEventLog.date = Utils.returnDateAsDate()
            drivingEventLog.isSuccessful = isSuccessful
            realm.copyToRealmOrUpdate(drivingEventLog)
            EventBus.getDefault().postSticky(DrivingMessage("newLog"))
        }
        realm.close()
    }

    /**
     * sends a notification to the user when they complete a safe driving session
     */
    fun sendSuccessNotification() {
        //notification will open the DrivingLogActivity when clicked
        val logIntent = Intent(mContext, DrivingLogActivity::class.java)
        logIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val pendingIntent = PendingIntent.getActivity(mContext, 0, logIntent, 0)

        val builder = NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_trip_success)
                .setContentTitle(
                        mContext?.resources?.getString(R.string.notification_success_title))
                .setContentText(
                        mContext?.resources?.getString(R.string.notification_success_content))
                .addAction(R.drawable.redcar,
                        mContext?.getString(R.string.notification_success_button),
                        pendingIntent)

        //shows notification text on the status bar when received
        builder.priority = NotificationCompat.PRIORITY_HIGH
        builder.setDefaults(Notification.DEFAULT_VIBRATE)

        //sends the notification
        val manager = mContext?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(2, builder.build())
    }

    // Check if the location services are enabled
    fun isLocationEnabled(): Boolean{
        return SmartLocation.with(mContext).location().state().locationServicesEnabled()
    }

    // Check if any provider (network or gps) is enabled
    fun isProviderAvailable(): Boolean{
        return SmartLocation.with(mContext).location().state().isAnyProviderAvailable()
    }

    // Check if GPS is available
    fun isGpsAvailable(): Boolean{
        return SmartLocation.with(mContext).location().state().isGpsAvailable()
    }

    // Check if Network is available
    fun isNetworkAvailable(): Boolean{
        return SmartLocation.with(mContext).location().state().isNetworkAvailable()
    }

    // Check if the passive provider is available
    fun isPassiveProviderAvailable(): Boolean{
        return SmartLocation.with(mContext).location().state().isPassiveAvailable()
    }

    // Check if the location is mocked
    fun isMockSettingsEnabled(): Boolean{
       return SmartLocation.with(mContext).location().state().isMockSettingEnabled
    }
}