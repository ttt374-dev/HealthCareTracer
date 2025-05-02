package com.github.ttt374.healthcaretracer.data.repository

import androidx.room.Transaction
import com.github.ttt374.healthcaretracer.data.item.DailyItem
import com.github.ttt374.healthcaretracer.data.item.Item
import com.github.ttt374.healthcaretracer.data.item.ItemDao
import com.github.ttt374.healthcaretracer.data.item.Vitals
import com.github.ttt374.healthcaretracer.data.item.toDailyItemList
import com.github.ttt374.healthcaretracer.data.metric.MeasuredValue
import com.github.ttt374.healthcaretracer.data.metric.MetricType
import com.github.ttt374.healthcaretracer.data.metric.MetricValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

interface ItemRepository {
    // CRUD操作
    suspend fun upsertItem(item: Item)
    suspend fun deleteItem(item: Item)

    // 一括操作
    suspend fun replaceAllItems(items: List<Item>)

    // クエリ操作
    fun getItemFlow(itemId: Long): Flow<Item?>
    fun getAllLocationsFlow(): Flow<List<String>>
    fun getAllItemsFlow(): Flow<List<Item>>
    fun getAllDailyItemsFlow(zoneId: ZoneId): Flow<List<DailyItem>>
    suspend fun getAllItems(): List<Item>
    fun getRecentItemsFlow(days: Long?): Flow<List<Item>>

    // 測定値の取得
    fun getMeasuredValuesFlow(metric: MetricType, days: Long? = null): Flow<List<MeasuredValue<MetricValue>>>
}

@Singleton
class ItemRepositoryImpl @Inject constructor(private val itemDao: ItemDao) : ItemRepository {
    override suspend fun upsertItem(item: Item) {
        if (item.vitals.bp == null) {
            throw IllegalArgumentException("bpUpper and bpLower must not be null")
        }
        itemDao.upsertItem(item)
    }

    override suspend fun deleteItem(item: Item) {
        itemDao.deleteItem(item)
    }

    override suspend fun replaceAllItems(items: List<Item>) {
        itemDao.clearAll()
        itemDao.insertItems(items)
    }

    override fun getItemFlow(itemId: Long) = itemDao.getItemFlow(itemId)

    override fun getAllLocationsFlow() = itemDao.getAllLocationsFlow()

    override fun getAllItemsFlow() = itemDao.getAllItemsFlow()
    override fun getAllDailyItemsFlow(zoneId: ZoneId) = getAllItemsFlow().map { it.toDailyItemList(zoneId) }

    override suspend fun getAllItems() = itemDao.getAllItems()

    override fun getRecentItemsFlow(days: Long?): Flow<List<Item>> {
        return when (days) {
            null -> itemDao.getAllItemsFlow()
            else -> itemDao.getItemsFromFlow(Instant.now().minus(days, ChronoUnit.DAYS))
        }
    }

    override fun getMeasuredValuesFlow(metric: MetricType, days: Long?): Flow<List<MeasuredValue<MetricValue>>> {
        return getRecentItemsFlow(days).map { items ->
            items.toMeasuredValue(metric.selector)
        }
    }
}

fun List<Item>.toMeasuredValue(selector: (Vitals) -> MetricValue?) = this.mapNotNull { item ->
    selector(item.vitals)?.let { MeasuredValue(item.measuredAt, it) }
}