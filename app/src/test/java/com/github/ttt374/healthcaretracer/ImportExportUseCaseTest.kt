package com.github.ttt374.healthcaretracer

import android.content.ContentResolver
import android.net.Uri
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressure
import com.github.ttt374.healthcaretracer.data.item.Item
import com.github.ttt374.healthcaretracer.data.item.Vitals
import com.github.ttt374.healthcaretracer.data.repository.ItemRepository
import com.github.ttt374.healthcaretracer.usecase.ContentResolverWrapper
import com.github.ttt374.healthcaretracer.usecase.ExportDataUseCase
import com.github.ttt374.healthcaretracer.usecase.toInstantOrNull
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.StringWriter



class ExportDataUseCaseTest {

//    private lateinit var itemRepository: ItemRepository
//    private lateinit var contentResolver: ContentResolver
//    private lateinit var exportDataUseCase: ExportDataUseCase
//
//    @Before
//    fun setUp() {
//        //itemRepository = mock()
//        //contentResolver = mock()
//        //exportDataUseCase = ExportDataUseCase(itemRepository)
//    }

    @Test
    fun `test export data success`() = runBlocking {
        // モックデータ
        val items = listOf(
            Item(measuredAt = "2022-01-01T00:00:00Z".toInstantOrNull()!!, vitals = Vitals(bp = BloodPressure(120, 80), pulse = 70), location = "Tokyo", memo = "Test memo"),
            Item(measuredAt = "2022-01-02T00:00:00Z".toInstantOrNull()!!, vitals = Vitals(bp = BloodPressure(130, 85), pulse = 75), location = "Osaka", memo = "Another memo")
        )

        val contentResolverWrapper = mock<ContentResolverWrapper>()
        val itemRepository = mock<ItemRepository>()
        val exportDataUseCase = ExportDataUseCase(itemRepository, contentResolverWrapper)

        whenever(itemRepository.getAllItems()).thenReturn(items)

        val outputStream = ByteArrayOutputStream()
        val uri = mock<Uri>()

        whenever(contentResolverWrapper.openOutputStream(uri)).thenReturn(outputStream)

        // 実行
        val result = exportDataUseCase(uri)

        // 結果の検証
        assertEquals("download to CSV done", result.getOrNull())

        // 書き込まれた内容の確認
        val expectedCsvData = """
            "Measured at","Bp upper","Bp lower","Pulse","Body weight","Body temperature","Location","Memo"
            "2022-01-01T00:00:00Z","120","80","70","","","Tokyo","Test memo"
            "2022-01-02T00:00:00Z","130","85","75","","","Osaka","Another memo"
        """.trimIndent()

        val actualCsv = outputStream.toString(Charsets.UTF_8.name()).trim()
        assertEquals(expectedCsvData, actualCsv)

    }
//
//    @Test
//    fun `test export data failure when no output stream`() = runBlocking {
//        // モックの設定
//        whenever(itemRepository.getAllItems()).thenReturn(emptyList())
//
//        //val uri = mock<Uri>()
//        val uri = Uri.parse("content://com.example.app/test")
//        whenever(contentResolver.openOutputStream(uri)).thenReturn(null)
//
//        // 実行
//        val result = exportDataUseCase(uri)
//
//        // 結果の検証
//        assert(result.isFailure)
//    }
}
