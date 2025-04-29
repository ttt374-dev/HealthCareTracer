package com.github.ttt374.healthcaretracer.ui.analysis

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressure
import com.github.ttt374.healthcaretracer.data.bloodpressure.toAnnotatedString
import com.github.ttt374.healthcaretracer.data.metric.MetricType
import com.github.ttt374.healthcaretracer.data.metric.MetricValue
import com.github.ttt374.healthcaretracer.data.metric.StatData
import com.github.ttt374.healthcaretracer.data.metric.StatType
import com.github.ttt374.healthcaretracer.data.metric.StatValue
import com.github.ttt374.healthcaretracer.data.metric.get


//////////////////////////
@Composable
fun StatDataTable(metricType: MetricType, statData: StatData<MetricValue>,
                  meGapStatValue: StatValue<MetricValue>? = null,
                  formatBloodPressure: (BloodPressure?) -> AnnotatedString = { it.toAnnotatedString(showUnit = false) } ){
    CustomDivider()
    with(statData){
        StatValueHeadersRow(stringResource(metricType.resId))
        CustomDivider()

        val format = { mv: MetricValue ->
            when(mv){
                is MetricValue.BloodPressure -> { formatBloodPressure(mv.value)}
                else -> { mv.format() }
            }
        }
        StatValueRow(stringResource(R.string.all), all, format)
        byPeriod.forEach { (period, statValue) ->
            StatValueRow(stringResource(period.resId), statValue, format)
        }
        if (metricType == MetricType.BLOOD_PRESSURE){
            meGapStatValue?.let { StatValueRow(stringResource(R.string.me_gap), it, format)}
        }
        CustomDivider()
    }
}

@Composable
fun StatValueHeadersRow(label: String){
    Row(Modifier.fillMaxWidth()) {
        val mod = Modifier.weight(1f)
        //val modCount = Modifier.weight(0.7f)
        Text(label, mod, fontWeight = FontWeight.Bold)
        StatType.entries.forEach {
            Box(contentAlignment = Alignment.Center, modifier=Modifier.weight(1f)){
                Text(stringResource(it.resId))
            }
        }
    }
}
@Composable
fun StatValueRow(label: String, statValue: StatValue<MetricValue>, format: ((MetricValue) -> AnnotatedString)? = null){
    Row(Modifier.fillMaxWidth()) {
        Text(label, textAlign = TextAlign.Center, modifier=Modifier.weight(1f))

        StatType.entries.forEach { statType ->
            Text(statValue.formatMetricValue(statType, format), textAlign = TextAlign.Center, modifier=Modifier.weight(1f))
        }
    }
}
fun StatValue<MetricValue>.formatMetricValue(statType: StatType, format: ((MetricValue) -> AnnotatedString)? = null): AnnotatedString {
    val metricValue = get(statType)
    return if (metricValue != null) {
        format?.invoke(metricValue) ?: metricValue.format()
    } else {
        AnnotatedString("-")
    }
}


@Composable
internal fun CustomDivider(thickness: Dp = 2.dp, color: Color = Color.Gray, paddingVertical: Dp = 4.dp){
    HorizontalDivider(thickness = thickness, color = color, modifier = Modifier.padding(vertical = paddingVertical))
}

//@Composable
//fun BloodPressureStatDataTable(statDataList: List<StatData>, meGapStatValue: StatValue? = null,
//                               format: (BloodPressure?) -> AnnotatedString = { it.toAnnotatedString(showUnit = false)} ){
//    val (statUpperData, statLowerData) = statDataList.firstAndSecondOrNull()
//    CustomDivider()
//    if (statUpperData != null && statLowerData != null){
//        StatValueHeadersRow(stringResource(R.string.blood_pressure))
//        CustomDivider()
//        StatValueBpRow(stringResource(R.string.all), statUpperData.all, statLowerData.all, format)
//        statUpperData.byPeriod.forEach { (period, statUpper) ->
//            statLowerData.byPeriod[period]?.let { statLower ->
//                StatValueBpRow(stringResource(period.resId), statUpper, statLower, format)
//            }
//        }
//        meGapStatValue?.let { statValue ->
//            StatValueRow(stringResource(R.string.me_gap), statValue, { (it as MetricNumber).value.toAnnotatedString("%.0f")})  // TODO: cast check
//        }
//        CustomDivider()
//    }
//
//}
//@Composable
//fun StatValueBpRow(label: String, statUpper: StatValue, statLower: StatValue, format: (BloodPressure?) -> AnnotatedString ){
//    Row {
//        Text(label, Modifier.weight(1f))
//        StatType.entries.forEach { statType ->
//            //val bp = (statType.selector(statUpper)?.toInt() to statType.selector(statLower)?.toInt()).toBloodPressure()
//            val bp = BloodPressure(0, 0) // TODO
//            Box(contentAlignment = Alignment.Center, modifier=Modifier.weight(1f)){
//                statType.format?.let { statFormat ->
//                    Text(statFormat(statType.selector(statUpper)))
//                } ?:  Text(format(bp))
//                    //Text(format(bp))
//            }
//        }
////        Box(contentAlignment = Alignment.Center, modifier=Modifier.weight(1f)){
////            Text(statUpper.count.toString())
////        }
//    }
//}
//


//@Composable
//fun MetricDefStatDataTable(statData: StatData){
//    CustomDivider()
//    with(statData){
//        StatValueHeadersRow(stringResource(metricType.resId))
//        CustomDivider()
//        StatValueRow(stringResource(R.string.all), all, metricType.format)
//        byPeriod.forEach { (period, statValue) ->
//            StatValueRow(stringResource(period.resId), statValue, metricType.format)
//        }
//        CustomDivider()
//    }
//}