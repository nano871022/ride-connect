package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.domain.EvConfig
import co.japl.android.ev_ride_connect.core.domain.EvConstants
import co.japl.android.ev_ride_connect.core.domain.LlmConfig
import co.japl.android.ev_ride_connect.core.mappers.EvConfigMapper
import co.japl.android.ev_ride_connect.core.ports.LlmClientPort
import javax.inject.Inject

class FetchEvInfoUseCase @Inject constructor(
    private val llmClientPort: LlmClientPort
) {

    suspend fun execute(requestText: String, config: LlmConfig, currentEvConfig: EvConfig): EvConfig {
        val prompt = String.format(
            EvConstants.EV_LLM_PROMPT_TEMPLATE.trimIndent(),
            requestText
        )
        val targetModel = config.selectedVersion.ifBlank { config.modelName }
        val responseText = llmClientPort.generateResponse(targetModel, config.apiKey, prompt)
        val parsedConfig = EvConfigMapper.fromLlmResponse(requestText, responseText)

        return currentEvConfig.copy(
            brand = parsedConfig.brand.ifBlank { currentEvConfig.brand },
            version = parsedConfig.version.ifBlank { currentEvConfig.version },
            motors = parsedConfig.motors.ifEmpty { currentEvConfig.motors },
            manufactoryYear = parsedConfig.manufactoryYear.ifBlank { currentEvConfig.manufactoryYear },
            manufactoryCompany = parsedConfig.manufactoryCompany.ifBlank { currentEvConfig.manufactoryCompany },
            batteryTechnology = parsedConfig.batteryTechnology.ifBlank { currentEvConfig.batteryTechnology },
            batteryVolts = parsedConfig.batteryVolts.ifBlank { currentEvConfig.batteryVolts },
            batteryAmpers = parsedConfig.batteryAmpers.ifBlank { currentEvConfig.batteryAmpers },
            brakeQuantity = if (parsedConfig.brakeQuantity > 0) parsedConfig.brakeQuantity else currentEvConfig.brakeQuantity,
            brakeTechnology = parsedConfig.brakeTechnology.ifBlank { currentEvConfig.brakeTechnology },
            suspensionTechnology = parsedConfig.suspensionTechnology.ifBlank { currentEvConfig.suspensionTechnology },
            chargePower = parsedConfig.chargePower.ifBlank { currentEvConfig.chargePower },
            otherCharacteristics = parsedConfig.otherCharacteristics.ifBlank { currentEvConfig.otherCharacteristics }
        )
    }
}
