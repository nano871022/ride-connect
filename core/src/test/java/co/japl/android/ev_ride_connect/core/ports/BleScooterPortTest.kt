package co.japl.android.ev_ride_connect.interfaces.ports

import co.japl.android.ev_ride_connect.interfaces.model.BleLogEntry
import co.japl.android.ev_ride_connect.interfaces.model.ScooterState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

        private val _scooterState = MutableStateFlow(stateToEmit ?: ScooterState())
        override val scooterState: StateFlow<ScooterState> = _scooterState.asStateFlow()

        var lastSentDpId: Int? = null
        var lastSentValue: Any? = null

        override fun observeScooterState(): Flow<ScooterState> {
            return flowOf(stateToEmit ?: error("State not provided"))
        }

        override fun observeConnectionState(): Flow<Boolean> = flowOf(false)

        override fun observeRawLogs(): Flow<List<BleLogEntry>> = flowOf(emptyList())

        override fun clearLogs() {}

        override fun sendCommand(dpId: Int, value: Any) {
            lastSentDpId = dpId
            lastSentValue = value
        }

        override fun connect(macAddress: String?) {}

        override suspend fun connect(deviceAddress: String): Boolean = true

        override suspend fun disconnect() {}

        override suspend fun toggleLock(lock: Boolean): Boolean = true

        override suspend fun setSpeedMode(mode: Int): Boolean = true

        override suspend fun toggleLight(lightOn: Boolean): Boolean = true
    }
}
