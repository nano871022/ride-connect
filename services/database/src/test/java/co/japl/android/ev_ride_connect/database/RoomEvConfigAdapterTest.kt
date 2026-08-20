package co.japl.android.ev_ride_connect.database

import co.japl.android.ev_ride_connect.core.domain.EvConfig
import co.japl.android.ev_ride_connect.core.domain.MotorSpec
import co.japl.android.ev_ride_connect.database.dao.EvConfigDao
import co.japl.android.ev_ride_connect.database.entities.EvConfigEntity
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import uk.co.jemos.podam.api.PodamFactoryImpl

class RoomEvConfigAdapterTest {

    private val podamFactory = PodamFactoryImpl()
    private lateinit var fakeEvConfigDao: FakeEvConfigDao
    private lateinit var adapter: RoomEvConfigAdapter

    @Before
    fun setUp() {
        fakeEvConfigDao = FakeEvConfigDao()
        adapter = RoomEvConfigAdapter(fakeEvConfigDao)
    }

    @Test
    fun shouldSaveEvConfigAndReturnGeneratedId() = runTest {
        val config = EvConfig(
            request = "Vsett c7 plus",
            brand = "VSETT",
            version = "C7 Plus",
            motors = listOf(MotorSpec("Front", 1000), MotorSpec("Rear", 1000)),
            manufactoryYear = "2023",
            batteryVolts = "60V",
            isLoaded = true
        )

        val id = adapter.saveEvConfig(config)

        assertThat(id).isGreaterThan(0)
        assertThat(fakeEvConfigDao.configs).hasSize(1)
        val savedEntity = fakeEvConfigDao.configs.first()
        assertThat(savedEntity.brand).isEqualTo("VSETT")
        assertThat(savedEntity.version).isEqualTo("C7 Plus")
        assertThat(savedEntity.motorsJson).contains("Front").contains("1000")
        assertThat(savedEntity.isLoaded).isTrue()
    }

    @Test
    fun shouldGetLatestEvConfig() = runTest {
        val entity = EvConfigEntity(
            id = 1L,
            request = "Vsett c7 plus",
            brand = "VSETT",
            version = "C7 Plus",
            motorsJson = "Front:1000",
            manufactoryYear = "2023",
            batteryVolts = "60V",
            isLoaded = true
        )
        fakeEvConfigDao.configs.add(entity)

        val retrieved = adapter.getEvConfig()

        assertThat(retrieved).isNotNull
        assertThat(retrieved?.brand).isEqualTo("VSETT")
        assertThat(retrieved?.version).isEqualTo("C7 Plus")
        assertThat(retrieved?.motors).hasSize(1)
        assertThat(retrieved?.motors?.first()?.name).isEqualTo("Front")
        assertThat(retrieved?.motors?.first()?.watts).isEqualTo(1000)
    }

    @Test
    fun shouldReturnNullWhenNoEvConfigInDatabase() = runTest {
        val retrieved = adapter.getEvConfig()
        assertThat(retrieved).isNull()
    }

    private class FakeEvConfigDao : EvConfigDao {
        val configs = mutableListOf<EvConfigEntity>()
        private var autoId = 1L

        override suspend fun getLatestEvConfig(): EvConfigEntity? {
            return configs.lastOrNull()
        }

        override suspend fun insertEvConfig(config: EvConfigEntity): Long {
            val id = if (config.id == 0L) autoId++ else config.id
            val newEntity = config.copy(id = id)
            configs.removeIf { it.id == id }
            configs.add(newEntity)
            return id
        }
    }
}
