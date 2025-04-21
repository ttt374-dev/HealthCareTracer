package com.github.ttt374.healthcaretracer.ui.chart

import androidx.annotation.StringRes
import com.github.mikephil.charting.data.Entry
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.item.Vitals

data class ChartData(val chartType: ChartType = ChartType.Default, val chartSeriesList: List<ChartSeries> = emptyList())
data class ChartSeries(val seriesDef: SeriesDef = SeriesDef.BpUpper, val actualEntries: List<Entry> = emptyList(), val targetEntries: List<Entry> = emptyList())

sealed class SeriesDef(
    @StringRes val labelResId: Int?,
    @StringRes val targetLabelResId: Int? = null,
    //val color: Color = Color.Unspecified,
    val takeValue: (Vitals) -> Double?
){
    data object BpUpper: SeriesDef(R.string.bpUpper, R.string.targetBpUpper,  { it.bp?.upper?.toDouble()} )
    data object BpLower: SeriesDef(R.string.bpLower, R.string.targetBpLower,  { it.bp?.lower?.toDouble()} )
    data object Pulse: SeriesDef(R.string.pulse, null,  { it.pulse })
    data object BodyTemperature: SeriesDef(R.string.bodyTemperature, null, takeValue = { it.bodyTemperature })
    data object BodyWeight: SeriesDef(R.string.bodyWeight, R.string.targetBodyWeight,  takeValue = { it.bodyWeight } )

    fun createTargetEntries(targetValues: Vitals, entries: List<Entry>): List<Entry> {
        val targetValue = takeValue.invoke(targetValues)?.toFloat() ?: return emptyList()
        if (entries.isEmpty()) return emptyList()

        val startX = entries.first().x
        val endX = entries.last().x

        return listOf(
            Entry(startX, targetValue),
            Entry(endX, targetValue)
        )
    }
}

sealed class ChartType(@StringRes val labelResId: Int, val seriesDefList: List<SeriesDef>){
    data object BloodPressure : ChartType(R.string.blood_pressure,
        listOf(SeriesDef.BpUpper, SeriesDef.BpLower))
    data object Pulse: ChartType(R.string.pulse, listOf(SeriesDef.Pulse))
    data object BodyTemperature: ChartType(R.string.bodyTemperature, listOf(SeriesDef.BodyTemperature))
    data object BodyWeight: ChartType(R.string.bodyWeight, listOf(SeriesDef.BodyWeight))

    companion object {
        val Default = BloodPressure
        val entries
            get() = listOf(BloodPressure, Pulse, BodyTemperature, BodyWeight)
    }
}
