package co.japl.android.ev_ride_connect.core.ports

import co.japl.android.ev_ride_connect.core.domain.ActiveSession
import kotlinx.coroutines.flow.Flow

interface SessionStatePort {
    suspend fun saveActiveSession(session: ActiveSession)
    suspend fun getActiveSession(): ActiveSession?
    fun observeActiveSession(): Flow<ActiveSession?>
    suspend fun clearActiveSession()
}
