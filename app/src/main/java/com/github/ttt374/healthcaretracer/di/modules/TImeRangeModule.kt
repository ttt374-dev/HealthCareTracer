package com.github.ttt374.healthcaretracer.di.modules

import com.github.ttt374.healthcaretracer.data.repository.PreferencesRepository
import com.github.ttt374.healthcaretracer.data.repository.TimeRangeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ChartTimeRange

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class StatisticsTimeRange

@Module
@InstallIn(SingletonComponent::class)
object TimeRangeModule {
    @Provides
    @StatisticsTimeRange
    fun provideStatisticsTimeRangeManager(
        preferencesRepository: PreferencesRepository
    ): TimeRangeRepository {
        return TimeRangeRepository(
            preferencesRepository,
            getter = { it.timeRangeStatistics },
            updater = { prefs, range -> prefs.copy(timeRangeStatistics = range) }
        )
    }

    @Provides
    @ChartTimeRange
    fun provideChartTimeRangeManager(
        preferencesRepository: PreferencesRepository
    ): TimeRangeRepository {
        return TimeRangeRepository(
            preferencesRepository,
            getter = { it.timeRangeChart },
            updater = { prefs, range -> prefs.copy(timeRangeChart = range) }
        )
    }
}
