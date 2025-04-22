package com.github.ttt374.healthcaretracer.ui.chart

import androidx.annotation.StringRes
import com.github.mikephil.charting.data.Entry
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.item.Vitals
import com.github.ttt374.healthcaretracer.data.repository.toTargetEntries
import com.github.ttt374.healthcaretracer.shared.TimeRange
import java.time.Instant

data class ChartData(val chartType: ChartType = ChartType.Default, val chartSeriesList: List<ChartSeries> = emptyList())
data class ChartSeries(val seriesDef: SeriesDef = SeriesDef.BpUpper, val actualEntries: List<Entry> = emptyList(), val targetEntries: List<Entry> = emptyList())

fun List<Entry>.firstDate(): Instant? {
    return this.minByOrNull { it.x }?.x?.toLong()?.toInstant()
}

fun List<ChartSeries>.firstDate(): Instant? {
    return this.flatMap { it.actualEntries }.firstDate()
}

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

    fun createSeries(entries: List<Entry>, targetValues: Vitals, timeRange: TimeRange
    ): ChartSeries {
        val targetValue = takeValue(targetValues)
        val targetEntries = targetValue?.let { entries.toTargetEntries(it, timeRange) }.orEmpty()
        return ChartSeries(this, entries, targetEntries)
    }

}

enum class ChartType(@StringRes val labelResId: Int, val seriesDefList: List<SeriesDef>){
    BloodPressure(R.string.blood_pressure, listOf(SeriesDef.BpUpper, SeriesDef.BpLower)),
    Pulse(R.string.pulse, listOf(SeriesDef.Pulse)),
    BodyTemperature(R.string.bodyTemperature, listOf(SeriesDef.BodyTemperature)),
    BodyWeight(R.string.bodyWeight, listOf(SeriesDef.BodyWeight));

    companion object {
        val Default = BloodPressure
    }
}

//sealed class ChartType(@StringRes val labelResId: Int, val seriesDefList: List<SeriesDef>){
//    data object BloodPressure : ChartType(R.string.blood_pressure,
//        listOf(SeriesDef.BpUpper, SeriesDef.BpLower))
//    data object Pulse: ChartType(R.string.pulse, listOf(SeriesDef.Pulse))
//    data object BodyTemperature: ChartType(R.string.bodyTemperature, listOf(SeriesDef.BodyTemperature))
//    data object BodyWeight: ChartType(R.string.bodyWeight, listOf(SeriesDef.BodyWeight))
//
//    companion object {
//        val Default = BloodPressure
//        val entries
//            get() = listOf(BloodPressure, Pulse, BodyTemperature, BodyWeight)
//    }
//}
