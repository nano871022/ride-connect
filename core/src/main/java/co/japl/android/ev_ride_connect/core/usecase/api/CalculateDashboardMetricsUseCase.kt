package co.japl.android.ev_ride_connect.core.usecase.api

import co.japl.android.ev_ride_connect.interfaces.model.DashboardMetrics
import co.japl.android.ev_ride_connect.interfaces.model.EvConfig
import co.japl.android.ev_ride_connect.interfaces.model.EvData
import co.japl.android.ev_ride_connect.interfaces.model.Trip

interface CalculateDashboardMetricsUseCase {
    suspend fun execute(
        trips: List<Trip>,
        latestEvData: EvData?,
        evConfig: EvConfig?
    ): DashboardMetrics
}
