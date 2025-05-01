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


sealed class CsvValue() {
    data class Double(val value: kotlin.Double) : CsvValue()
    data class Int(val value: kotlin.Int) : CsvValue()
    data class String(val value: kotlin.String) : CsvValue()
    data class Instant(val value: java.time.Instant): CsvValue()
}
internal fun Instant.toCsvValue() = CsvValue.Instant(this)
internal fun Int.toCsvValue() = CsvValue.Int(this)
internal fun Double.toCsvValue() = CsvValue.Double(this)
internal fun String.toCsvValue() = CsvValue.String(this)

abstract class CsvField<T>(
    val csvName: String,
    val isRequired: Boolean,
    val format: (item: Item) -> String,
    val parseString: (string: String) -> CsvValue? = { CsvValue.String("asdf") },
    val parse: (string: String) -> CsvValue = { CsvValue.String(it) },
) {
    //abstract fun format(item: Item): String
    //abstract fun parse(line: Array<String>, map: Map<String, Int>): T?
    //abstract fun parse(string: String): T?
    fun parseLine(line: List<String>, map: Map<String, Int>): CsvValue? {
        return line.getOrNull(map[MeasuredAt.csvName] ?: -1)?.let { parse(it) }
    }
    companion object {
        val entries: List<CsvField<*>> = listOf(
            MeasuredAt, BpUpper, BpLower, Pulse  // , BodyWeight, BodyTemp, Location, Memo
        )
    }
    object MeasuredAt : CsvField<CsvValue>("measuredAt", true,
        { item ->
            val formatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault())
            formatter.format(item.measuredAt)
        },
        { string -> string.takeIf { it.isNotBlank() }?.let { Instant.parse(it).toCsvValue() } }
    )
    object BpUpper : CsvField<CsvValue>("BP upper", true,
        { it.vitals.bp?.upper?.toString() ?: ""},
        { it.toIntOrNull()?.toCsvValue()}
    )

    object BpLower : CsvField<CsvValue>("BP lower", true,
        { it.vitals.bp?.lower?.toString() ?: ""},
        { it.toIntOrNull()?.toCsvValue()}
    )

    object Pulse : CsvField<CsvValue>("pulse", false,
        { it.vitals.pulse?.toString() ?: ""}, { it.toIntOrNull()?.toCsvValue()}
    )
//    object BodyWeight : CsvField<CsvValue>("body weight", false,
//        { it.vitals.bodyWeight?.toString() ?: ""}
//    ) {
//        override fun parse(string: String) = string.toDoubleOrNull()?.toCsvValue()
//    }
//
//    object BodyTemp : CsvField<CsvValue>("body temperature", false,
//        { it.vitals.bodyTemperature?.toString() ?: ""}
//    ) {
//        override fun parse(string: String) = string.toDoubleOrNull()?.toCsvValue()
//    }
//
//    object Location : CsvField<CsvValue>("location", false, { it.location }){
//        override fun parse(string: String) = string.toCsvValue()
//    }
//
//    object Memo : CsvField<CsvValue>("memo", false, { it.memo }) {
//        override fun parse(string: String) = string.toCsvValue()
//    }
}

/////////////////////////////////

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


    private fun readItemsFromCsv(reader: Reader): List<Item> {
        val result = mutableListOf<Item>()
        CSVReader(reader).use { csvReader ->
            val headers = csvReader.readNext()?.map { it.trim() } ?: return emptyList()
            val indexMap = headers.withIndex().associate { it.value to it.index }

            var rowIndex = 1
            var line = csvReader.readNext()
            while (line != null) {
                try {
                    line.toList().parseToItem(indexMap, rowIndex)?.let { result.add(it)}

                } catch (e: Exception) {
                    Log.e("read csv", "Row $rowIndex error: ${e.message}")
                }
                line = csvReader.readNext(); rowIndex++
            }
        }

        return result
    }
}
fun List<String>.parseToItem(indexMap: Map<String, Int>, rowIndex: Int): Item? {
    // 1回のループで全部パースしてキャッシュ
    val parsedValues = CsvField.entries.associateWith { it.parseLine(this, indexMap) }
    val missingRequired = CsvField.entries.filter { it.isRequired }.filter { parsedValues[it] == null }
    if (missingRequired.isNotEmpty()) {
        Log.e("read csv", "Row $rowIndex skipped: missing required fields - ${missingRequired.joinToString { it.csvName }}")
        return null
    }

    // 必須は !! で安全に取り出し、任意は ? で取り出す
    val measuredAt = parsedValues[CsvField.MeasuredAt] as CsvValue.Instant
    val upper = parsedValues[CsvField.BpUpper] as CsvValue.Int
    val lower = parsedValues[CsvField.BpLower] as CsvValue.Int
    val pulse = parsedValues[CsvField.Pulse] as CsvValue.Int
//        val bodyWeight = parsedValues[CsvField.BodyWeight] as? CsvValue.Double
//        val bodyTemp = parsedValues[CsvField.BodyTemp] as? CsvValue.Double
//        val location = (parsedValues[CsvField.Location] as? CsvValue.String)?.value ?: ""
//        val memo = (parsedValues[CsvField.Memo] as? CsvValue.String)?.value ?: ""

    return Item(
        measuredAt = measuredAt.value,
        vitals = Vitals(
            bp = (upper.value to lower.value).toBloodPressure(),
            pulse = pulse.value,
//                bodyWeight = bodyWeight?.value,
//                bodyTemperature = bodyTemp?.value
        ),
//            location = location,
//            memo = memo
    )
}