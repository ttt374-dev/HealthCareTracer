package com.github.ttt374.healthcaretracer.ui.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ChartViewModel @Inject constructor (private val itemRepository: ItemRepository) : ViewModel(){
//    val items = itemRepository.retrieveItemsFlow()
//        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyItems = itemRepository.dailyItemsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

}