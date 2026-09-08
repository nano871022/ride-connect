package co.japl.android.ev_ride_connect.track

object TrackingSettings {
    val CHANNEL_ID = "${ScooterTrackingService::class.java.name}.CHANNEL"
    const val NOTIFICATION_ID = 1001
    val ACTION_START_TRACKING = "${ScooterTrackingService::class.java.name}.ACTION_START_TRACKING"
    val ACTION_STOP_TRACKING = "${ScooterTrackingService::class.java.name}.ACTION_STOP_TRACKING"
    val ACTION_PROCESS_LLM_PROMPT = "${ScooterTrackingService::class.java.name}.ACTION_PROCESS_LLM_PROMPT"

    const val EXTRA_PROMPT = "EXTRA_PROMPT"
    const val EXTRA_MODEL_NAME = "EXTRA_MODEL_NAME"
    const val EXTRA_API_KEY = "EXTRA_API_KEY"
    const val EXTRA_PROMPT_TEMPLATE = "EXTRA_PROMPT_TEMPLATE"
}
