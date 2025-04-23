package com.github.ttt374.healthcaretracer

import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressure
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressureGuideline
import com.github.ttt374.healthcaretracer.shared.DayPeriod
import com.github.ttt374.healthcaretracer.shared.TimeOfDayConfig
import com.github.ttt374.healthcaretracer.shared.TimeRange
import com.github.ttt374.healthcaretracer.shared.toDayPeriod
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

@Serializable
data class ConfigWrapper(val guideline: BloodPressureGuideline)

class SerializeTest(){

    private val json = Json { encodeDefaults = true }

    @Test
    fun guidelineSerializeTest(){
        val config = ConfigWrapper(BloodPressureGuideline.JSH2019)


        val jsonString = json.encodeToString(config)
        //Log.d("test", jsonString)
        //
        //assertEquals("""{"guideline":["JSH2019"]}""", jsonString)
        assert(jsonString.contains("JSH2019"))

    }
}
class BloodPressureCategoryTest() {
    @Test
    fun categoryESCESHTest() {
        val guideline = BloodPressureGuideline.ESCESH
        assertEquals(guideline.getCategory(BloodPressure(110, 65)), guideline.normal)
        assertEquals(guideline.getCategory(BloodPressure(145, 65)), guideline.htn1)
        assertEquals(guideline.getCategory(BloodPressure(200, 75)), guideline.htn3)
//        assertEquals(guideline.getCategory(BloodPressure(null, null)), BloodPressureCategory.Invalid)
//        assertEquals(guideline.getCategory(BloodPressure(165, true)), guideline.htn2)
//        assertEquals(guideline.getCategory(BloodPressure(87, false)), guideline.elevated)
    }
    @Test
    fun categoryAHACCTest() {
        val guideline = BloodPressureGuideline.AHAACC
        assertEquals(guideline.getCategory(BloodPressure(115, 75)), guideline.normal)
        assertEquals(guideline.getCategory(BloodPressure(125, 75)), guideline.elevated)
    }
}
class DayPeriodTest {
    @Test
    fun timeOfDayTest(){
        val timeOfDayConfig = TimeOfDayConfig(
            mapOf(DayPeriod.Morning to LocalTime.of(6, 0),
                DayPeriod.Afternoon to LocalTime.of(13, 0),
                DayPeriod.Evening to LocalTime.of(18, 0))
        )
        val zoneId = ZoneId.of("UTC")
        Instant.parse("2024-01-01T07:00:00Z").let {
            assertTrue(it.toDayPeriod(timeOfDayConfig, zoneId) == DayPeriod.Morning)
            assertFalse(it.toDayPeriod(timeOfDayConfig, zoneId) == DayPeriod.Afternoon)
            assertFalse(it.toDayPeriod(timeOfDayConfig, zoneId) == DayPeriod.Evening)
        }
        Instant.parse("2024-01-01T15:00:00Z").let {
            assertFalse(it.toDayPeriod(timeOfDayConfig, zoneId) == DayPeriod.Morning)
            assertTrue(it.toDayPeriod(timeOfDayConfig, zoneId) == DayPeriod.Afternoon)
            assertFalse(it.toDayPeriod(timeOfDayConfig, zoneId) == DayPeriod.Evening)
        }
        Instant.parse("2024-01-01T23:00:00Z").let {
            assertFalse(it.toDayPeriod(timeOfDayConfig, zoneId) == DayPeriod.Morning)
            assertFalse(it.toDayPeriod(timeOfDayConfig, zoneId) == DayPeriod.Afternoon)
            assertTrue(it.toDayPeriod(timeOfDayConfig, zoneId) == DayPeriod.Evening)
        }
    }
}
class MorningEveningTest {
    @Test
    fun morningEveningTest() {
        val timeOfDayConfig = TimeOfDayConfig(
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
            assertEquals(DayPeriod.Morning, it.toDayPeriod(timeOfDayConfig, zoneId))
            assertNotEquals(DayPeriod.Evening, it.toDayPeriod(timeOfDayConfig, zoneId))
//            assertTrue(it.isMorning(zoneId))
//            assertFalse(it.isEvening(zoneId))
        }
        Instant.parse("2024-01-01T02:00:00Z").let {
            assertNotEquals(DayPeriod.Morning, it.toDayPeriod(timeOfDayConfig, zoneId))
            assertEquals(DayPeriod.Evening, it.toDayPeriod(timeOfDayConfig, zoneId))

//            assertFalse(morningTimeRange.contains(it.toLocalTime(zoneId)))
//            assertTrue(eveningTimeRange.contains(it.toLocalTime(zoneId)))

        }
        Instant.parse("2024-01-01T15:00:00Z").let {
            assertNotEquals(DayPeriod.Morning, it.toDayPeriod(timeOfDayConfig, zoneId))
            assertNotEquals(DayPeriod.Evening, it.toDayPeriod(timeOfDayConfig, zoneId))

//            assertFalse(morningTimeRange.contains(it.toLocalTime(zoneId)))
//            assertFalse(eveningTimeRange.contains(it.toLocalTime(zoneId)))
        }
    }
}