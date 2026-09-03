package co.japl.android.ev_ride_connect.database

import co.japl.android.ev_ride_connect.core.domain.LlmConfig
import co.japl.android.ev_ride_connect.database.dao.LlmConfigDao
import co.japl.android.ev_ride_connect.database.entities.LlmConfigEntity
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import uk.co.jemos.podam.api.PodamFactoryImpl

class RoomLlmConfigAdapterTest {

    private val podamFactory = PodamFactoryImpl()
    private lateinit var fakeLlmConfigDao: FakeLlmConfigDao
    private lateinit var adapter: RoomLlmConfigAdapter

    @Before
    fun setUp() {
        fakeLlmConfigDao = FakeLlmConfigDao()
        adapter = RoomLlmConfigAdapter(fakeLlmConfigDao)
    }

    @Test
    fun shouldSaveConfigAndReturnGeneratedId() = runTest {
        val config = LlmConfig(
            modelName = "Gemini",
            selectedVersion = "gemini-1.5-flash",
            apiKey = "sample-key-123",
            isActive = true
        )

        val generatedId = adapter.saveConfig(config)

        assertThat(generatedId).isGreaterThan(0)
        assertThat(fakeLlmConfigDao.configs).hasSize(1)
        val savedEntity = fakeLlmConfigDao.configs.first()
        assertThat(savedEntity.modelName).isEqualTo("Gemini")
        assertThat(savedEntity.selectedVersion).isEqualTo("gemini-1.5-flash")
        assertThat(savedEntity.apiKey).isEqualTo("sample-key-123")
        assertThat(savedEntity.createdAt).isNotEmpty()
        assertThat(savedEntity.updatedAt).isNotEmpty()
    }

    @Test
    fun shouldGetAllConfigs() = runTest {
        val entity1 = LlmConfigEntity(1L, "Gemini", "gemini-1.5-flash", "key1", "2025-01-01", "2025-01-01", true)
        val entity2 = LlmConfigEntity(2L, "DeepSeek", "deepseek-chat", "key2", "2025-01-01", "2025-01-01", false)
        fakeLlmConfigDao.configs.addAll(listOf(entity1, entity2))

        val result = adapter.getAllConfigs()

        assertThat(result).hasSize(2)
        assertThat(result.map { it.modelName }).containsExactly("Gemini", "DeepSeek")
    }

    @Test
    fun shouldGetActiveConfigsOnly() = runTest {
        val entity1 = LlmConfigEntity(1L, "Gemini", "gemini-1.5-flash", "key1", "2025-01-01", "2025-01-01", true)
        val entity2 = LlmConfigEntity(2L, "DeepSeek", "deepseek-chat", "key2", "2025-01-01", "2025-01-01", false)
        fakeLlmConfigDao.configs.addAll(listOf(entity1, entity2))

        val result = adapter.getActiveConfigs()

        assertThat(result).hasSize(1)
        assertThat(result.first().modelName).isEqualTo("Gemini")
    }

    @Test
    fun shouldToggleActiveStatus() = runTest {
        val entity = LlmConfigEntity(1L, "ChatGPT", "gpt-4o", "key3", "2025-01-01", "2025-01-01", true)
        fakeLlmConfigDao.configs.add(entity)

        val updated = adapter.toggleActiveStatus(1L, false)

        assertThat(updated).isTrue()
        assertThat(fakeLlmConfigDao.configs.first().isActive).isFalse()
    }

    @Test
    fun shouldDeleteConfig() = runTest {
        val entity = LlmConfigEntity(1L, "ChatGPT", "gpt-4o", "key3", "2025-01-01", "2025-01-01", true)
        fakeLlmConfigDao.configs.add(entity)

        val deleted = adapter.deleteConfig(1L)

        assertThat(deleted).isTrue()
        assertThat(fakeLlmConfigDao.configs).isEmpty()
    }

    private class FakeLlmConfigDao : LlmConfigDao {
        val configs = mutableListOf<LlmConfigEntity>()
        private var autoId = 1L

        override suspend fun getAllConfigs(): List<LlmConfigEntity> {
            return configs.toList()
        }

        override suspend fun getActiveConfigs(): List<LlmConfigEntity> {
            return configs.filter { it.isActive }
        }

        override suspend fun getConfigById(id: Long): LlmConfigEntity? {
            return configs.find { it.id == id }
        }

        override suspend fun insertConfig(config: LlmConfigEntity): Long {
            val id = if (config.id == 0L) autoId++ else config.id
            val newConfig = config.copy(id = id)
            configs.removeIf { it.id == id }
            configs.add(newConfig)
            return id
        }

        override suspend fun updateConfig(config: LlmConfigEntity): Int {
            val index = configs.indexOfFirst { it.id == config.id }
            if (index >= 0) {
                configs[index] = config
                return 1
            }
            return 0
        }

        override suspend fun updateActiveStatus(id: Long, isActive: Boolean, updatedAt: String): Int {
            val index = configs.indexOfFirst { it.id == id }
            if (index >= 0) {
                val current = configs[index]
                configs[index] = current.copy(isActive = isActive, updatedAt = updatedAt)
                return 1
            }
            return 0
        }

        override suspend fun deleteConfigById(id: Long): Int {
            val removed = configs.removeIf { it.id == id }
            return if (removed) 1 else 0
        }
    }
}
