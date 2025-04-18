package com.github.ttt374.healthcaretracer.data.repository

import androidx.room.Transaction
import com.github.ttt374.healthcaretracer.data.item.Item
import com.github.ttt374.healthcaretracer.data.item.ItemDao
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemRepository @Inject constructor(private val itemDao: ItemDao) {
    // crud
//    suspend fun insertItem(item: Item){
//        itemDao.insertItem(item)
//    }
    suspend fun upsertItem(item: Item){
        itemDao.upsertItem(item)
    }
    suspend fun deleteItem(item: Item){
        itemDao.deleteItem(item)
    }
    @Transaction
    suspend fun replaceAllItems(items: List<Item>) {
        itemDao.clearAll()
        itemDao.insertItems(items)
    }
    // query
    //fun retrieveItemsFlow() = itemDao.getAllItemsFlow()
    fun getItemFlow(itemId: Long) = itemDao.getItemFlow(itemId)
    fun getAllLocationsFlow() = itemDao.getAllLocationsFlow()

    fun getAllItemsFlow() = itemDao.getAllItemsFlow()
    suspend fun getAllItems() = itemDao.getAllItems()
    fun getRecentItemsFlow(days: Long?): Flow<List<Item>> {
        return when (days){
            null -> itemDao.getAllItemsFlow()
            else -> itemDao.getItemsFromFlow(Instant.now().minus(days, ChronoUnit.DAYS))
        }
    }
}
fun <T : Number> List<T?>.averageOrNull(): Double? {
    val filtered = this.filterNotNull()
    return if (filtered.isEmpty()) null else filtered.map { it.toDouble() }.average()
}
