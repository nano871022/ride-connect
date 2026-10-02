package co.japl.android.ev_ride_connect.core.domain

import co.japl.android.ev_ride_connect.interfaces.model.EvConfig
import co.japl.android.ev_ride_connect.interfaces.model.EvConstants
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import uk.co.jemos.podam.api.PodamFactoryImpl

class EvConfigTest {

    private val podamFactory = PodamFactoryImpl()

    @Test
    fun shouldInstantiateEvConfigWithPodam() {
        val config = podamFactory.manufacturePojo(EvConfig::class.java)

        assertThat(config).isNotNull
    }

    @Test
    fun shouldCreateEvConfigWithGivenValues() {
        val config = EvConfig(
            id = 1,
            brand = "VSETT",
            version = "C7 Plus",
            manufactoryYear = "2024",
            manufactoryCompany = "VSETT Mobility",
            batteryTechnology = "Li-Ion",
            batteryVolts = "52V",
            batteryAmpers = "20Ah",
            brakeQuantity = 2,
            brakeTechnology = "Hydraulic Disc",
            suspensionTechnology = "Hydraulic Spring",
            chargePower = "58.8V 2A",
            otherCharacteristics = "Dual Motor, NFC"
        )

        assertThat(config.id).isEqualTo(1)
        assertThat(config.brand).isEqualTo("VSETT")
        assertThat(config.version).isEqualTo("C7 Plus")
        assertThat(config.manufactoryYear).isEqualTo("2024")
        assertThat(config.manufactoryCompany).isEqualTo("VSETT Mobility")
        assertThat(config.batteryTechnology).isEqualTo("Li-Ion")
        assertThat(config.batteryVolts).isEqualTo("52V")
        assertThat(config.batteryAmpers).isEqualTo("20Ah")
        assertThat(config.brakeQuantity).isEqualTo(2)
        assertThat(config.brakeTechnology).isEqualTo("Hydraulic Disc")
        assertThat(config.suspensionTechnology).isEqualTo("Hydraulic Spring")
        assertThat(config.chargePower).isEqualTo("58.8V 2A")
        assertThat(config.otherCharacteristics).isEqualTo("Dual Motor, NFC")
    }

    @Test
    fun shouldConstantClassesUseDynamicClassReferences() {
        assertThat(EvConstants.EV_LLM_PROMPT_TEMPLATE).isNotNull
    }
}
