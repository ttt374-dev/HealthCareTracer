package com.github.ttt374.healthcaretracer

import android.net.Uri
import com.github.ttt374.csv_backup_lib.ContentResolverWrapper
import com.github.ttt374.csv_backup_lib.CsvExporter
import com.github.ttt374.csv_backup_lib.ExportDataUseCase
import com.github.ttt374.csv_backup_lib.ImportDataUseCase
import com.github.ttt374.healthcaretracer.data.backup.ItemCsvSchema
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressure
import com.github.ttt374.healthcaretracer.data.item.Item
import com.github.ttt374.healthcaretracer.data.item.Vitals
import com.github.ttt374.healthcaretracer.data.repository.ItemRepository
import com.github.ttt374.healthcaretracer.shared.Logger
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
import java.time.Instant
import java.time.format.DateTimeParseException

internal fun String.toInstantOrNull(): Instant? {
    return try {
        Instant.parse(this)  // ISO 8601形式の文字列をInstantに変換
    } catch (e: DateTimeParseException) {
        null  // 変換に失敗した場合はnullを返す
    }
}
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

        val exportDataUseCase = ExportDataUseCase( { itemRepository.getAllItems() },
            //itemRepository,
            CsvExporter(ItemCsvSchema.fields), contentResolverWrapper
        )
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
    private lateinit var importDataUseCase: ImportDataUseCase<Item>
    private lateinit var uri: Uri

    @Before
    fun setup() {
        contentResolverWrapper = mock()
        itemRepository = mock()
        val logger = mock<Logger>()
        importDataUseCase = ImportDataUseCase(
            { itemRepository.replaceAllItems(it)},
            com.github.ttt374.csv_backup_lib.CsvImporter({ Item()}, ItemCsvSchema.fields), contentResolverWrapper
        )
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
    @Test
    fun importInstantFormatErrorTest() = runBlocking {
        val importedCsvData = """
            "Measured at","Bp upper","Bp lower","Pulse","Body weight","Body temperature","Location","Memo"
            "2022-01-01T00:00:00Zabcdef","120","80","70","","","","measured at format error"
           
        """.trimIndent()
        val inputStream = ByteArrayInputStream(importedCsvData.toByteArray())
        whenever(contentResolverWrapper.openInputStream(uri)).thenReturn(inputStream)
        importDataUseCase(uri)

        val captor = argumentCaptor<List<Item>>()

        verify(itemRepository).replaceAllItems(captor.capture())
        val capturedItems = captor.firstValue
        assertEquals(0, capturedItems.size)
    }
}
