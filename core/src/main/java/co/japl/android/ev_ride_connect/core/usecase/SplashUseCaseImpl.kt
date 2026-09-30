package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.interfaces.ports.EvConfigPort
import co.japl.android.ev_ride_connect.interfaces.ports.EvDataPort
import co.japl.android.ev_ride_connect.interfaces.ports.LlmConfigPort
import co.japl.android.ev_ride_connect.interfaces.usecase.SplashPreloadData
import co.japl.android.ev_ride_connect.interfaces.usecase.SplashUseCase
import javax.inject.Inject

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
