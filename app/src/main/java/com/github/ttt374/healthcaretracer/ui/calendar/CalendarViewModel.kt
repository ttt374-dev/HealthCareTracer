package com.github.ttt374.healthcaretracer.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.item.toDailyItemList
import com.github.ttt374.healthcaretracer.data.repository.Config
import com.github.ttt374.healthcaretracer.data.repository.ConfigRepository
import com.github.ttt374.healthcaretracer.data.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(itemRepository: ItemRepository, configRepository: ConfigRepository): ViewModel(){
    val config = configRepository.dataFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Config())
//    val items = itemRepository.getAllItemsFlow()
//        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val dailyItems = itemRepository.getAllItemsFlow().map { items ->items.toDailyItemList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

}