package co.japl.android.ev_ride_connect.core.ports

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TripDatabasePortTest {

    @Test
    fun shouldSaveTripData() = runTest {
        val port = FakeTripDatabasePort()
        val expectedDistance = 1500
        val expectedBatteryConsumed = 25

        port.saveTripData(expectedDistance, expectedBatteryConsumed)

        assertThat(port.savedDistance).isEqualTo(expectedDistance)
        assertThat(port.savedBatteryConsumed).isEqualTo(expectedBatteryConsumed)
    }

    private class FakeTripDatabasePort : TripDatabasePort {
        var savedDistance: Int? = null
        var savedBatteryConsumed: Int? = null

        override suspend fun saveTripData(distance: Int, batteryConsumed: Int) {
            savedDistance = distance
            savedBatteryConsumed = batteryConsumed
        }
    }
}
