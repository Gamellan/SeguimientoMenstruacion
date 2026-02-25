package com.seguimiento.menstruacion.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "period_records")
data class PeriodRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startDate: String,
    val endDate: String?,
    val isOngoing: Boolean,
    val flowLevel: String,
    val symptoms: String,
    val painLevel: Int,
    val notes: String
)

@Dao
interface PeriodRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: PeriodRecordEntity)

    @Update
    suspend fun update(record: PeriodRecordEntity)

    @Query("DELETE FROM period_records WHERE id = :recordId")
    suspend fun deleteById(recordId: Long)

    @Query("SELECT * FROM period_records ORDER BY startDate DESC")
    fun observeAll(): Flow<List<PeriodRecordEntity>>
}

@Database(entities = [PeriodRecordEntity::class], version = 3, exportSchema = false)
abstract class PeriodDatabase : RoomDatabase() {
    abstract fun periodRecordDao(): PeriodRecordDao

    companion object {
        @Volatile
        private var INSTANCE: PeriodDatabase? = null

        fun getDatabase(context: Context): PeriodDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PeriodDatabase::class.java,
                    "period_tracker_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
