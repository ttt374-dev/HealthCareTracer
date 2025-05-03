package com.github.ttt374.healthcaretracer

import kotlinx.coroutines.runBlocking
import org.junit.Test
import android.net.Uri
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressure
import com.github.ttt374.healthcaretracer.data.item.Item
import com.github.ttt374.healthcaretracer.data.item.Vitals
import com.github.ttt374.healthcaretracer.data.repository.ItemRepository
import com.github.ttt374.healthcaretracer.shared.Logger
import com.github.ttt374.healthcaretracer.data.backup.ContentResolverWrapper
import com.github.ttt374.healthcaretracer.data.backup.CsvExporter
import com.github.ttt374.healthcaretracer.data.backup.CsvImporter
import com.github.ttt374.healthcaretracer.usecase.ExportDataUseCase
import com.github.ttt374.healthcaretracer.usecase.ImportDataUseCase
import com.github.ttt374.healthcaretracer.data.backup.toInstantOrNull
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import com.github.ttt374.healthcaretracer.data.repository.Config
import java.time.ZoneId


class ConfigSettingsTest {
    private lateinit var config: Config

    @Before
    fun setup(){
        config = Config()
    }
    @Test
    fun validZoneIdTest() = runBlocking{
        // 有効なタイムゾーンを渡す
        val updatedConfig = config.updateTimeZone("Asia/Tokyo")

        // 更新されたConfigにタイムゾーンが反映されていることを確認
        assertEquals(ZoneId.of("Asia/Tokyo"), updatedConfig.zoneId)
    }
    @Test
    fun `test invalid zoneId returns default with error message`() {
        // 無効なタイムゾーンを渡す
        val updatedConfig = config.updateTimeZone("Invalid/Zone")

        // タイムゾーンがデフォルト値になっていることを確認
        assertEquals(ZoneId.systemDefault(), updatedConfig.zoneId)

    }

    @Test
    fun `test empty zoneId returns default with error message`() {
        // 空のタイムゾーンを渡す
        val updatedConfig = config.updateTimeZone("")

        // タイムゾーンがデフォルト値になっていることを確認
        assertEquals(ZoneId.systemDefault(), updatedConfig.zoneId)
    }
}
