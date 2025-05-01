package com.github.ttt374.healthcaretracer

import android.content.ContentResolver
import android.net.Uri
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressure
import com.github.ttt374.healthcaretracer.data.item.Item
import com.github.ttt374.healthcaretracer.data.item.Vitals
import com.github.ttt374.healthcaretracer.data.repository.ItemRepository
import com.github.ttt374.healthcaretracer.usecase.ExportDataUseCase
import com.github.ttt374.healthcaretracer.usecase.toInstantOrNull
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.StringWriter


class ExportDataUseCaseTest {

    private lateinit var itemRepository: ItemRepository
    private lateinit var contentResolver: ContentResolver
    private lateinit var exportDataUseCase: ExportDataUseCase

    @Before
    fun setUp() {
        itemRepository = mock()
        contentResolver = mock()
        exportDataUseCase = ExportDataUseCase(itemRepository)
    }

    @Test
    fun `test export data success`() = runBlocking {
        // モックデータ
        val items = listOf(
            Item(measuredAt = "2022-01-01T00:00:00".toInstantOrNull()!!, vitals = Vitals(bp = BloodPressure(120, 80), pulse = 70), location = "Tokyo", memo = "Test memo"),
            Item(measuredAt = "2022-01-02T00:00:00".toInstantOrNull()!!, vitals = Vitals(bp = BloodPressure(130, 85), pulse = 75), location = "Osaka", memo = "Another memo")
        )

        // モックの設定
        whenever(itemRepository.getAllItems()).thenReturn(items)

        val outputStream = ByteArrayOutputStream()
        val uri = mock<Uri>()

        whenever(contentResolver.openOutputStream(uri)).thenReturn(outputStream)

        // 実行
        val result = exportDataUseCase(uri, contentResolver)

        // 結果の検証
        assertEquals("download to CSV done", result.getOrNull())

        // 書き込まれた内容の確認
        val expectedCsvData = """
            measuredAt,BP upper,BP lower,pulse,body weight,body temperature,location,memo
            2022-01-01T00:00:00,120,80,70,,,Tokyo,Test memo
            2022-01-02T00:00:00,130,85,75,,,Osaka,Another memo
        """.trimIndent()

        val actualCsv = outputStream.toString(Charsets.UTF_8.name()).trim()
        assertEquals(expectedCsvData, actualCsv)

    }

    @Test
    fun `test export data failure when no output stream`() = runBlocking {
        // モックの設定
        whenever(itemRepository.getAllItems()).thenReturn(emptyList())

        val uri = mock<Uri>()
        whenever(contentResolver.openOutputStream(uri)).thenReturn(null)

        // 実行
        val result = exportDataUseCase(uri, contentResolver)

        // 結果の検証
        assert(result.isFailure)
    }
}
