package com.github.ttt374.healthcaretracer

import com.github.ttt374.healthcaretracer.data.metric.DayPeriod
import com.github.ttt374.healthcaretracer.data.metric.DayPeriodConfig
import com.github.ttt374.healthcaretracer.data.metric.toDayPeriod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

class DayPeriodTest {
    @Test
    fun timeOfDayTest(){
        val dayPeriodCOnfig = DayPeriodConfig(
            mapOf(
                DayPeriod.Morning to LocalTime.of(6, 0),
                DayPeriod.Afternoon to LocalTime.of(13, 0),
                DayPeriod.Evening to LocalTime.of(18, 0))
        )
        val zoneId = ZoneId.of("UTC")
        Instant.parse("2024-01-01T07:00:00Z").let {
            assertTrue(it.toDayPeriod(dayPeriodCOnfig, zoneId) == DayPeriod.Morning)
            assertFalse(it.toDayPeriod(dayPeriodCOnfig, zoneId) == DayPeriod.Afternoon)
            assertFalse(it.toDayPeriod(dayPeriodCOnfig, zoneId) == DayPeriod.Evening)
        }
        Instant.parse("2024-01-01T15:00:00Z").let {
            assertFalse(it.toDayPeriod(dayPeriodCOnfig, zoneId) == DayPeriod.Morning)
            assertTrue(it.toDayPeriod(dayPeriodCOnfig, zoneId) == DayPeriod.Afternoon)
            assertFalse(it.toDayPeriod(dayPeriodCOnfig, zoneId) == DayPeriod.Evening)
        }
        Instant.parse("2024-01-01T23:00:00Z").let {
            assertFalse(it.toDayPeriod(dayPeriodCOnfig, zoneId) == DayPeriod.Morning)
            assertFalse(it.toDayPeriod(dayPeriodCOnfig, zoneId) == DayPeriod.Afternoon)
            assertTrue(it.toDayPeriod(dayPeriodCOnfig, zoneId) == DayPeriod.Evening)
        }
    }
}
class MorningEveningTest {
    @Test
    fun morningEveningTest() {
        val dayPeriodCOnfig = DayPeriodConfig(
            mapOf(
                DayPeriod.Morning to LocalTime.of(5, 0),
                DayPeriod.Afternoon to LocalTime.of(12, 0),
                DayPeriod.Evening to LocalTime.of(18, 0),
            )
        )
//        val dateTime = ZonedDateTime.of(2024, 1, 1, 8, 0, 0, 0, ZoneId.systemDefault())
//        val instant = dateTime.toInstant()

//        val morningTimeRange = LocalTimeRange(LocalTime.of(7, 0), LocalTime.of(12, 0))
//        val eveningTimeRange = LocalTimeRange(LocalTime.of(18, 0), LocalTime.of(2, 0))
        val zoneId = ZoneId.of("UTC")
//
        Instant.parse("2024-01-01T08:00:00Z").let {
            assertEquals(DayPeriod.Morning, it.toDayPeriod(dayPeriodCOnfig, zoneId))
            assertNotEquals(DayPeriod.Evening, it.toDayPeriod(dayPeriodCOnfig, zoneId))
//            assertTrue(it.isMorning(zoneId))
//            assertFalse(it.isEvening(zoneId))
        }
        Instant.parse("2024-01-01T02:00:00Z").let {
            assertNotEquals(DayPeriod.Morning, it.toDayPeriod(dayPeriodCOnfig, zoneId))
            assertEquals(DayPeriod.Evening, it.toDayPeriod(dayPeriodCOnfig, zoneId))

//            assertFalse(morningTimeRange.contains(it.toLocalTime(zoneId)))
//            assertTrue(eveningTimeRange.contains(it.toLocalTime(zoneId)))

        }
        Instant.parse("2024-01-01T15:00:00Z").let {
            assertNotEquals(DayPeriod.Morning, it.toDayPeriod(dayPeriodCOnfig, zoneId))
            assertNotEquals(DayPeriod.Evening, it.toDayPeriod(dayPeriodCOnfig, zoneId))

//            assertFalse(morningTimeRange.contains(it.toLocalTime(zoneId)))
//            assertFalse(eveningTimeRange.contains(it.toLocalTime(zoneId)))
        }
    }
}