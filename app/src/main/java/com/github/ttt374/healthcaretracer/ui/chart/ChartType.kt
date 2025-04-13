package com.github.ttt374.healthcaretracer.ui.chart

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.github.ttt374.healthcaretracer.R

data class ChartColorPalette(
    val primary: Color,
    val secondary: Color
)

enum class ChartType(@StringRes val labelResId: Int, val datasets: (Context, ChartUiState, ChartColorPalette) -> List<LineDataSet>) {
    BloodPressure(
        R.string.blood_pressure,
        { context, uiState, colors ->
            val primary = colors.primary
            val secondary = colors.secondary
            listOf(
                createLineDataSet(context, uiState.actualEntries.bpUpper, R.string.bpUpper, primary),
                createLineDataSet(context, uiState.actualEntries.bpLower, R.string.bpLower, secondary),
                createLineDataSet(context, uiState.targetEntries.bpUpper, R.string.bpUpper, primary, true),
                createLineDataSet(context, uiState.targetEntries.bpLower, R.string.bpLower, secondary, true),
            )
        }
    ),
    Pulse(
        R.string.pulse,
        { context, uiState, colors ->
            listOf(
                createLineDataSet(context, uiState.actualEntries.pulse, R.string.pulse, colors.primary)
            )
        }
    ),
    BodyWeight(
        R.string.body_weight,
        { context, uiState, colors ->
            listOf(
                createLineDataSet(context, uiState.actualEntries.bodyWeight, R.string.body_weight, colors.primary),
                createLineDataSet(context, uiState.targetEntries.bodyWeight, R.string.target_body_weight, colors.primary, true)
            )
        }
    ),
    BodyTemperature(
        R.string.bodyTemperature,
        { context, uiState, colors ->
            listOf(
                createLineDataSet(context, uiState.actualEntries.bodyTemperature, R.string.bodyTemperature, colors.primary),
                //createLineDataSet(context, uiState.targetEntries.bodyWeight, R.string.targetBodyTemperature, colors.primary, true)
            )
        }
    )
}

private fun createLineDataSet(context: Context, entries: List<Entry>, labelRes: Int, color: Color, isTarget: Boolean = false): LineDataSet {
    val label = context.getString(labelRes) // stringResource(labelRes)
    val dataSet = LineDataSet(entries, label)
    return dataSet.applyStyle(color.toArgb(), isTarget = isTarget)
}

private fun LineDataSet.applyStyle(color: Int, lineWidth: Float = 2f, circleRadius: Float = 4f, isTarget: Boolean = false) = apply {
    this.color = color
    setCircleColor(color)
    valueTextColor = color
    this.lineWidth = lineWidth
    this.circleRadius = circleRadius
    if (isTarget){
        enableDashedLine(15f, 10f, 0f)
        this.lineWidth = 1f
        setDrawValues(false)
        setDrawCircles(false)
    }
}
