package com.github.ttt374.healthcaretracer.data.repository

import androidx.room.Transaction
import com.github.ttt374.healthcaretracer.data.item.Item
import com.github.ttt374.healthcaretracer.data.item.ItemDao
import com.github.ttt374.healthcaretracer.data.item.Vitals
import com.github.ttt374.healthcaretracer.data.metric.MeasuredValue
import com.github.ttt374.healthcaretracer.data.metric.MetricType
import com.github.ttt374.healthcaretracer.data.metric.MetricValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
        if (item.vitals.bp == null) {
            throw IllegalArgumentException("bpUpper and bpLower must not be null")
        }
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
    //suspend fun getFirstDate(): Instant? = itemDao.getFirstDate()
    // measured value
    fun getMeasuredValuesFlow(metric: MetricType, days: Long? = null): Flow<List<MeasuredValue>> {
        return getRecentItemsFlow(days)
            .map { items ->
                items.toMeasuredValue(metric.selector)
//                items.mapNotNull { item ->
//                    metric.selector(item.vitals)?.let { value -> MeasuredValue(item.measuredAt, value) }
//                }
            }
    }
}
fun List<Item>.toMeasuredValue(selector: (Vitals) -> MetricValue?) = this.mapNotNull { item ->
    selector(item.vitals)?.let { MeasuredValue(item.measuredAt, it) }
}