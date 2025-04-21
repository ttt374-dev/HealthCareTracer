package com.github.ttt374.healthcaretracer.shared

import androidx.annotation.StringRes
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.repository.Preferences
import com.github.ttt374.healthcaretracer.data.repository.PreferencesRepository
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class TimeRangeManager @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    val getter: (Preferences) -> TimeRange,
    val updater: (Preferences, TimeRange) -> Preferences,
) {
    val timeRangeFlow: Flow<TimeRange> = preferencesRepository.dataFlow.map(getter)
    val timeRange: StateFlow<TimeRange> = timeRangeFlow
        .stateIn(
            CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
            SharingStarted.WhileSubscribed(5000),
            TimeRange.Default
        )

    suspend fun setSelectedRange(range: TimeRange) {
        preferencesRepository.updateData {
            updater(it, range)
            //it.copy(timeRangeStatistics = range)
        }
    }
}

enum class TimeRange(val days: Long?,  @StringRes val labelRes: Int) {
    ONE_WEEK(7, R.string.range__1week),
    ONE_MONTH(30, R.string.range__1month),
    SIX_MONTHS(180, R.string.range__6months),
    ONE_YEAR(365, R.string.range__1year),
    FULL(null, R.string.range__full_range);

    companion object {
        val Default = ONE_MONTH
    }
}
