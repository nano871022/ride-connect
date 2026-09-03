package co.japl.android.ev_ride_connect.database

import co.japl.android.ev_ride_connect.core.domain.EvData
import co.japl.android.ev_ride_connect.database.dao.EvDataDao
import co.japl.android.ev_ride_connect.database.entities.EvDataEntity
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class RoomEvDataAdapterTest {

    private lateinit var fakeEvDataDao: FakeEvDataDao
    private lateinit var adapter: RoomEvDataAdapter

    @Before
    fun setUp() {
        fakeEvDataDao = FakeEvDataDao()
        adapter = RoomEvDataAdapter(fakeEvDataDao)
    }

    @Test
    fun shouldSaveEvData() = runTest {
        val evData = EvData(evCode = "EV01", km = 150L, batteryLevel = 85)
        adapter.saveEvData(evData)

        assertThat(fakeEvDataDao.dataList).hasSize(1)
        val savedEntity = fakeEvDataDao.dataList.first()
        assertThat(savedEntity.evCode).isEqualTo("EV01")
        assertThat(savedEntity.km).isEqualTo(150L)
        assertThat(savedEntity.batteryLevel).isEqualTo(85.toShort())
    }

    @Test
    fun shouldGetLatestEvData() = runTest {
        val entity1 = EvDataEntity(evCode = "EV01", km = 100L, batteryLevel = 90, createTmst = 1000L)
        val entity2 = EvDataEntity(evCode = "EV01", km = 120L, batteryLevel = 80, createTmst = 2000L)
        fakeEvDataDao.dataList.add(entity1)
        fakeEvDataDao.dataList.add(entity2)

        val latest = adapter.getLatestEvData()

        assertThat(latest).isNotNull
        assertThat(latest?.km).isEqualTo(120L)
        assertThat(latest?.batteryLevel).isEqualTo(80.toShort())
    }

    @Test
    fun shouldGetAllEvData() = runTest {
        val entity1 = EvDataEntity(evCode = "EV01", km = 100L, batteryLevel = 90, createTmst = 1000L)
        val entity2 = EvDataEntity(evCode = "EV01", km = 120L, batteryLevel = 80, createTmst = 2000L)
        fakeEvDataDao.dataList.add(entity1)
        fakeEvDataDao.dataList.add(entity2)

        val list = adapter.getAllEvData()

        assertThat(list).hasSize(2)
    }

    private class FakeEvDataDao : EvDataDao {
        val dataList = mutableListOf<EvDataEntity>()

        override suspend fun getLatestEvData(): EvDataEntity? {
            return dataList.maxByOrNull { it.createTmst }
        }

        override suspend fun getAllEvData(): List<EvDataEntity> {
            return dataList.sortedByDescending { it.createTmst }
        }

        override suspend fun insertEvData(evData: EvDataEntity): Long {
            dataList.add(evData)
            return dataList.size.toLong()
        }
    }
}
