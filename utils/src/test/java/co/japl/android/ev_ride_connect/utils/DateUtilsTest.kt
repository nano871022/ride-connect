package co.japl.android.ev_ride_connect.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.Locale

class DateUtilsTest {

    @Test
    fun shouldFormatTimestampToStringCorrectly() {
        val timestamp = 1620000000000L // 2021-05-03 00:00:00 UTC
        val result = DateUtils.formatTimestamp(timestamp, Locale.US)

        assertThat(result).isNotBlank
        assertThat(result).contains("2021")
    }

    @Test
    fun shouldReturnEmptyStringWhenTimestampIsZeroOrNegative() {
        assertThat(DateUtils.formatTimestamp(0L, Locale.US)).isEmpty()
        assertThat(DateUtils.formatTimestamp(-1L, Locale.US)).isEmpty()
    }
}
