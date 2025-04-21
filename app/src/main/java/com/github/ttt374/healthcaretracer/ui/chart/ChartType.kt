package com.github.ttt374.healthcaretracer.ui.chart

import androidx.annotation.StringRes
import com.github.mikephil.charting.data.Entry
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.item.Vitals

data class ChartData(val chartType: ChartType = ChartType.Default, val chartSeriesList: List<ChartSeries> = emptyList())
data class ChartSeries(val seriesDef: SeriesDef = SeriesDef.BpUpper, val actualEntries: List<Entry> = emptyList(), val targetEntries: List<Entry> = emptyList())

//enum class SeriesPriority { Primary, Secondary }
//
//data class ColorPalette (val primary: Color = Color.Unspecified, val secondary: Color)

//data class ChartableItem (
//    val bpUpper: Double? = null,
//    val bpLower: Double? = null,
//    val pulse: Double? = null,
//    val bodyTemperature: Double? = null,
//    val bodyWeight: Double? = null,
//)

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

//@Serializable(with = ChartTypeSerializer::class)
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
//    val SeriesDef.hasTarget: Boolean
//        get() = targetLabelResId != null
}

//
//object ChartTypeSerializer : KSerializer<ChartType> {
//    override val descriptor: SerialDescriptor =
//        PrimitiveSerialDescriptor("ChartType", PrimitiveKind.STRING)
//
//    override fun serialize(encoder: Encoder, value: ChartType) {
//        encoder.encodeString(
//            when (value) {
//                ChartType.BloodPressure -> "BloodPressure"
//                ChartType.Pulse -> "Pulse"
//                ChartType.BodyTemperature -> "BodyTemperature"
//                ChartType.BodyWeight -> "BodyWeight"
//            }
//        )
//    }
//
//    override fun deserialize(decoder: Decoder): ChartType {
//        return when (val name = decoder.decodeString()) {
//            "BloodPressure" -> ChartType.BloodPressure
//            "Pulse" -> ChartType.Pulse
//            "BodyTemperature" -> ChartType.BodyTemperature
//            "BodyWeight" -> ChartType.BodyWeight
//            else -> error("Unknown ChartType: $name")
//        }
//    }
//}


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
