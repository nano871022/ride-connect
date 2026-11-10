package co.japl.android.ev_ride_connect.core.usecase.api

import co.japl.android.ev_ride_connect.interfaces.model.ActiveSession
import kotlinx.coroutines.flow.Flow

interface MainScaffoldUseCase {
    fun observeActiveSession(): Flow<ActiveSession?>
}

