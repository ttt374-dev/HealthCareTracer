package com.github.ttt374.healthcaretracer

import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressure
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressureGuideline
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */


class BloodPressureGuidelineTest() {
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
