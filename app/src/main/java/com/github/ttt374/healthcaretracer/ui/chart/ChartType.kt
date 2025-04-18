package com.github.ttt374.healthcaretracer.ui.chart

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.ChartRepository

//data class ChartColorPalette(
//    val primary: Color,
//    val secondary: Color
//)

data class ChartEntries(
    val bpUpper: List<Entry> =  emptyList(),
    val bpLower: List<Entry> = emptyList(),
    val pulse: List<Entry> = emptyList(),
    val bodyWeight: List<Entry> = emptyList(),
    val bodyTemperature: List<Entry> = emptyList(),
)
data class ChartData(val chartType: ChartType = ChartType.Default, val chartSeriesList: List<ChartSeries> = emptyList())
data class ChartSeries(val seriesDef: SeriesDef = SeriesDef.BpUpper, val actualEntries: List<Entry> = emptyList(), val targetEntries: List<Entry> = emptyList())

enum class SeriesPriority { Primary, Secondary }

data class ChartableItem (
    val bpUpper: Double? = null,
    val bpLower: Double? = null,
    val pulse: Double? = null,
    val bodyTemperature: Double? = null,
    val bodyWeight: Double? = null,
)

sealed class SeriesDef(val takeValue: (ChartEntries) -> List<Entry>,
                       @StringRes val labelResId: Int?, @StringRes val targetLabelResId: Int? = null,
                       val seriesPriority: SeriesPriority = SeriesPriority.Primary,
                       val getTargetValue: ((ChartableItem) -> Number?)? = null){
    data object BpUpper: SeriesDef({ it.bpUpper }, R.string.bpUpper, R.string.targetBpUpper, SeriesPriority.Primary, getTargetValue = { it.bpUpper })
    data object BpLower: SeriesDef({ it.bpLower }, R.string.bpLower, R.string.targetBpLower, SeriesPriority.Secondary, getTargetValue = { it.bpLower })
    data object Pulse: SeriesDef({ it.pulse }, R.string.pulse, null)
    data object BodyTemperature: SeriesDef({ it.bodyTemperature }, R.string.bodyTemperature, null)
    data object BodyWeight: SeriesDef({ it.bodyWeight }, R.string.bodyWeight, R.string.targetBodyWeight, getTargetValue = { it.bodyWeight })

    fun createTargetEntries(targetItem: ChartableItem, actualEntries: ChartEntries): List<Entry> {
        val targetValue = getTargetValue?.invoke(targetItem)?.toFloat() ?: return emptyList()
        val entries = takeValue(actualEntries)

        if (entries.isEmpty()) return emptyList()

        val startX = entries.first().x
        val endX = entries.last().x

        return listOf(
            Entry(startX, targetValue),
            Entry(endX, targetValue)
        )
    }
    fun createChartSeries(entries: ChartEntries, targetValue: ChartableItem) =
          ChartSeries(
            seriesDef = this,
            actualEntries = takeValue(entries),
            targetEntries = createTargetEntries(targetValue, entries)
        )
}

sealed class ChartType(@StringRes val labelResId: Int, private val seriesDefList: List<SeriesDef>){
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
    val SeriesDef.hasTarget: Boolean
        get() = targetLabelResId != null


    fun toChartSeriesList(entries: ChartEntries, targetValue: ChartableItem): List<ChartSeries> {
        return seriesDefList.map { def -> def.createChartSeries(entries, targetValue)
//            ChartSeries(
//                seriesDef = def,
//                actualEntries = def.takeValue(entries),
//                targetEntries = def.createTargetEntries(targetValue, entries)
                //targetEntries = def.takeValue(entries.target)
            //)
        }
    }
}

fun LineDataSet.applyStyle(color: Int, lineWidth: Float = 2f, circleRadius: Float = 4f, isTarget: Boolean = false) = apply {
    this.color = color
    setCircleColor(color)
    valueTextColor = color
    this.circleRadius = circleRadius

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


//
//sealed class ChartSeries {
//    abstract val actual: List<Entry>
//    open val target: List<Entry> = emptyList()
//
//    data class BpUpper(
//        override val actual: List<Entry>,
//        override val target: List<Entry>
//    ) : ChartSeries()
//
//    data class BpLower(
//        override val actual: List<Entry>,
//        override val target: List<Entry>
//    ) : ChartSeries()
//
//    data class Pulse(
//        override val actual: List<Entry>
//    ) : ChartSeries()
//
//    data class BodyWeight(
//        override val actual: List<Entry>,
//        override val target: List<Entry>
//    ) : ChartSeries()
//
//    data class BodyTemperature(
//        override val actual: List<Entry>
//    ) : ChartSeries()
//}
//data class ChartGroup(
//    val series: List<ChartSeries>
//)


//enum class ChartType(@StringRes val labelResId: Int) {
//    BloodPressure(R.string.blood_pressure,),
//    Pulse(R.string.pulse,),
//    BodyWeight(R.string.bodyWeight,),
//    BodyTemperature(R.string.bodyTemperature,)
//}
//data class ChartSection(
//    val type: ChartType,
//    val datasets: List<LineDataSet>
//)

//
//private fun createLineDataSet(context: Context, entries: List<Entry>, labelRes: Int, color: Color, isTarget: Boolean = false): LineDataSet {
//    val label = context.getString(labelRes) // stringResource(labelRes)
//    val dataSet = LineDataSet(entries, label)
//    return dataSet.applyStyle(color.toArgb(), isTarget = isTarget)
//}
//

//
//enum class ChartType(@StringRes val labelResId: Int, val datasets: (Context, ChartUiState, ChartColorPalette) -> List<LineDataSet>) {
//    BloodPressure(
//        R.string.blood_pressure,
//        { context, uiState, colors ->
//            val primary = colors.primary
//            val secondary = colors.secondary
//            listOf(
//                createLineDataSet(context, uiState.actualEntries.bpUpper, R.string.bpUpper, primary),
//                createLineDataSet(context, uiState.actualEntries.bpLower, R.string.bpLower, secondary),
//                createLineDataSet(context, uiState.targetEntries.bpUpper, R.string.targetBpUpper, primary, true),
//                createLineDataSet(context, uiState.targetEntries.bpLower, R.string.targetBpLower, secondary, true),
//            )
//        }
//    ),
//    Pulse(
//        R.string.pulse,
//        { context, uiState, colors ->
//            listOf(
//                createLineDataSet(context, uiState.actualEntries.pulse, R.string.pulse, colors.primary)
//            )
//        }
//    ),
//    BodyWeight(
//        R.string.bodyWeight,
//        { context, uiState, colors ->
//            listOf(
//                createLineDataSet(context, uiState.actualEntries.bodyWeight, R.string.bodyWeight, colors.primary),
//                createLineDataSet(context, uiState.targetEntries.bodyWeight, R.string.targetBodyWeight, colors.primary, true)
//            )
//        }
//    ),
//    BodyTemperature(
//        R.string.bodyTemperature,
//        { context, uiState, colors ->
//            listOf(
//                createLineDataSet(context, uiState.actualEntries.bodyTemperature, R.string.bodyTemperature, colors.primary),
//                //createLineDataSet(context, uiState.targetEntries.bodyWeight, R.string.targetBodyTemperature, colors.primary, true)
//            )
//        }
//    )
//}
//
//fun createLineDataSet(context: Context, entries: List<Entry>, labelRes: Int, color: Color, isTarget: Boolean = false): LineDataSet {
//    val label = context.getString(labelRes) // stringResource(labelRes)
//    val dataSet = LineDataSet(entries, label)
//    return dataSet.applyStyle(color.toArgb(), isTarget = isTarget)
//}

//fun LineDataSet.applyStyle(color: Int, lineWidth: Float = 2f, circleRadius: Float = 4f, isTarget: Boolean = false) = apply {
//    this.color = color
//    setCircleColor(color)
//    valueTextColor = color
//    this.lineWidth = lineWidth
//    this.circleRadius = circleRadius
//    if (isTarget){
//        enableDashedLine(15f, 10f, 0f)
//        this.lineWidth = 1f
//        setDrawValues(false)
//        setDrawCircles(false)
//    }
//}
