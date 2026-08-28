package co.japl.android.ev_ride_connect.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {

    private const val DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm"

    fun formatTimestamp(timestamp: Long, locale: Locale = Locale.getDefault()): String {
        if (timestamp <= 0) return ""
        val sdf = SimpleDateFormat(DEFAULT_DATE_FORMAT, locale)
        return sdf.format(Date(timestamp))
    }

    fun formatDurationSeconds(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hours > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, secs)
        }
    }
}
