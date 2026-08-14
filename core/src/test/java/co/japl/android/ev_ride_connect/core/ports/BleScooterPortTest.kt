package co.japl.android.ev_ride_connect.core.ports

import co.japl.android.ev_ride_connect.core.domain.ScooterState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import uk.co.jemos.podam.api.PodamFactoryImpl

class BleScooterPortTest {

    private val podamFactory = PodamFactoryImpl()

    @Test
    fun shouldObserveScooterState() = runTest {
        val expectedState = podamFactory.manufacturePojo(ScooterState::class.java)
        val port = FakeBleScooterPort(expectedState)

        val result = port.observeScooterState().first()

        assertThat(result).isEqualTo(expectedState)
    }

    @Test
    fun shouldSendCommandToScooter() {
        val port = FakeBleScooterPort()
        val dpId = 1
        val commandValue = true

        port.sendCommand(dpId, commandValue)

        assertThat(port.lastSentDpId).isEqualTo(dpId)
        assertThat(port.lastSentValue).isEqualTo(commandValue)
    }

    private class FakeBleScooterPort(
        private val stateToEmit: ScooterState? = null
    ) : BleScooterPort {

        var lastSentDpId: Int? = null
        var lastSentValue: Any? = null

        override fun observeScooterState(): Flow<ScooterState> {
            return flowOf(stateToEmit ?: error("State not provided"))
        }

        override fun sendCommand(dpId: Int, value: Any) {
            lastSentDpId = dpId
            lastSentValue = value
        }
    }
}
