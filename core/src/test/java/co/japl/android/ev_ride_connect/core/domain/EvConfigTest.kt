package co.japl.android.ev_ride_connect.core.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import uk.co.jemos.podam.api.PodamFactoryImpl

class EvConfigTest {

    private val podamFactory = PodamFactoryImpl()

    @Test
    fun shouldInstantiateEvConfigWithPodam() {
        val evConfig = podamFactory.manufacturePojo(EvConfig::class.java)

        assertThat(evConfig).isNotNull
        assertThat(evConfig.brand).isNotNull
        assertThat(evConfig.version).isNotNull
    }

    @Test
    fun shouldInstantiateMotorSpecWithPodam() {
        val motorSpec = podamFactory.manufacturePojo(MotorSpec::class.java)

        assertThat(motorSpec).isNotNull
        assertThat(motorSpec.name).isNotNull
        assertThat(motorSpec.watts).isNotNull
    }

    @Test
    fun shouldCreateEvConfigWithGivenValues() {
        val motors = listOf(
            MotorSpec("Front Motor", 1000),
            MotorSpec("Rear Motor", 1000)
        )
        val config = EvConfig(
            id = 1L,
            request = "Vsett c7 plus by emove colombia seller",
            brand = "VSETT",
            version = "C7 Plus",
            motors = motors,
            manufactoryYear = "2023",
            manufactoryCompany = "VSETT / eMove Colombia",
            boughtDate = "2023-10-10",
            batteryTechnology = "Li-ion",
            batteryVolts = "60V",
            batteryAmpers = "20.8Ah",
            brakeQuantity = 2,
            brakeTechnology = "Hydraulic Disc Brake",
            suspensionTechnology = "Spring & Hydraulic Suspension",
            chargePower = "67.2V 2A",
            otherCharacteristics = "Dual motor scooter",
            isLoaded = true
        )

        assertThat(config.id).isEqualTo(1L)
        assertThat(config.request).isEqualTo("Vsett c7 plus by emove colombia seller")
        assertThat(config.brand).isEqualTo("VSETT")
        assertThat(config.version).isEqualTo("C7 Plus")
        assertThat(config.motors).hasSize(2)
        assertThat(config.motors.first().name).isEqualTo("Front Motor")
        assertThat(config.motors.first().watts).isEqualTo(1000)
        assertThat(config.manufactoryYear).isEqualTo("2023")
        assertThat(config.manufactoryCompany).isEqualTo("VSETT / eMove Colombia")
        assertThat(config.boughtDate).isEqualTo("2023-10-10")
        assertThat(config.batteryTechnology).isEqualTo("Li-ion")
        assertThat(config.batteryVolts).isEqualTo("60V")
        assertThat(config.batteryAmpers).isEqualTo("20.8Ah")
        assertThat(config.brakeQuantity).isEqualTo(2)
        assertThat(config.brakeTechnology).isEqualTo("Hydraulic Disc Brake")
        assertThat(config.suspensionTechnology).isEqualTo("Spring & Hydraulic Suspension")
        assertThat(config.chargePower).isEqualTo("67.2V 2A")
        assertThat(config.otherCharacteristics).isEqualTo("Dual motor scooter")
        assertThat(config.isLoaded).isTrue()
    }

    @Test
    fun shouldHaveConstantClassName() {
        assertThat(EvConstants.EV_CONFIG_CLASS_NAME).isEqualTo(EvConfig::class.java.name)
    }
}
