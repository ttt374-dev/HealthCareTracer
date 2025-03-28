package com.github.ttt374.healthcaretracer.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.Item
import com.github.ttt374.healthcaretracer.data.ItemRepository
import com.github.ttt374.healthcaretracer.usecase.ExportDataUseCase
import com.github.ttt374.healthcaretracer.usecase.ImportDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DailyItemsViewModel @Inject constructor (
    itemRepository: ItemRepository) : ViewModel(){
        val dailyItems = itemRepository.dailyItemsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

data class DailyItem (
    val date: LocalDate,
    val avgBpHigh: Int,
    val avgBpLow: Int,
    val avgPulse: Int,
    val avgBodyWeight: Float,
    val items: List<Item>
)