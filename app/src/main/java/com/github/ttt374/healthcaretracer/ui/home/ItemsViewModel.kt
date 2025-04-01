package com.github.ttt374.healthcaretracer.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.Item
import com.github.ttt374.healthcaretracer.data.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ItemsViewModel @Inject constructor (itemRepository: ItemRepository) : ViewModel(){
    val dailyItems = itemRepository.dailyItemsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val items = itemRepository.getAllItemsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

}

data class DailyItem (
    val date: LocalDate,
    //val avgBp: BloodPressure,
    val avgBpUpper: Double? = null,
    val avgBpLower: Double? = null,
    val avgPulse: Double? = null,
    val avgBodyWeight: Double? = null,
    val items: List<Item> = emptyList(),
)