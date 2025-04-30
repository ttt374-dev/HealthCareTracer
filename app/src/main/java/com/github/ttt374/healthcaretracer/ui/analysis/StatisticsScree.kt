package com.github.ttt374.healthcaretracer.ui.analysis

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.metric.MetricType
import com.github.ttt374.healthcaretracer.data.metric.MetricValue
import com.github.ttt374.healthcaretracer.data.metric.StatData
import com.github.ttt374.healthcaretracer.data.metric.StatType
import com.github.ttt374.healthcaretracer.data.metric.StatValue
import com.github.ttt374.healthcaretracer.data.metric.get
import com.github.ttt374.healthcaretracer.shared.toAnnotatedString

typealias MetricValueFormatter = (MetricValue) -> AnnotatedString

//////////////////////////
@Composable
fun StatDataTable(metricType: MetricType, statData: StatData<MetricValue>,
                  meGapStatValue: StatValue<MetricValue>? = null,
                  format: MetricValueFormatter? = null){
    CustomDivider()
    with(statData){
        StatValueHeadersRow(stringResource(metricType.resId))
        CustomDivider()

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
fun StatValueBaseRow(label: String, values: List<AnnotatedString>){
    Row (modifier=Modifier.fillMaxWidth().padding(horizontal = 4.dp)){
        Text(label, modifier=Modifier.weight(1f))
        values.forEach { value ->
            Text(value, textAlign = TextAlign.Center, modifier=Modifier.weight(1f) )
        }
    }
}
@Composable
fun StatValueHeadersRow(label: String){
    StatValueBaseRow(label, StatType.entries.map { stringResource(it.resId).toAnnotatedString() } )
}
@Composable
fun StatValueRow(label: String, statValue: StatValue<MetricValue>, format: MetricValueFormatter? = null){
    StatValueBaseRow(label, StatType.entries.map { statType ->
        statValue.formatMetricValue(statType, format)
    })
}

@Composable
internal fun CustomDivider(thickness: Dp = 2.dp, color: Color = Color.Gray, paddingVertical: Dp = 4.dp){
    HorizontalDivider(thickness = thickness, color = color, modifier = Modifier.padding(vertical = paddingVertical))
}
fun StatValue<MetricValue>.formatMetricValue(statType: StatType, format: MetricValueFormatter? = null): AnnotatedString {
    val metricValue = get(statType)
    return if (metricValue != null) {
        format?.invoke(metricValue) ?: metricValue.format()
    } else {
        AnnotatedString("-")
    }
}