package co.japl.android.ev_ride_connect.core.usecase.api

import co.japl.android.ev_ride_connect.interfaces.model.EvConfig
import co.japl.android.ev_ride_connect.interfaces.model.EvData
import co.japl.android.ev_ride_connect.interfaces.model.LlmConfig

data class SplashPreloadData(
    val latestEvData: EvData?,
    val evConfig: EvConfig?,
    val activeLlmConfigs: List<LlmConfig>,
    val allLlmConfigs: List<LlmConfig>
)

interface SplashUseCase {
    suspend fun preloadSplashData(): SplashPreloadData
}

