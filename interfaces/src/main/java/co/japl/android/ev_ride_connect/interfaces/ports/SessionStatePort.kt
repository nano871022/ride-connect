package co.japl.android.ev_ride_connect.interfaces.ports

import co.japl.android.ev_ride_connect.interfaces.model.ActiveSession
import kotlinx.coroutines.flow.Flow

interface SessionStatePort {
    suspend fun saveActiveSession(session: ActiveSession)
    suspend fun getActiveSession(): ActiveSession?
    fun observeActiveSession(): Flow<ActiveSession?>
    suspend fun clearActiveSession()
}
