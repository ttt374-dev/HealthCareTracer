package com.github.ttt374.healthcaretracer.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.bloodpressure.toBloodPressure
import com.github.ttt374.healthcaretracer.data.item.DailyItem
import com.github.ttt374.healthcaretracer.data.item.Item
import com.github.ttt374.healthcaretracer.data.repository.ItemRepository
import com.github.ttt374.healthcaretracer.ui.common.averageOrNull
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
    val dailyItems = itemRepository.getAllItemsFlow().map { items ->items.groupByDate() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

fun List<Item>.groupByDate(): List<DailyItem> {
    return groupBy { it.measuredAt.atZone(ZoneId.systemDefault()).toLocalDate() }
        .map { (date, dailyItems) ->
            DailyItem(
                date = date,
                bp = (dailyItems.map { it.bp?.upper }.averageOrNull() to dailyItems.map { it.bp?.lower }.averageOrNull())
                    .toBloodPressure(),
                pulse = dailyItems.map { it.pulse }.averageOrNull(),
                bodyWeight = dailyItems.map { it.bodyWeight }.averageOrNull(),
                bodyTemperature = dailyItems.map { it.bodyTemperature }.averageOrNull(),
                items = dailyItems
            )
        }.sortedBy { it.date }
}

