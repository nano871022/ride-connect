package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.domain.EvConfig
import co.japl.android.ev_ride_connect.core.domain.EvData
import co.japl.android.ev_ride_connect.core.domain.LlmConfig
import co.japl.android.ev_ride_connect.core.ports.EvConfigPort
import co.japl.android.ev_ride_connect.core.ports.EvDataPort
import co.japl.android.ev_ride_connect.core.ports.LlmConfigPort
import javax.inject.Inject

data class SplashPreloadData(
    val latestEvData: EvData?,
    val evConfig: EvConfig?,
    val activeLlmConfigs: List<LlmConfig>,
    val allLlmConfigs: List<LlmConfig>
)

interface SplashUseCase {
    suspend fun preloadSplashData(): SplashPreloadData
}

class SplashUseCaseImpl @Inject constructor(
    private val evDataPort: EvDataPort,
    private val evConfigPort: EvConfigPort,
    private val llmConfigPort: LlmConfigPort,
    private val getLatestEvDataUseCase: GetLatestEvDataUseCase,
    private val getEvConfigUseCase: GetEvConfigUseCase,
    private val getActiveLlmConfigsUseCase: GetActiveLlmConfigsUseCase,
    private val getAllLlmConfigsUseCase: GetAllLlmConfigsUseCase
) : SplashUseCase {
    override suspend fun preloadSplashData(): SplashPreloadData {
        val latestEvData = getLatestEvDataUseCase.execute()
        val evConfig = getEvConfigUseCase.execute()
        val activeLlmConfigs = getActiveLlmConfigsUseCase.execute()
        val allLlmConfigs = getAllLlmConfigsUseCase.execute()
        return SplashPreloadData(
            latestEvData = latestEvData,
            evConfig = evConfig,
            activeLlmConfigs = activeLlmConfigs,
            allLlmConfigs = allLlmConfigs
        )
    }
}
