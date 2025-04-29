package com.github.ttt374.healthcaretracer.ui.analysis

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressure
import com.github.ttt374.healthcaretracer.data.bloodpressure.toAnnotatedString
import com.github.ttt374.healthcaretracer.data.bloodpressure.toBloodPressure
import com.github.ttt374.healthcaretracer.data.metric.MetricBloodPressure
import com.github.ttt374.healthcaretracer.data.metric.MetricNumber
import com.github.ttt374.healthcaretracer.data.metric.MetricType
import com.github.ttt374.healthcaretracer.data.metric.MetricValue
import com.github.ttt374.healthcaretracer.data.metric.StatData
import com.github.ttt374.healthcaretracer.data.metric.StatValue
import com.github.ttt374.healthcaretracer.data.metric.toAnnotatedString
import com.github.ttt374.healthcaretracer.data.metric.toMetricNumber
import com.github.ttt374.healthcaretracer.data.metric.toMetricValue
import com.github.ttt374.healthcaretracer.shared.toAnnotatedString
import com.github.ttt374.healthcaretracer.shared.toDisplayString

//fun Any?.toAnnotatedString(format: String) : AnnotatedString = AnnotatedString("--fallback--")

enum class StatType (val resId: Int, val selector: (StatValue) -> MetricValue?, val format: ((MetricValue?) -> AnnotatedString)? = null){
    Average(R.string.average, { it.avg } ),
    Max(R.string.max, { it.max  }),
    Min(R.string.min, { it.min  }),
    Count(R.string.count, { it.count.toMetricValue()  });
}
//////////////////////////
@Composable
fun StatDataTable(metricType: MetricType, statData: StatData,
                  meGapStatValue: StatValue? = null,
                  bpToAnnotatedString: (BloodPressure?) -> AnnotatedString = { it.toAnnotatedString(showUnit = false) } ){
    CustomDivider()
    with(statData){
        StatValueHeadersRow(stringResource(metricType.resId))
        CustomDivider()
        val format = when (metricType){
            MetricType.BLOOD_PRESSURE -> { mv: MetricValue? ->
                when (mv){
                    is MetricNumber -> { mv.value.toAnnotatedString()}
                    is MetricBloodPressure -> { mv.value.toAnnotatedString(showUnit = false)}
                    null -> { AnnotatedString("-")}
                }
            }
            else -> metricType.format
        }
        StatValueRow(stringResource(R.string.all), all, format)
        byPeriod.forEach { (period, statValue) ->
            StatValueRow(stringResource(period.resId), statValue, format)
        }
        CustomDivider()
    }
//    when (metricType){
//        MetricType.BLOOD_PRESSURE -> {
//            MetricDefStatDataTable(statData)
//            //BloodPressureStatDataTable(statData, meGapStatValue, bpToAnnotatedString)
//            //meGapStatValue?.let { statValue -> StatValueRow(stringResource(R.string.me_gap), statValue, { it.toAnnotatedString("%.0f")}) }
//        }
//        else -> {
//            MetricDefStatDataTable(statData)
//        }
//    }
}
@Composable
fun BloodPressureStatDataTable(statDataList: List<StatData>, meGapStatValue: StatValue? = null,
                               format: (BloodPressure?) -> AnnotatedString = { it.toAnnotatedString(showUnit = false)} ){
    val (statUpperData, statLowerData) = statDataList.firstAndSecondOrNull()
    CustomDivider()
    if (statUpperData != null && statLowerData != null){
        StatValueHeadersRow(stringResource(R.string.blood_pressure))
        CustomDivider()
        StatValueBpRow(stringResource(R.string.all), statUpperData.all, statLowerData.all, format)
        statUpperData.byPeriod.forEach { (period, statUpper) ->
            statLowerData.byPeriod[period]?.let { statLower ->
                StatValueBpRow(stringResource(period.resId), statUpper, statLower, format)
            }
        }
        meGapStatValue?.let { statValue ->
            StatValueRow(stringResource(R.string.me_gap), statValue, { (it as MetricNumber).value.toAnnotatedString("%.0f")})  // TODO: cast check
        }
        CustomDivider()
    }

}
@Composable
fun StatValueBpRow(label: String, statUpper: StatValue, statLower: StatValue, format: (BloodPressure?) -> AnnotatedString ){
    Row {
        Text(label, Modifier.weight(1f))
        StatType.entries.forEach { statType ->
            //val bp = (statType.selector(statUpper)?.toInt() to statType.selector(statLower)?.toInt()).toBloodPressure()
            val bp = BloodPressure(0, 0) // TODO
            Box(contentAlignment = Alignment.Center, modifier=Modifier.weight(1f)){
                statType.format?.let { statFormat ->
                    Text(statFormat(statType.selector(statUpper)))
                } ?:  Text(format(bp))
                    //Text(format(bp))
            }
        }
//        Box(contentAlignment = Alignment.Center, modifier=Modifier.weight(1f)){
//            Text(statUpper.count.toString())
//        }
    }
}

@Composable
fun MetricDefStatDataTable(statData: StatData){
    CustomDivider()
    with(statData){
        StatValueHeadersRow(stringResource(metricType.resId))
        CustomDivider()
        StatValueRow(stringResource(R.string.all), all, metricType.format)
        byPeriod.forEach { (period, statValue) ->
            StatValueRow(stringResource(period.resId), statValue, metricType.format)
        }
        CustomDivider()
    }
}

@Composable
fun StatValueHeadersRow(label: String){
    Row {
        val mod = Modifier.weight(1f)
        //val modCount = Modifier.weight(0.7f)
        Text(label, mod, fontWeight = FontWeight.Bold)
        StatType.entries.forEach {
            Box(contentAlignment = Alignment.Center, modifier=Modifier.weight(1f)){
                Text(stringResource(it.resId))
            }
        }
        //Text(stringResource(R.string.count), Modifier.weight(0.7f))
    }
}
@Composable
fun StatValueRow(label: String, statValue: StatValue, format: (MetricValue?) -> AnnotatedString){
    Row {
        Box(contentAlignment = Alignment.CenterStart, modifier=Modifier.weight(1f)){
            Text(label)
        }
        StatType.entries.forEach { statType ->
            Box(contentAlignment = Alignment.Center, modifier=Modifier.weight(1f)){
                statType.format?.let { statFormat ->
                    Text(statFormat(statType.selector(statValue)))
                } ?:  Text(format(statType.selector(statValue))) // TODO
            }
        }
//        Box(contentAlignment = Alignment.Center, modifier=Modifier.weight(1f)){
//            Text(statValue.count.toDisplayString())
//        }
    }
}
@Composable
internal fun CustomDivider(thickness: Dp = 2.dp, color: Color = Color.Gray, paddingVertical: Dp = 4.dp){
    HorizontalDivider(thickness = thickness, color = color, modifier = Modifier.padding(vertical = paddingVertical))
}
fun <T> List<T>.firstAndSecondOrNull(): Pair<T?, T?> {
    return if (this.size > 1) {
        this[0] to this[1]  // 1番目と2番目の要素をペアで返す
    } else {
        null to null  // 要素が2つ未満の場合はnullを返す
    }
}
