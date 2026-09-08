package co.japl.android.ev_ride_connect.database

import co.japl.android.ev_ride_connect.core.domain.ActiveSession
import co.japl.android.ev_ride_connect.database.dao.ActiveSessionDao
import co.japl.android.ev_ride_connect.database.entities.ActiveSessionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import uk.co.jemos.podam.api.PodamFactoryImpl

class RoomActiveSessionAdapterTest {

    private val podamFactory = PodamFactoryImpl()
    private lateinit var fakeDao: FakeActiveSessionDao
    private lateinit var adapter: RoomActiveSessionAdapter

    private class FakeActiveSessionDao : ActiveSessionDao {
        var currentEntity: ActiveSessionEntity? = null

        override suspend fun insertActiveSession(session: ActiveSessionEntity) {
            currentEntity = session
        }

        override suspend fun getActiveSession(): ActiveSessionEntity? = currentEntity

        override fun observeActiveSession() = flowOf(currentEntity)

        override suspend fun clearActiveSession() {
            currentEntity = null
        }
    }

    @Before
    fun setUp() {
        fakeDao = FakeActiveSessionDao()
        adapter = RoomActiveSessionAdapter(fakeDao)
    }

    @Test
    fun shouldSaveAndGetActiveSession() = runTest {
        val session = podamFactory.manufacturePojo(ActiveSession::class.java).copy(
            isRideActive = true,
            pendingLlmPrompt = "Analyze telemetry"
        )

        adapter.saveActiveSession(session)
        val retrieved = adapter.getActiveSession()

        assertThat(retrieved).isNotNull
        assertThat(retrieved?.isRideActive).isTrue
        assertThat(retrieved?.pendingLlmPrompt).isEqualTo("Analyze telemetry")
    }

    @Test
    fun shouldObserveActiveSession() = runTest {
        val session = ActiveSession(isRideActive = true, currentDistanceKm = 12.5)
        adapter.saveActiveSession(session)

        val observed = adapter.observeActiveSession().first()

        assertThat(observed).isNotNull
        assertThat(observed?.currentDistanceKm).isEqualTo(12.5)
    }

    @Test
    fun shouldClearActiveSession() = runTest {
        val session = ActiveSession(isRideActive = true)
        adapter.saveActiveSession(session)

        adapter.clearActiveSession()
        val retrieved = adapter.getActiveSession()

        assertThat(retrieved).isNull()
    }
}
