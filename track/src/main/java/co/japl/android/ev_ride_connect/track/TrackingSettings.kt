package co.japl.android.ev_ride_connect.track

object TrackingSettings {
    val CHANNEL_ID = "${ScooterTrackingService::class.java.name}.CHANNEL"
    const val NOTIFICATION_ID = 1001
    val ACTION_START_TRACKING = "${ScooterTrackingService::class.java.name}.ACTION_START_TRACKING"
    val ACTION_STOP_TRACKING = "${ScooterTrackingService::class.java.name}.ACTION_STOP_TRACKING"
}
