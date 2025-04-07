package com.github.ttt374.healthcaretracer

import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressureGuideline
import com.github.ttt374.healthcaretracer.data.datastore.LocalTimeRange
import com.github.ttt374.healthcaretracer.ui.entry.toLocalTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.LocalTime

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
        val config = ConfigWrapper(BloodPressureGuideline.JSH)


        val jsonString = json.encodeToString(config)
        //Log.d("test", jsonString)
        assertEquals("""{"guideline":{"type":"JSH"}}""", jsonString)

    }
}
class BloodPressureCategoryTest() {
    private val guideline = BloodPressureGuideline.WHO

    @Test
    fun categoryTest() {
        assertEquals(guideline.getCategory(110, 65), guideline.normal)
        assertEquals(guideline.getCategory(145, 65), guideline.htn1)
        assertEquals(guideline.getCategory(200, 75), guideline.htn3)
        assertEquals(guideline.getCategory(null, null), guideline.invalidCategory)
        assertEquals(guideline.getCategory(165, true), guideline.htn2)
        assertEquals(guideline.getCategory(87, false), guideline.elevated)
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