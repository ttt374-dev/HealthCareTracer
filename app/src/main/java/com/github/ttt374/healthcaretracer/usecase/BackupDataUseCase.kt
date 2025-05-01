package com.github.ttt374.healthcaretracer.usecase

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.github.ttt374.healthcaretracer.data.bloodpressure.toBloodPressure
import com.github.ttt374.healthcaretracer.data.item.Item
import com.github.ttt374.healthcaretracer.data.item.Vitals
import com.github.ttt374.healthcaretracer.data.repository.ItemRepository
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Reader
import java.io.Writer
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

abstract class CsvField<T>(
    val csvName: String,
    val isRequired: Boolean
) {
    abstract fun format(item: Item): String
    //abstract fun parse(line: Array<String>, map: Map<String, Int>): T?
    abstract fun parse(string: String): T?
    fun parseLine(line: Array<String>, map: Map<String, Int>): T? {
        return line.getOrNull(map[MeasuredAt.csvName] ?: -1)?.let { parse(it) }
    }
    companion object {
        val entries: List<CsvField<*>> = listOf(
            MeasuredAt, BpUpper, BpLower, Pulse, BodyWeight, BodyTemp, Location, Memo
        )
    }

    object MeasuredAt : CsvField<Instant>("measuredAt", true) {
        private val formatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault())
        override fun format(item: Item): String = formatter.format(item.measuredAt)
        override fun parse(string: String): Instant? =
            string.takeIf { it.isNotBlank() }?.let { Instant.parse(it) }
    }

    object BpUpper : CsvField<Int>("BP upper", true) {
        override fun format(item: Item): String = item.vitals.bp?.upper?.toString() ?: ""
        override fun parse(string: String): Int? = string.toIntOrNull()
    }

    object BpLower : CsvField<Int>("BP lower", true) {
        override fun format(item: Item): String = item.vitals.bp?.lower?.toString() ?: ""
        override fun parse(string: String): Int? = string.toIntOrNull()
    }

    object Pulse : CsvField<Int>("pulse", false) {
        override fun format(item: Item): String = item.vitals.pulse?.toString() ?: ""
        override fun parse(string: String): Int? = string.toDoubleOrNull()?.toInt()
    }

    object BodyWeight : CsvField<Double>("body weight", false) {
        override fun format(item: Item): String = item.vitals.bodyWeight?.toString() ?: ""
        override fun parse(string: String): Double? = string.toDoubleOrNull()
    }

    object BodyTemp : CsvField<Double>("body temperature", false) {
        override fun format(item: Item): String = item.vitals.bodyTemperature?.toString() ?: ""
        override fun parse(string: String): Double? = string.toDoubleOrNull()
    }

    object Location : CsvField<String>("location", false) {
        override fun format(item: Item): String = item.location
        override fun parse(string: String): String = string
    }

    object Memo : CsvField<String>("memo", false) {
        override fun format(item: Item): String = item.memo
        override fun parse(string: String): String =
            string
    }
}


class ExportDataUseCase( private val itemRepository: ItemRepository) {
    suspend operator fun invoke(uri: Uri, contentResolver: ContentResolver): Result<String> = runCatching {
        val items = itemRepository.getAllItems()  // .firstOrNull() ?: emptyList()

        withContext(Dispatchers.IO) {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writeItemsToCsv(writer, items)
                }
            }
        }
        "download to CSV done"
    }.onFailure { e -> Log.e("ExportDataUseCase", "CSV export failed", e) }

    ///////////
    fun writeItemsToCsv(writer: Writer, items: List<Item>) {
        CSVWriter(writer).use { csvWriter ->
            // 1. ヘッダー行を書き出し
            val headers = CsvField.entries.map { it.csvName }
            csvWriter.writeNext(headers.toTypedArray())

            // 2. 各 Item を CsvField で format して行を構成
            for (item in items) {
                val row = CsvField.entries.map { it.format(item) }
                csvWriter.writeNext(row.toTypedArray())
            }
        }
    }


}
////////////////
class ImportDataUseCase(private val itemRepository: ItemRepository){
    suspend operator fun invoke(uri: Uri, contentResolver: ContentResolver): Result<String> = runCatching {
        Log.d("import data", uri.toString())

        withContext(Dispatchers.IO) {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    val importedItems = readItemsFromCsv(reader)
                    itemRepository.replaceAllItems(importedItems)
                }
            }
        }
        "Import successful"
    }.onFailure { e -> Log.e("ImportDataUseCase", "CSV import failed", e) }

    fun readItemsFromCsv(reader: Reader): List<Item> {
        val result = mutableListOf<Item>()

        CSVReader(reader).use { csvReader ->
            val headers = csvReader.readNext()?.map { it.trim() } ?: return emptyList()
            val indexMap = headers.withIndex().associate { it.value to it.index }

            var index = 1
            var line = csvReader.readNext()
            while (line != null) {
                try {
                    val requiredMissing = CsvField.entries
                        .filter { it.isRequired }
                        .any { it.parseLine(line, indexMap) == null }

                    if (requiredMissing) {
                        Log.e("read csv", "Row $index skipped: missing required fields")
                        line = csvReader.readNext(); index++; continue
                    }

                    val measuredAt = CsvField.MeasuredAt.parseLine(line, indexMap)!!
                    val upper = CsvField.BpUpper.parseLine(line, indexMap)
                    val lower = CsvField.BpLower.parseLine(line, indexMap)
                    val pulse = CsvField.Pulse.parseLine(line, indexMap)
                    val bodyWeight = CsvField.BodyWeight.parseLine(line, indexMap)
                    val bodyTemp = CsvField.BodyTemp.parseLine(line, indexMap)
                    val location = CsvField.Location.parseLine(line, indexMap) ?: ""
                    val memo = CsvField.Memo.parseLine(line, indexMap) ?: ""

                    result.add(
                        Item(
                            measuredAt = measuredAt,
                            vitals = Vitals(
                                bp = (upper to lower).toBloodPressure(),
                                pulse = pulse,
                                bodyWeight = bodyWeight,
                                bodyTemperature = bodyTemp
                            ),
                            location = location,
                            memo = memo
                        )
                    )
                } catch (e: Exception) {
                    Log.e("read csv", "Row $index error: ${e.message}")
                }
                line = csvReader.readNext(); index++
            }
        }

        return result
    }




}
/////////////////////
// for export
fun Item.toCsvRow(formatter: DateTimeFormatter): Array<String> = arrayOf(
    id.toString(),
    formatter.format(measuredAt),
    vitals.bp?.upper?.toString() ?: "",
    vitals.bp?.lower?.toString() ?: "",
    vitals.pulse?.toString() ?: "",
    vitals.bodyWeight?.toString() ?: "",
    vitals.bodyTemperature?.toString() ?: "",
    location,
    memo
)
// for import
