package com.github.ttt374.healthcaretracer.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    // CRUD
    @Insert
    suspend fun insertItem(item: Item): Long
    @Update
    suspend fun updateItem(item: Item)
    @Delete
    suspend fun deleteItem(item: Item)

    // query
    @Query("SELECT * FROM items")
    fun getAllItemsFlow(): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getItem(id: Long): Item?
}