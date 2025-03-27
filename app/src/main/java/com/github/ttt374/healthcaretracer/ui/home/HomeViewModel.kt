package com.github.ttt374.healthcaretracer.ui.home

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.Item
import com.github.ttt374.healthcaretracer.data.ItemRepository
import com.github.ttt374.healthcaretracer.usecase.ExportDataUseCase
import com.github.ttt374.healthcaretracer.usecase.ImportDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor (
    private val itemRepository: ItemRepository,
    private val exportDataUseCase: ExportDataUseCase,
    private val importDataUseCase: ImportDataUseCase,
    ) : ViewModel(){
    val dailyItems = itemRepository.dailyItemsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

//    val groupedItems = itemRepository.retrieveItemsFlow()
//        .map { items ->
//            items.groupBy { it.measuredAt.atZone(ZoneId.systemDefault()).toLocalDate() }
//                .map { (date, dailyItems) ->
//                    val avgBpHigh = dailyItems.map { it.bpHigh }.average().toInt()
//                    val avgBpLow = dailyItems.map { it.bpLow }.average().toInt()
//                    val avgPulse = dailyItems.map { it.pulse }.average().toInt()
//
//                    DailyItem(
//                        date = date,
//                        avgBpHigh = avgBpHigh,
//                        avgBpLow = avgBpLow,
//                        avgPulse = avgPulse,
//                        items = dailyItems
//                    )
//                }
//        }
//        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    fun exportData(){
        viewModelScope.launch {
            exportDataUseCase()
        }
    }
    fun importData(uri: Uri){
        viewModelScope.launch {
            importDataUseCase(uri)
        }
    }
}
data class DailyItem (
    val date: LocalDate,
    val avgBpHigh: Int,
    val avgBpLow: Int,
    val avgPulse: Int,
    val items: List<Item>
)