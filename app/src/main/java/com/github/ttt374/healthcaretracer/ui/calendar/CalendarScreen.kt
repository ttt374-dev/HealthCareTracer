package com.github.ttt374.healthcaretracer.ui.calendar

import android.util.Log
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.github.ttt374.healthcaretracer.navigation.Screen
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.home.DailyItem
import com.github.ttt374.healthcaretracer.ui.home.DailyItemRow
import com.github.ttt374.healthcaretracer.ui.home.DailyItemsViewModel
import com.github.ttt374.healthcaretracer.ui.home.ItemRow
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(dailyItemsViewModel: DailyItemsViewModel = hiltViewModel(), navController: NavController){ // (chartViewModel: ChartViewModel = hiltViewModel(), navController: NavController) {
    //val datePickerState = rememberDatePickerState()
    val dailyItems by dailyItemsViewModel.dailyItems.collectAsState()
    var selectedDate by remember { mutableStateOf<LocalDate>(LocalDate.now()) }
    //var selectedItem by remember { mutableStateOf<DailyItem?>(null)}

    val navigateToEntry = {date: LocalDate ->
        navController.navigate("${Screen.Entry.route}/$selectedDate")
    }
    Scaffold(topBar = { CustomTopAppBar("Chart") },

        bottomBar = {
            CustomBottomAppBar(
                navController = navController,
                floatingActionButton = {
                    FloatingActionButton(onClick = { navigateToEntry(selectedDate) }){
                    //FloatingActionButton(onClick = { navController.navigate("${Screen.Entry.route}/${selectedDate.toString()}") }){
                        Icon(Icons.Filled.Add, "add")
                    }
                })
        }) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            LazyColumn {
                item {
                    //DatePicker(datePickerState)
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
                                onClick = { selectedDate = it.date; })
                        },
                         monthHeader = { month ->
                             val daysOfWeek = month.weekDays.first().map { it.date.dayOfWeek }
                             Text(month.yearMonth.toString())
                             DaysOfWeekTitle(daysOfWeek = daysOfWeek)
                         }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    val selectedItem =  dailyItems.find { item -> item.date == selectedDate }
                    selectedItem?.let { dailyItem ->
                        DailyItemRow(dailyItem, navigateToEdit = { navController.navigate("${Screen.Edit.route}/$it")})
                    }

//                    Button(onClick = {
//                        //val selectedDate = datePickerState.selectedDateMillis
//                        Log.d("SelectedDate", "選択した日付: $selectedDate")
//                    }) {
//                        Text("選択")
//                    }
                }
            }
        }
    }
}

@Composable
fun Day(day: CalendarDay, dailyItem: DailyItem? = null, isSelected: Boolean = false, onClick: (CalendarDay) -> Unit = {}) {
    Box(
        modifier = Modifier
            .aspectRatio(1f) // This is important for square sizing!
            .clip(CircleShape)
            .background(color = if (isSelected) Color.Green else Color.Transparent)
            .clickable(
                enabled = day.position == DayPosition.MonthDate,
                onClick = { onClick(day) }
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Column {
            Text(text = day.date.dayOfMonth.toString())
            if (dailyItem != null){
                Text("[${dailyItem.items.size}]")
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