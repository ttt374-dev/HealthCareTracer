package com.github.ttt374.healthcaretracer

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
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream


class ExportDataUseCaseTest {

    private lateinit var itemRepository: ItemRepository
    private lateinit var contentResolverWrapper: ContentResolverWrapper
    //private lateinit var exportDataUseCase: ExportDataUseCase
    private lateinit var uri: Uri

    @Before
    fun setup() {
        itemRepository = mock()
        contentResolverWrapper = mock()
        uri = mock()
        //exportDataUseCase = ExportDataUseCase(itemRepository, contentResolver)
    }


    @Test
    fun exportTest() = runBlocking {
        //val contentResolverWrapper = mock<ContentResolverWrapper>()
        //val itemRepository = mock<ItemRepository>()
        val outputStream = ByteArrayOutputStream()

        val exportDataUseCase = ExportDataUseCase(itemRepository, CsvExporter(), contentResolverWrapper)
        //val uri = mock<Uri>()

        // モックデータ
        val items = listOf(
            Item(measuredAt = "2022-01-01T00:00:00Z".toInstantOrNull()!!, vitals = Vitals(bp = BloodPressure(120, 80), pulse = 70), location = "Tokyo", memo = "Test memo"),
            Item(measuredAt = "2022-01-02T00:00:00Z".toInstantOrNull()!!, vitals = Vitals(bp = BloodPressure(130, 85), pulse = 75), location = "Osaka", memo = "Another memo")
        )
        whenever(itemRepository.getAllItems()).thenReturn(items)
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
}
////////////

class ImportCsvUseCaseTest {
    private lateinit var contentResolverWrapper: ContentResolverWrapper
    private lateinit var itemRepository: ItemRepository
    private lateinit var importDataUseCase: ImportDataUseCase
    private lateinit var uri: Uri

    @Before
    fun setup() {
        contentResolverWrapper = mock()
        itemRepository = mock()
        val logger = mock<Logger>()
        importDataUseCase = ImportDataUseCase(itemRepository, CsvImporter(logger), contentResolverWrapper)
        uri = mock<Uri>()
    }

    @Test
    fun importTest() = runBlocking {
        val importedCsvData = """
            "Measured at","Bp upper","Bp lower","Pulse","Body weight","Body temperature","Location","Memo"
            "2022-01-01T00:00:00Z","120","80","70","","","Tokyo","Test memo"
            "2022-01-02T00:00:00Z","130","85","60.2","","","Osaka","Another memo"
        """.trimIndent()
        val inputStream = ByteArrayInputStream(importedCsvData.toByteArray())
        whenever(contentResolverWrapper.openInputStream(uri)).thenReturn(inputStream)
        //whenever(itemRepository.replaceAllItems(any())).thenReturn(Unit)

//        val result = importDataUseCase(uri)
//        assertTrue(result.isSuccess)
        // UseCase呼び出し
        importDataUseCase(uri)

        val captor = argumentCaptor<List<Item>>()
        //val csvData = "id,name\n1,Apple\n2,Banana" // 例：CSVデータ

        verify(itemRepository).replaceAllItems(captor.capture())
        val capturedItems = captor.firstValue
        assertEquals(2, capturedItems.size)
        assertEquals(120, capturedItems[0].vitals.bp?.upper)
//        assertEquals(null, capturedItems[2].vitals.bp?.upper)
//        assertEquals(null, capturedItems[2].vitals.bp?.lower)
//        assertEquals(null, capturedItems[1].vitals.pulse)
    }
    @Test
    fun importRequiredMissingTest() = runBlocking {
        val importedCsvData = """
            "Measured at","Bp upper","Bp lower","Pulse","Body weight","Body temperature","Location","Memo"
            "2022-01-01T00:00:00Z","120","80","70","","","","ok"
            "2022-01-02T00:00:00Z","130","","60","","","","ng: bp lower missing"
            "2022-01-03T00:00:00Z","","80","60","","","","ng: bp upper missing"            
        """.trimIndent()
        val inputStream = ByteArrayInputStream(importedCsvData.toByteArray())
        whenever(contentResolverWrapper.openInputStream(uri)).thenReturn(inputStream)
        importDataUseCase(uri)

        val captor = argumentCaptor<List<Item>>()

        verify(itemRepository).replaceAllItems(captor.capture())
        val capturedItems = captor.firstValue
        assertEquals(1, capturedItems.size)
//        assertEquals(Item(), capturedItems[2])

    }
}
