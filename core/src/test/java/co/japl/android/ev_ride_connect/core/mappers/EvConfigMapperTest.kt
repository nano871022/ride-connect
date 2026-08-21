package co.japl.android.ev_ride_connect.core.mappers

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EvConfigMapperTest {

    @Test
    fun shouldParseMarkdownWrappedLlmJsonResponseCorrectly() {
        val jsonResponse = """
            ```json
            {
              "brand": "VSETT",
              "version": "C7 Plus",
              "motors": [
                {"name": "Rear Hub Motor", "watts": 350}
              ],
              "manufactoryYear": 2023,
              "manufactoryCompany": "Ningbo Vsett Intelligent Technology Co., Ltd.",
              "batteryTechnology": "Lithium-ion",
              "batteryVolts": 36,
              "batteryAmpers": 14.0,
              "brakeQuantity": 2,
              "brakeTechnology": "Hydraulic disc brakes",
              "suspensionTechnology": "Front suspension fork",
              "chargePower": "84W (42V 2A)",
              "otherCharacteristics": [
                "Dual battery system",
                "LCD display"
              ]
            }
            ```
        """.trimIndent()

        val result = EvConfigMapper.fromLlmResponse("buy vsett c7 plus", jsonResponse)

        assertThat(result.brand).isEqualTo("VSETT")
        assertThat(result.version).isEqualTo("C7 Plus")
        assertThat(result.manufactoryYear).isEqualTo("2023")
        assertThat(result.manufactoryCompany).isEqualTo("Ningbo Vsett Intelligent Technology Co., Ltd.")
        assertThat(result.batteryTechnology).isEqualTo("Lithium-ion")
        assertThat(result.batteryVolts).isEqualTo("36")
        assertThat(result.batteryAmpers).isEqualTo("14.0")
        assertThat(result.brakeQuantity).isEqualTo(2)
        assertThat(result.brakeTechnology).isEqualTo("Hydraulic disc brakes")
        assertThat(result.suspensionTechnology).isEqualTo("Front suspension fork")
        assertThat(result.chargePower).isEqualTo("84W (42V 2A)")
        assertThat(result.otherCharacteristics).contains("Dual battery system", "LCD display")
        assertThat(result.motors).hasSize(1)
        assertThat(result.motors.first().name).isEqualTo("Rear Hub Motor")
        assertThat(result.motors.first().watts).isEqualTo(350)
    }

    @Test
    fun shouldFallbackToDefaultVsettWhenJsonIsInvalidAndUserQueryMentionsVsett() {
        val result = EvConfigMapper.fromLlmResponse("buy vsett c7 plus", "invalid response")

        assertThat(result.brand).isEqualTo("VSETT")
        assertThat(result.version).isEqualTo("C7 Plus")
        assertThat(result.motors).hasSize(2)
        assertThat(result.batteryVolts).isEqualTo("60V")
    }
}
