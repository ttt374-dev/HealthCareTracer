package com.github.ttt374.healthcaretracer.data.item

import java.time.LocalDate

data class DailyItem (
    val date: LocalDate,
    val vitals: Vitals = Vitals(),
    val items: List<Item> = emptyList(),
)
