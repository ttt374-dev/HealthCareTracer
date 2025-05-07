package com.github.ttt374.healthcaretracer

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Before
import com.github.ttt374.healthcaretracer.data.repository.Config
import java.time.ZoneId


class ConfigSettingsTest {
    private lateinit var config: Config

    @Before
    fun setup(){
        config = Config()
    }
//    @Test  //TODO
//    fun validZoneIdTest() = runBlocking{
//        // 有効なタイムゾーンを渡す
//        val updatedConfig = config.updateTimeZone("Asia/Tokyo")
//
//        // 更新されたConfigにタイムゾーンが反映されていることを確認
//        assertEquals(ZoneId.of("Asia/Tokyo"), updatedConfig.zoneId)
//    }
//    @Test
//    fun `test invalid zoneId returns default with error message`() {
//        // 無効なタイムゾーンを渡す
//        val updatedConfig = config.updateTimeZone("Invalid/Zone")
//
//        // タイムゾーンがデフォルト値になっていることを確認
//        assertEquals(ZoneId.systemDefault(), updatedConfig.zoneId)
//
//    }

//    @Test
//    fun `test empty zoneId returns default with error message`() {
//        // 空のタイムゾーンを渡す
//        val updatedConfig = config.updateTimeZone("")
//
//        // タイムゾーンがデフォルト値になっていることを確認
//        assertEquals(ZoneId.systemDefault(), updatedConfig.zoneId)
//    }
}
