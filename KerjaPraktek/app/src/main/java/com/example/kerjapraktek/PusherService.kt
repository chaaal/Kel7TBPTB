package com.example.kerjapraktek

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.kerjapraktek.MainActivity
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.Channel
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import org.json.JSONException
import org.json.JSONObject

class PusherService(private val context: Context) {
    private lateinit var pusher: Pusher
    private lateinit var channel: Channel
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    companion object {
        private const val CHANNEL_ID = "PusherNotificationChannel"
        private const val CHANNEL_NAME = "Pusher Notifications"
        private const val NOTIFICATION_ID = 1
    }

    fun initialize() {
        // Create notification channel for Android Oreo and above
        createNotificationChannel()

        // Configure Pusher
        val options = PusherOptions()
        options.setCluster("ap1")

        pusher = Pusher("985bda4f2110f1365188", options)

        // Connect to Pusher
        pusher.connect(object : ConnectionEventListener {
            override fun onConnectionStateChange(change: ConnectionStateChange) {
                Log.i("Pusher", "State changed from ${change.previousState} to ${change.currentState}")
            }

            override fun onError(message: String, code: String, e: Exception) {
                Log.e("Pusher", "Connection error: code ($code), message ($message)", e)
            }
        }, ConnectionState.ALL)

        // Subscribe to channel and bind events
        channel = pusher.subscribe("kerja-praktek")

        // Example of binding to multiple event types
        channel.bind("New-Announcement") { announcement ->

            Log.d("PusherService", "Event received: ${announcement.data}")

            try {
                val data = JSONObject(announcement.data)
                if (data.has("message") && data.has("title")) {
                    val message = data.getString("message")
                    val title = data.getString("title")
                    showNotification(title, message)
                } else {
                    showNotification("New Announcement", "Message or title missing")
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                showNotification("New Announcement", "Data parsing failed")
            }
        }

        // Initialize permission launcher
        permissionLauncher = (context as MainActivity).registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (!isGranted) {
                Log.e("PusherService", "POST_NOTIFICATIONS permission not granted")
            }
        }

        // Check and request POST_NOTIFICATIONS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }



    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Pusher Real-time Notifications"
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(title: String, content: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        ) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(NOTIFICATION_ID, builder.build())
            } else {
                // Handle the case where the permission is not granted
                Log.e("PusherService", "POST_NOTIFICATIONS permission not granted")
            }
        } else {
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        }
    }

}