package com.github.ttt374.healthcaretracer.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressureGuideline
import com.github.ttt374.healthcaretracer.data.bloodpressure.toAnnotatedString
import com.github.ttt374.healthcaretracer.data.item.DailyItem
import com.github.ttt374.healthcaretracer.data.item.Item
import com.github.ttt374.healthcaretracer.data.metric.DayPeriod
import com.github.ttt374.healthcaretracer.data.metric.DayPeriodConfig
import com.github.ttt374.healthcaretracer.data.metric.toDayPeriod
import com.github.ttt374.healthcaretracer.shared.toBodyTemperatureString
import com.github.ttt374.healthcaretracer.shared.toBodyWeightString
import com.github.ttt374.healthcaretracer.shared.toPulseString
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun DailyItemRow(dailyItem: DailyItem, guideline: BloodPressureGuideline = BloodPressureGuideline.Default,
                 dayPeriodConfig: DayPeriodConfig = DayPeriodConfig(),
                 navigateToEdit: (Long) -> Unit = {}){
    Row (modifier= Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically){
        Text(DateTimeFormatter.ofPattern("yyyy-M-d (E) ").format(dailyItem.date), modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
        Text(dailyItem.vitals.bp.toAnnotatedString(guideline = guideline), fontWeight = FontWeight.Bold)
        Text(dailyItem.vitals.pulse.toPulseString(), textAlign = TextAlign.End)
    }
    dailyItem.items.forEach { item ->
        ItemRow(item, guideline, dayPeriodConfig, navigateToEdit, )
    }
}
@Composable
fun ItemRow(item: Item, guideline: BloodPressureGuideline = BloodPressureGuideline.Default,
            dayPeriodConfig: DayPeriodConfig = DayPeriodConfig(),
            navigateToEdit: (Long) -> Unit = {}){
    val dateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())
    val bp = item.vitals.bp

    Column (modifier= Modifier.padding(horizontal = 8.dp, vertical = 4.dp).fillMaxWidth().clickable { navigateToEdit(item.id) }) {
        Row {
            Text(dateTimeFormatter.format(item.measuredAt), fontSize = 14.sp)
            when (item.measuredAt.toDayPeriod(dayPeriodConfig = dayPeriodConfig)){
                DayPeriod.Morning -> Icon(Icons.Filled.WbSunny, "morning", modifier = Modifier.size(12.dp))
                DayPeriod.Evening -> Icon(Icons.Filled.DarkMode, "evening", modifier = Modifier.size(12.dp))
                else -> {}
            }
//            item.measuredAt.toLocalTime().let {
//                when {
//                    morningTimeRange.contains(it) -> Icon(Icons.Filled.WbSunny, "morning", modifier = Modifier.size(12.dp))
//                    eveningTimeRange.contains(it) -> Icon(Icons.Filled.DarkMode, "evening", modifier = Modifier.size(12.dp))
//                    else -> Text("")
//                }
//            }

            Spacer(modifier = Modifier.width(16.dp))
            Text(bp.toAnnotatedString(guideline = guideline))
            Text(item.vitals.pulse.toPulseString())
            Spacer(modifier = Modifier.weight(1f)) // 左右の間に余白を作る
            Text(item.vitals.bodyWeight.toBodyWeightString())
            Text(item.vitals.bodyTemperature.toBodyTemperatureString())
        }

        Row {
            if (item.vitals.bp == null){
                Text("-")
            } else {
                guideline.getCategory(bp).let {
                    Text(stringResource(it.nameLabel), color=it.color)
                    //Text(it.name, color=it.color)
                }
            }
            Spacer(modifier = Modifier.weight(1f)) // 左右の間に余白を作る
            Text(item.location, textAlign = TextAlign.Right)
        }
        Row {
            item.memo.takeIf { it.isNotBlank() }?.let { memoText ->
                Text(text = "memo: $memoText", fontSize = 12.sp)
            }
        }
    }
    HorizontalDivider(thickness = 0.75.dp, color = Color.LightGray)
}
