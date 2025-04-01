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
    fun retrieveItemsFlow() = itemDao.getAllItemsFlow()
    fun getItemFlow(itemId: Long) = itemDao.getItemFlow(itemId)
    fun getAllLocationsFlow() = itemDao.getAllLocationsFlow()

    fun getAllItemsFlow() = itemDao.getAllItemsFlow()

    fun dailyItemsFlow(): Flow<List<DailyItem>> = itemDao.getAllItemsFlow().map { items ->
        items.groupBy { it.measuredAt.atZone(ZoneId.systemDefault()).toLocalDate() }
            .map { (date, dailyItems) ->
                DailyItem(
                    date = date,
                    avgBpUpper = dailyItems.map { it.bpUpper }.averageOrNull(),
                    avgBpLower = dailyItems.map { it.bpLower }.averageOrNull(),
                    avgPulse = dailyItems.map { it.pulse }.averageOrNull(),
                    avgBodyWeight = dailyItems.map { it.bodyWeight }.averageOrNull(),
                    items = dailyItems
                )
            }.sortedBy{ it.date }
    }
}
fun <T : Number> List<T?>.averageOrNull(): Double? {
    val filtered = this.filterNotNull()
    return if (filtered.isEmpty()) null else filtered.map { it.toDouble() }.average()
}

//fun List<BloodPressure>.average(): BloodPressure {
//    if (this.isEmpty()) return BloodPressure(0, 0) // 空リストならデフォルト値を返す
//
//    val systolicAvg = this.sumOf { it.systolic } / this.size
//    val diastolicAvg = this.sumOf { it.diastolic } / this.size
//
//    return BloodPressure(systolicAvg, diastolicAvg)
//}
//public fun Iterable<Byte>.average(): Double {
//    var sum: Double = 0.0
//    var count: Int = 0
//    for (element in this) {
//        sum += element
//        checkCountOverflow(++count)
//    }
//    return if (count == 0) Double.NaN else sum / count
//}