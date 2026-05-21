package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "mantras")
data class MantraEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String = "Sugerido"
)

@Entity(tableName = "scenarios")
data class OasisScenarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val metaphor: String,
    val prompt: String,
    val color1Hex: String,
    val color2Hex: String,
    val isCustom: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val modeName: String,
    val durationSeconds: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface OasisDao {
    // Mantras
    @Query("SELECT * FROM mantras ORDER BY timestamp DESC")
    fun getAllMantras(): Flow<List<MantraEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMantra(mantra: MantraEntity)

    @Delete
    suspend fun deleteMantra(mantra: MantraEntity)

    // Scenarios
    @Query("SELECT * FROM scenarios ORDER BY timestamp DESC")
    fun getAllScenarios(): Flow<List<OasisScenarioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScenario(scenario: OasisScenarioEntity)

    @Delete
    suspend fun deleteScenario(scenario: OasisScenarioEntity)

    // Sessions
    @Query("SELECT * FROM sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Query("DELETE FROM sessions")
    suspend fun clearSessions()
}

@Database(entities = [MantraEntity::class, OasisScenarioEntity::class, SessionEntity::class], version = 1, exportSchema = false)
abstract class OasisDatabase : RoomDatabase() {
    abstract fun oasisDao(): OasisDao

    companion object {
        @Volatile
        private var INSTANCE: OasisDatabase? = null

        fun getDatabase(context: Context): OasisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OasisDatabase::class.java,
                    "oasis_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
