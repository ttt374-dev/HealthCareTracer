package com.github.ttt374.healthcaretracer.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.bloodpressure.toBloodPressure
import com.github.ttt374.healthcaretracer.data.item.DailyItem
import com.github.ttt374.healthcaretracer.data.item.Item
import com.github.ttt374.healthcaretracer.data.item.Vitals
import com.github.ttt374.healthcaretracer.data.metric.averageOrNull
import com.github.ttt374.healthcaretracer.data.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class ItemsViewModel @Inject constructor (itemRepository: ItemRepository) : ViewModel(){
    val items = itemRepository.getAllItemsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val dailyItems = itemRepository.getAllItemsFlow().map { items ->items.toDailyItemList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

fun List<Item>.toDailyItemList(): List<DailyItem> {
    return groupBy { it.measuredAt.atZone(ZoneId.systemDefault()).toLocalDate() }
        .map { (date, dailyItems) ->
            DailyItem(
                date = date,
                vitals = dailyItems.toAveragedVitals(),
                items = dailyItems
            )
        }.sortedBy { it.date }
}

fun List<Item>.toAveragedVitals(): Vitals {
    return Vitals(
        bp = (mapNotNull { it.vitals.bp?.upper?.toDouble() }.averageOrNull() to mapNotNull { it.vitals.bp?.lower?.toDouble() }.averageOrNull())
            .toBloodPressure(),
        pulse = mapNotNull { it.vitals.pulse?.toDouble() }.averageOrNull()?.toInt(),
        bodyWeight = mapNotNull { it.vitals.bodyWeight }.averageOrNull(),
        bodyTemperature = mapNotNull { it.vitals.bodyTemperature }.averageOrNull(),
    )
}