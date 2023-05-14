package com.towsal.towsal.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.towsal.towsal.R
import com.towsal.towsal.views.fragments.HostTripDetailFragment

/**
* Broadcast Receiver class for handling local notification for drop off time
 * */
class MyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val channelId = "channel-01"
        val channelName = "Channel Name"
        if (intent.getStringExtra("myAction") != null && intent.getStringExtra("myAction") == "notify") {
            val i = Intent(context, HostTripDetailFragment::class.java)
            i.putExtra(Constants.DataParsing.ID, intent.extras!!.getInt(Constants.DataParsing.ID))
            i.putExtra(Constants.DataParsing.FLOW_COMING_FROM, intent.extras!!.getInt(Constants.DataParsing.FLOW_COMING_FROM))
            i.putExtra(Constants.DataParsing.CHANGE_REQ_ID, intent.extras!!.getInt(Constants.DataParsing.CHANGE_REQ_ID))


            val pendingIntent =
                PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_ONE_SHOT)
            val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
			val importance = NotificationManager.IMPORTANCE_HIGH

            val notificationBuilder = NotificationCompat.Builder(context, "Notification")
                .setSmallIcon(R.drawable.ic_app_logo_white)
                .setContentTitle("Drop-off Alert!")
                .setContentText(
                    "Here's a reminder that your trip is coming to end.\n" +
                            "please click to here to view details"
                )
                .setAutoCancel(true)
                .setSound(notificationSound)
                .setContentIntent(pendingIntent)

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                notificationBuilder.setChannelId(channelId)
                val mChannel = NotificationChannel(channelId, channelName, importance)
                notificationManager.createNotificationChannel(mChannel)
            }
            notificationManager.notify(0, notificationBuilder.build())

        }
    }
}