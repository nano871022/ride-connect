package co.japl.android.ev_ride_connect.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import co.japl.android.ev_ride_connect.database.entities.LlmConfigEntity

@Dao
interface LlmConfigDao {

    @Query("SELECT * FROM llm_configs ORDER BY id DESC")
    suspend fun getAllConfigs(): List<LlmConfigEntity>

    @Query("SELECT * FROM llm_configs WHERE is_active = 1 ORDER BY id DESC")
    suspend fun getActiveConfigs(): List<LlmConfigEntity>

    @Query("SELECT * FROM llm_configs WHERE id = :id LIMIT 1")
    suspend fun getConfigById(id: Long): LlmConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: LlmConfigEntity): Long

    @Update
    suspend fun updateConfig(config: LlmConfigEntity): Int

    @Query("UPDATE llm_configs SET is_active = :isActive, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateActiveStatus(id: Long, isActive: Boolean, updatedAt: String): Int

    @Query("DELETE FROM llm_configs WHERE id = :id")
    suspend fun deleteConfigById(id: Long): Int
}
