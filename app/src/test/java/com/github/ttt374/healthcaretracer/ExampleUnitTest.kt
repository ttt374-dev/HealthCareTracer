package com.github.ttt374.healthcaretracer

import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressureCategory
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressureGuideline
import com.github.ttt374.healthcaretracer.data.repository.LocalTimeRange
import com.github.ttt374.healthcaretracer.ui.common.TimeOfDay
import com.github.ttt374.healthcaretracer.ui.common.TimeOfDayConfig
import com.github.ttt374.healthcaretracer.ui.common.toTimeOfDay
import com.github.ttt374.healthcaretracer.ui.entry.toLocalTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        assertEquals("""{"guideline":{"type":"JSH"}}""", jsonString)

    }
}
class BloodPressureCategoryTest() {
    @Test
    fun categoryESCESHTest() {
        val guideline = BloodPressureGuideline.ESCESH
        assertEquals(guideline.getCategory(110, 65), guideline.normal)
        assertEquals(guideline.getCategory(145, 65), guideline.htn1)
        assertEquals(guideline.getCategory(200, 75), guideline.htn3)
        assertEquals(guideline.getCategory(null, null), BloodPressureCategory.Invalid)
        assertEquals(guideline.getCategory(165, true), guideline.htn2)
        assertEquals(guideline.getCategory(87, false), guideline.elevated)
    }
    @Test
    fun categoryAHACCTest() {
        val guideline = BloodPressureGuideline.AHAACC
        assertEquals(guideline.getCategory(115, 75), guideline.normal)
        assertEquals(guideline.getCategory(125, 75), guideline.elevated)
    }
}
class TimeOfDayTest {
    @Test
    fun timeOfDayTest(){
        val config = TimeOfDayConfig(LocalTime.of(6, 0), LocalTime.of(13, 0), LocalTime.of(18, 0))
        val zoneId = ZoneId.of("UTC")
        Instant.parse("2024-01-01T07:00:00Z").let {
            assertTrue(it.toTimeOfDay(zoneId, config) is TimeOfDay.Morning)
            assertFalse(it.toTimeOfDay(zoneId, config) is TimeOfDay.Afternoon)
            assertFalse(it.toTimeOfDay(zoneId, config) is TimeOfDay.Evening)
        }
        Instant.parse("2024-01-01T15:00:00Z").let {
            assertFalse(it.toTimeOfDay(zoneId, config) is TimeOfDay.Morning)
            assertTrue(it.toTimeOfDay(zoneId, config) is TimeOfDay.Afternoon)
            assertFalse(it.toTimeOfDay(zoneId, config) is TimeOfDay.Evening)
        }
        Instant.parse("2024-01-01T23:00:00Z").let {
            assertFalse(it.toTimeOfDay(zoneId, config) is TimeOfDay.Morning)
            assertFalse(it.toTimeOfDay(zoneId, config) is TimeOfDay.Afternoon)
            assertTrue(it.toTimeOfDay(zoneId, config) is TimeOfDay.Evening)
        }
    }
}
class MorningEveningTest {
    @Test
    fun morningEveningTest() {
//        val dateTime = ZonedDateTime.of(2024, 1, 1, 8, 0, 0, 0, ZoneId.systemDefault())
//        val instant = dateTime.toInstant()

        val morningTimeRange = LocalTimeRange(LocalTime.of(7, 0), LocalTime.of(12, 0))
        val eveningTimeRange = LocalTimeRange(LocalTime.of(18, 0), LocalTime.of(2, 0))
        val zoneId = ZoneId.of("UTC")

        Instant.parse("2024-01-01T08:00:00Z").let {
            assertTrue(morningTimeRange.contains(it.toLocalTime(zoneId)))
            assertFalse(eveningTimeRange.contains(it.toLocalTime(zoneId)))
//            assertTrue(it.isMorning(zoneId))
//            assertFalse(it.isEvening(zoneId))
        }
        Instant.parse("2024-01-01T02:00:00Z").let {
            assertFalse(morningTimeRange.contains(it.toLocalTime(zoneId)))
            assertTrue(eveningTimeRange.contains(it.toLocalTime(zoneId)))

        }
        Instant.parse("2024-01-01T15:00:00Z").let {
            assertFalse(morningTimeRange.contains(it.toLocalTime(zoneId)))
            assertFalse(eveningTimeRange.contains(it.toLocalTime(zoneId)))
        }
    }
}