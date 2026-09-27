package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.domain.BatteryMode
import co.japl.android.ev_ride_connect.core.domain.EvConfig
import co.japl.android.ev_ride_connect.utils.BatteryCalculator
import javax.inject.Inject

class CalculateDynamicBatteryPercentageUseCase @Inject constructor() {

    fun execute(inputValue: Double, batteryMode: BatteryMode, minVoltage: Double, maxVoltage: Double): Short {
        return when (batteryMode) {
            BatteryMode.PERCENTAGE -> inputValue.toInt().coerceIn(0, 100).toShort()
            BatteryMode.VOLTAGE -> BatteryCalculator.calculatePercentage(inputValue, minVoltage, maxVoltage).toShort()
        }
    }

    fun execute(inputValue: Double, evConfig: EvConfig?): Short {
        val mode = evConfig?.batteryMode ?: BatteryMode.PERCENTAGE
        val minV = evConfig?.minVoltage ?: 39.0
        val maxV = evConfig?.maxVoltage ?: 54.6
        return execute(inputValue, mode, minV, maxV)
    }
}
