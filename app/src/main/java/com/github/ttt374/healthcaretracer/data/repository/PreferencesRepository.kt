package com.github.ttt374.healthcaretracer.data.repository

import android.content.Context
import com.github.ttt374.healthcaretracer.data.metric.MetricType
import com.github.ttt374.healthcaretracer.ui.analysis.DisplayMode
import com.github.ttt374.healthcaretracer.ui.analysis.TimeRange
import kotlinx.serialization.Serializable
import javax.inject.Singleton

@Serializable
data class Preferences(
    val timeRange: TimeRange = TimeRange.Default,
    val displayMode: DisplayMode = DisplayMode.Default,
    val selectedMetricType: MetricType = MetricType.Default,
)

@Singleton
class PreferencesRepository(context: Context) : DataStoreRepository<Preferences> by DataStoreRepositoryImpl (
    context = context,
    fileName = "preferences", // AppConst.DataStoreFilename.CONFIG.filename,
    serializer = GenericSerializer(serializer = Preferences.serializer(), default = Preferences())
)

