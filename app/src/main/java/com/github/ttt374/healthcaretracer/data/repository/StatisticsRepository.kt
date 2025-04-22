package com.github.ttt374.healthcaretracer.data.repository

import com.github.ttt374.healthcaretracer.shared.TimeRange
import com.github.ttt374.healthcaretracer.ui.statics.StatCalculator
import com.github.ttt374.healthcaretracer.ui.statics.StatisticsData
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class StatisticsRepository @Inject constructor(private val itemRepository: ItemRepository, configRepository: ConfigRepository) {
    private val timeOfDayConfigFlow = configRepository.dataFlow.map { it.timeOfDayConfig }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getStatisticsFlow(timeRange: TimeRange): Flow<StatisticsData> =
        timeOfDayConfigFlow.flatMapLatest { timeOfDayConfig ->
            itemRepository.getRecentItemsFlow(timeRange.days).map { items ->
                StatCalculator.calculateAll(items, timeOfDayConfig)
            }
        }.flowOn(Dispatchers.Default)
}
