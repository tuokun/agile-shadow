package io.github.cgfhsc.agileshadow.ime.data.clipboard

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity
data class ClipboardItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
)

@Dao
interface ClipboardDao {
    @Query("SELECT * FROM ClipboardItem ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ClipboardItem>>

    @Insert
    suspend fun insert(item: ClipboardItem)

    @Query("SELECT text FROM ClipboardItem ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestText(): String?

    @Query("DELETE FROM ClipboardItem WHERE id NOT IN (SELECT id FROM ClipboardItem ORDER BY timestamp DESC LIMIT 20)")
    suspend fun trimToLimit()

    @Query("DELETE FROM ClipboardItem WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("DELETE FROM ClipboardItem")
    suspend fun deleteAll()
}

@Database(entities = [ClipboardItem::class], version = 1)
abstract class ClipboardDatabase : RoomDatabase() {
    abstract fun clipboardDao(): ClipboardDao

    companion object {
        @Volatile
        private var INSTANCE: ClipboardDatabase? = null

        fun getInstance(context: Context): ClipboardDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ClipboardDatabase::class.java,
                    "clipboard.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
