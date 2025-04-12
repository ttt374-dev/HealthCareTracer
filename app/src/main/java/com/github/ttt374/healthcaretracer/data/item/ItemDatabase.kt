package com.github.ttt374.healthcaretracer.data.item

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.time.Instant

/////////////////////////////
@Database(entities = [Item::class], version = 1, exportSchema = false)
@TypeConverters(InstantConverters::class)
abstract class ItemDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao

    companion object {
        @Volatile
        private var instant: ItemDatabase? = null

        fun getDatabase(context: Context, databaseName: String="items_database_07"): ItemDatabase {
            return instant ?: synchronized(this) {
                Room.databaseBuilder(context, ItemDatabase::class.java, databaseName)
                    .build()
                    .also { instant = it }
            }
        }
//        fun getDatabaseInMemory(context: Context): ItemDatabase {
//            return Room.inMemoryDatabaseBuilder(context, ItemDatabase::class.java).build()
//        }
    }
}

class InstantConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }
    @TypeConverter
    fun dateToTimestamp(instant: Instant?): Long? {
        return instant?.toEpochMilli()
    }
}
