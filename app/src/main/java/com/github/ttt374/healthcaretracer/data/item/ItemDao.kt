package com.github.ttt374.healthcaretracer.data.item

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface ItemDao {
    // CRUD
    @Insert
    suspend fun insertItem(item: Item): Long
    @Update
    suspend fun updateItem(item: Item)
    @Upsert
    suspend fun upsertItem(item: Item)
    @Delete
    suspend fun deleteItem(item: Item)

    // bulk
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<Item>)

    @Query("DELETE FROM items")
    suspend fun clearAll()

    // query
    @Query("SELECT * FROM items ORDER by measuredAt ASC")
    fun getAllItemsFlow(): Flow<List<Item>>

    @Query("SELECT * FROM items ORDER by measuredAt ASC")
    suspend fun getAllItems(): List<Item>

    @Query("SELECT * FROM items WHERE measuredAt > :threshold ORDER by measuredAt ASC")
    fun getItemsFromFlow(threshold: Instant): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE id = :id")
    fun getItemFlow(id: Long): Flow<Item?>

    @Query("SELECT DISTINCT location FROM items ORDER by location")
    fun getAllLocationsFlow(): Flow<List<String>>

    @Query("SELECT * FROM items ORDER BY measuredAt ASC LIMIT 1")
    suspend fun getFirstItem(): Item?

    //suspend fun getFirstDate(): Instant? = getFirstItem()?.measuredAt

}