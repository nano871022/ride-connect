package co.japl.android.ev_ride_connect.database

import co.japl.android.ev_ride_connect.core.domain.ActiveSession
import co.japl.android.ev_ride_connect.core.ports.SessionStatePort
import co.japl.android.ev_ride_connect.database.dao.ActiveSessionDao
import co.japl.android.ev_ride_connect.database.entities.ActiveSessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomActiveSessionAdapter(
    private val activeSessionDao: ActiveSessionDao
) : SessionStatePort {

    override suspend fun saveActiveSession(session: ActiveSession) {
        val entity = ActiveSessionEntity(
            id = 1L,
            isRideActive = session.isRideActive,
            startTimeMillis = session.startTimeMillis,
            currentDurationMillis = session.currentDurationMillis,
            currentDistanceKm = session.currentDistanceKm,
            pendingLlmPrompt = session.pendingLlmPrompt,
            pendingLlmResponse = session.pendingLlmResponse,
            isLlmProcessing = session.isLlmProcessing,
            cachedTelemetryCount = session.cachedTelemetryCount,
            lastUpdatedTmst = session.lastUpdatedTmst
        )
        activeSessionDao.insertActiveSession(entity)
    }

    override suspend fun getActiveSession(): ActiveSession? {
        return activeSessionDao.getActiveSession()?.toDomain()
    }

    override fun observeActiveSession(): Flow<ActiveSession?> {
        return activeSessionDao.observeActiveSession().map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun clearActiveSession() {
        activeSessionDao.clearActiveSession()
    }

    private fun ActiveSessionEntity.toDomain(): ActiveSession {
        return ActiveSession(
            id = this.id,
            isRideActive = this.isRideActive,
            startTimeMillis = this.startTimeMillis,
            currentDurationMillis = this.currentDurationMillis,
            currentDistanceKm = this.currentDistanceKm,
            pendingLlmPrompt = this.pendingLlmPrompt,
            pendingLlmResponse = this.pendingLlmResponse,
            isLlmProcessing = this.isLlmProcessing,
            cachedTelemetryCount = this.cachedTelemetryCount,
            lastUpdatedTmst = this.lastUpdatedTmst
        )
    }
}
