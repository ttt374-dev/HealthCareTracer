package com.github.ttt374.healthcaretracer.data

import androidx.room.Transaction
import com.github.ttt374.healthcaretracer.ui.home.DailyItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemRepository @Inject constructor(private val itemDao: ItemDao) {
    // crud
    suspend fun insertItem(item: Item){
        itemDao.insertItem(item)
    }
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
    fun retrieveItemsFlow() = itemDao.getAllItemsFlow()
    fun getItemFlow(itemId: Long) = itemDao.getItemFlow(itemId)
    fun getAllLocationsFlow() = itemDao.getAllLocationsFlow()

    fun dailyItemsFlow(): Flow<List<DailyItem>> = itemDao.getAllItemsFlow().map { items ->
        items.sortedBy{ it.measuredAt }.groupBy { it.measuredAt.atZone(ZoneId.systemDefault()).toLocalDate() }
            .map { (date, dailyItems) ->
                val avgBpHigh = dailyItems.map { it.bpHigh }.average().toInt()
                val avgBpLow = dailyItems.map { it.bpLow }.average().toInt()
                val avgPulse = dailyItems.map { it.pulse }.average().toInt()
                val avgBodyWeight = dailyItems.map { it.bodyWeight }.average().toFloat()

                DailyItem(
                    date = date,
                    avgBpHigh = avgBpHigh,
                    avgBpLow = avgBpLow,
                    avgPulse = avgPulse,
                    avgBodyWeight = avgBodyWeight,
                    items = dailyItems
                )
            }
    }
}