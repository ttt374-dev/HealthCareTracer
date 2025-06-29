package com.github.ttt374.healthcaretracer.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressure
import com.github.ttt374.healthcaretracer.data.bloodpressure.toAnnotatedString
import com.github.ttt374.healthcaretracer.data.item.DailyItem
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.home.DailyItemRow
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(calendarViewModel: CalendarViewModel = hiltViewModel(),
                   appNavigator: AppNavigator){
    val dailyItems by calendarViewModel.dailyItems.collectAsState()
    var selectedDate by remember { mutableStateOf<LocalDate>(LocalDate.now()) }
    val config by calendarViewModel.config.collectAsState()

    Scaffold(topBar = { CustomTopAppBar(stringResource(R.string.calendar)) },
        bottomBar = {
            CustomBottomAppBar(
                appNavigator = appNavigator,
                floatingActionButton = {
                    FloatingActionButton(onClick = { appNavigator.navigateToEntry(selectedDate) }){
                    //FloatingActionButton(onClick = { navController.navigate("${Screen.Entry.route}/${selectedDate.toString()}") }){
                        Icon(Icons.Filled.Add, "add")
                    }
                })
        }) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)){
            LazyColumn {
                item {
                    val currentMonth = remember { YearMonth.now() }
                    val startMonth = remember { currentMonth.minusMonths(100) } // Adjust as needed
                    val endMonth = remember { currentMonth.plusMonths(100) } // Adjust as needed
                    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() } // Available from the library

                    val state = rememberCalendarState(
                        startMonth = startMonth,
                        endMonth = endMonth,
                        firstVisibleMonth = currentMonth,
                        firstDayOfWeek = firstDayOfWeek
                    )
                    HorizontalCalendar(
                        state = state,
                        dayContent = { cday ->
                            val dailyItem = dailyItems.find { item -> item.date == cday.date }
                            Day(cday, dailyItem,
                                isSelected = selectedDate == cday.date,
                                onClick = { selectedDate = it.date; },
                                formatBloodPressure = { it.toAnnotatedString(config.bloodPressureGuideline, false)}
                                )
                        },
                         monthHeader = { month ->
                             val daysOfWeek = month.weekDays.first().map { it.date.dayOfWeek }
                             val formatter = DateTimeFormatter.ofPattern("yyyy-M")
                             Text(month.yearMonth.format(formatter), Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally))
                             DaysOfWeekTitle(daysOfWeek = daysOfWeek)
                         }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    val selectedItem =  dailyItems.find { item -> item.date == selectedDate }
                    selectedItem?.let { dailyItem ->
                        DailyItemRow(dailyItem,  navigateToEdit = appNavigator::navigateToEdit, zoneId = config.zoneId)
                    }
                }
            }
        }
    }
}

@Composable
fun Day(day: CalendarDay, dailyItem: DailyItem? = null, isSelected: Boolean = false, onClick: (CalendarDay) -> Unit = {},
        formatBloodPressure: (bp: BloodPressure) -> AnnotatedString) {
    val highlightColor = MaterialTheme.colorScheme.primaryContainer
    Box(modifier = Modifier
            .aspectRatio(.8f) // This is important for square sizing!
            .clip(CircleShape)
            .background(color = if (isSelected) highlightColor else Color.Transparent)
            .clickable(
                onClick = { onClick(day) }
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = day.date.dayOfMonth.toString())
            dailyItem?.vitals?.bp?.let { bp ->
                Text(formatBloodPressure(bp), fontSize = 10.sp)
                //Text(bp.toAnnotatedString(showUnit = false), fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun DaysOfWeekTitle(daysOfWeek: List<DayOfWeek>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            )
        }
    }
}