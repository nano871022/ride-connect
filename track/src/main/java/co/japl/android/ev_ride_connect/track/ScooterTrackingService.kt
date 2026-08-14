package co.japl.android.ev_ride_connect.track

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import co.japl.android.ev_ride_connect.core.ports.BleScooterPort
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ScooterTrackingService : Service() {

    @Inject
    lateinit var bleScooterPort: BleScooterPort

    @Inject
    lateinit var tripDatabasePort: TripDatabasePort

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var trackingTracker: ScooterTrackingTracker

    override fun onCreate() {
        super.onCreate()
        trackingTracker = ScooterTrackingTracker(bleScooterPort, tripDatabasePort, serviceScope)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START_TRACKING

        when (action) {
            ACTION_START_TRACKING -> {
                val notification = createNotification()
                startForeground(NOTIFICATION_ID, notification)
                trackingTracker.startTracking()
            }
            ACTION_STOP_TRACKING -> {
                serviceScope.launch {
                    trackingTracker.stopTracking()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Scooter Tracking Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Tracks scooter ride status and distance in background"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Scooter Ride Tracking")
            .setContentText("Tracking current ride details...")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "SCOOTER_TRACKING_CHANNEL"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START_TRACKING = "co.japl.android.ev_ride_connect.action.START_TRACKING"
        const val ACTION_STOP_TRACKING = "co.japl.android.ev_ride_connect.action.STOP_TRACKING"
    }
}
