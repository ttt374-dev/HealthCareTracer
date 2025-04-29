package com.github.ttt374.healthcaretracer.ui.analysis

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.metric.ChartSeries
import com.github.ttt374.healthcaretracer.data.metric.MetricType
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Long.toInstant(): Instant = Instant.ofEpochMilli(this)


////////////
internal fun Int.toChartType(): MetricType? {
    return MetricType.entries.getOrNull(this)
}

private fun LineChart.setupValueFormatter(datePattern: String){
    val formatter = DateTimeFormatter.ofPattern(datePattern)
    xAxis.valueFormatter = object : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return Instant.ofEpochMilli(value.toLong())
                .atZone(ZoneId.systemDefault()).toLocalDate().format(formatter)
        }
    }
}
fun LineChart.setupChartAdaptive(timeRange: TimeRange, datePattern: String = "yyyy/M/d", maxLabelCount: Int = 10) {
    description.isEnabled = false

    val dataStart = data?.xMin?.toLong() ?: return
    //val dataEnd = data?.xMax?.toLong() ?: return
    val end = Instant.now().toEpochMilli()

    val start = timeRange.startDate()?.toEpochMilli() ?: dataStart
    val totalDays = ((end - start) / 86400000L).coerceAtLeast(1)
    val daysPerLabel = (totalDays / 10).coerceAtLeast(1)
    val granularityMillis = daysPerLabel * 86400000f
    val labelRotationAngle = -45f

    xAxis.apply {
        position = XAxis.XAxisPosition.BOTTOM
        granularity = granularityMillis
        isGranularityEnabled = true
        setLabelCount(maxLabelCount, false)
        this.labelRotationAngle = labelRotationAngle
        setAvoidFirstLastClipping(true)
        axisMinimum = start.toFloat()
        axisMaximum = Instant.now().toEpochMilli().toFloat()
        // X軸のラベル間隔を調整
        setLabelCount(5, true) // ラベル数を減らしてみる
    }

    axisLeft.apply {
        spaceTop = 40f
        spaceBottom = 40f
    }
    axisRight.isEnabled = false

    // 凡例の位置を調整
    legend.apply {
        verticalAlignment = Legend.LegendVerticalAlignment.TOP
        horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        orientation = Legend.LegendOrientation.HORIZONTAL
    }

    setupValueFormatter(datePattern)
    invalidate()
}

fun LineDataSet.applyStyle(color: Int? = null, lineWidth: Float = 2f, circleRadius: Float = 4f, isTarget: Boolean = false) = apply {
    if (color != null){
        this.color = color
        setCircleColor(color)
        valueTextColor = color
    }
    this.circleRadius = circleRadius
    setDrawValues(false)

    if (isTarget) {
        enableDashedLine(15f, 10f, 0f)
        setDrawValues(false)
        setDrawCircles(false)
        this.lineWidth = 1f
    } else {
        this.lineWidth = lineWidth
        //this.circleRadius = circleRadius
    }
}
@Composable
fun HealthChart(chartSeriesList: List<ChartSeries>, timeRange: TimeRange) {
    val list = chartSeriesList.toLineDataSets()
    val isDarkMode = isSystemInDarkTheme()
    AndroidView(
        factory = { context -> LineChart(context) },
        modifier = Modifier.fillMaxSize(),
        update = { chart ->
            chart.data = LineData(*list.toTypedArray())
            //chart.data = LineData(*lineDataSetLit.toTypedArray())
            chart.apply {
                this.legend.textColor = Color.Black.adjustForMode(isDarkMode).toArgb()
            }
            chart.setupChartAdaptive(timeRange)
        }
    )
}

@Composable
private fun List<ChartSeries>.toLineDataSets(): List<LineDataSet> {
    val colorList = listOf(
        MaterialTheme.colorScheme.primary,
        //MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.outline,
        MaterialTheme.colorScheme.surfaceVariant
    )

    return this.withIndex().flatMap { (index, series) ->
        val label = stringResource(series.resId)
        val targetLabel = (stringResource(R.string.target) + ":" + stringResource(series.resId))
        val color = colorList.getOrElse(index % colorList.size) { MaterialTheme.colorScheme.primary }

        listOfNotNull(
            LineDataSet(series.actualEntries, label).applyStyle(color.toArgb()),
            LineDataSet(series.targetEntries, targetLabel).applyStyle(color.toArgb(), isTarget = true)
        )
    }
}
fun Color.adjustForMode(isDarkMode: Boolean = false): Color {
    return if (isDarkMode) this.toDarkMode() else this
}
fun Color.toDarkMode(): Color {
    val r = 1f - red
    val g = 1f - green
    val b = 1f - blue
    return Color(r, g, b, alpha * 0.5f)
}
