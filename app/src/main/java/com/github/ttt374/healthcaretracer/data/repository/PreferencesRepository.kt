package com.github.ttt374.healthcaretracer.data.repository

import android.content.Context
import com.github.ttt374.healthcaretracer.ui.chart.ChartType
import com.github.ttt374.healthcaretracer.ui.common.TimeRange
import kotlinx.serialization.Serializable
import javax.inject.Singleton

@Serializable
data class Preferences(
    val timeRangeChart: TimeRange = TimeRange.Default,
    val timeRangeStatistics: TimeRange = TimeRange.Default,

    //val chartType: ChartType = ChartType.Default
)

@Singleton
class PreferencesRepository(context: Context) : DataStoreRepository<Preferences> by DataStoreRepositoryImpl (
    context = context,
    fileName = "preferences", // AppConst.DataStoreFilename.CONFIG.filename,
    serializer = GenericSerializer(serializer = Preferences.serializer(), default = Preferences())
)