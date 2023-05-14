package com.towsal.towsal.fcm

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.towsal.towsal.R
import com.towsal.towsal.app.MainApplication
import com.towsal.towsal.helper.DelegateStatic
import com.towsal.towsal.helper.PreferenceHelper
import com.towsal.towsal.interfaces.RecyclerViewItemClick
import com.towsal.towsal.network.serializer.trips.NotificationModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.views.activities.MainActivity
import com.towsal.towsal.views.activities.SplashActivity
import com.towsal.towsal.views.adapters.NotificationAdapter.Companion.SEND_MESSAGE
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Firebase Service class
 * */
@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val tag = "FirebaseMessagingService"
    private var notificationId = 1
    private var channelId = "channel-01"
    private var channelName = "Channel Name"

    private var broadcaster: LocalBroadcastManager? = null

    val preferenceHelper: PreferenceHelper by inject()


    private var importance = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        NotificationManager.IMPORTANCE_HIGH
    } else {
        0
    }

    override fun onCreate() {
        broadcaster = LocalBroadcastManager.getInstance(this)
    }


    @WorkerThread
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        try {
            if (remoteMessage.notification != null || remoteMessage.data.isEmpty()) {
                showNotification(
                    remoteMessage.notification?.title,
                    remoteMessage.notification?.body,
                    remoteMessage
                )

            } else {
                showNotification(
                    remoteMessage.data["title"],
                    remoteMessage.data["message"],
                    remoteMessage
                )
            }

        } catch (e: Exception) {
            println("$tag error -->${e.localizedMessage}")
        }
    }

    /**
     * Function for showing notification
     * */
    @SuppressLint("UnspecifiedImmutableFlag")
    private fun showNotification(
        title: String?,
        body: String?,
        remoteMessage: RemoteMessage
    ) {
        DelegateStatic.notificationType = remoteMessage.data["not_type"]?.toInt() ?: -1

        val intent =
            if (isAppIsInBackground(this)) {
                Intent(this, SplashActivity::class.java)

            } else {
                if (DelegateStatic.tmainActivity != null) {
                    DelegateStatic.tmainActivity.setBadgeIcon()
                }
                MainActivity.GOTO_TRIPS_FRAGMENT
                Intent(this, MainActivity::class.java)
            }
        /*val model = NotificationModel()
        model.notification_title = remoteMessage.notification?.title.toString()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.putExtra(Constants.TRIGGER_NOTIFICATION, 1)
        intent.putExtra("notificationTitle", model.notification_title)
        intent.action = UUID.randomUUID().toString()*/
        var isMessageNotification = false
        if (remoteMessage.data.isNotEmpty() && remoteMessage.data["not_type"] != null) {
            if ((remoteMessage.data["not_type"]?.toInt() ?: -1) == SEND_MESSAGE) {
                var getId = preferenceHelper.getInt(Constants.DataParsing.USER_THREAD_ID, 0)
                if (getId == remoteMessage.data["thread_id"]?.toInt()) {
                    isMessageNotification = true
                    val broadcastIntent = Intent(Constants.BROCAST_RECIEVER)
                    broadcastIntent.putExtra(Constants.TYPE, Constants.MESSAGE)
                    broadcastIntent.putExtra(Constants.DataParsing.THREAD_ID, remoteMessage.data["thread_id"].toString())
                    broadcastIntent.putExtra(Constants.DataParsing.MESSAGE_ID, remoteMessage.data["message_id"].toString())
                    broadcastIntent.putExtra(Constants.DataParsing.TEXT_MESSAGE, remoteMessage.data["message"].toString())
                    MainApplication.applicationContext().sendBroadcast(broadcastIntent)
                }
            }
        }
        if (!isMessageNotification) {
            val myDate = Date()
            notificationId = SimpleDateFormat("ddhhmmss", Locale.US).format(myDate).toInt()
            val pendingIntent = PendingIntent.getActivity(
                this, notificationId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT
            )

            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(soundUri)
            notificationBuilder.setContentIntent(pendingIntent)
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                @SuppressLint("WrongConstant") val mChannel = NotificationChannel(
                    channelId, channelName, importance
                )
                mChannel.enableVibration(true)
                notificationBuilder.setChannelId(channelId)
                notificationManager.createNotificationChannel(mChannel)
            }

            notificationManager.notify(notificationId, notificationBuilder.build())
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartServiceIntent = if (isAppIsInBackground(this)) {
            Intent(this, SplashActivity::class.java)
        } else {
            Intent(this, MainActivity::class.java)
        }
        restartServiceIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        restartServiceIntent.putExtra(Constants.TRIGGER_NOTIFICATION, 1)
        restartServiceIntent.setPackage(packageName)
        if (notificationId == 0) {
            val myDate = Date()
            notificationId = SimpleDateFormat("Adham's", Locale.US).format(myDate).toInt()
        }
        val restartServicePendingIntent = PendingIntent.getService(
            applicationContext,
            notificationId,
            restartServiceIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT
        )
        val alarmService =
            applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService[AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000] =
            restartServicePendingIntent
        super.onTaskRemoved(rootIntent)
    }

    /**
     * Function for checking app is in background or not
     * */
    private fun isAppIsInBackground(context: Context): Boolean {
        var isInBackground = true
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningProcesses =
            am.runningAppProcesses
        for (processInfo in runningProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (activeProcess in processInfo.pkgList) {
                    if (activeProcess == context.packageName) {
                        isInBackground = false
                    }
                }
            }
        }
        return isInBackground
    }
}