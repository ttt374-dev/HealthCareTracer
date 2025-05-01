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
import java.time.format.DateTimeParseException

fun String.toInstantOrNull(): Instant? {
    return try {
        Instant.parse(this)  // ISO 8601形式の文字列をInstantに変換
    } catch (e: DateTimeParseException) {
        null  // 変換に失敗した場合はnullを返す
    }
}
enum class CsvField(
    //val fieldName: String,
    val required: Boolean,
    val format: (Item) -> String,
    val parse: (String) -> (CsvItemPartial) -> CsvItemPartial,
    val specificFieldName: String? = null,
) {
    MEASURED_AT(
        specificFieldName = "Measured at",
        required = true,
        format = { it.measuredAt.toString() },
        parse = { str -> { it.copy(measuredAt = str.toInstantOrNull()) } }
    ),
    BP_UPPER(
        specificFieldName = "Bp upper",
        required = true,
        format = { it.vitals.bp?.upper?.toString().orEmpty() },
        parse = { str -> { it.copy(bpUpper = str.toIntOrNull()) } }
    ),
    BP_LOWER(
        specificFieldName = "Bp lower",
        required = true,
        format = { it.vitals.bp?.lower?.toString().orEmpty() },
        parse = { str -> { it.copy(bpLower = str.toIntOrNull()) } }
    ),
    PULSE(
        specificFieldName = "Pulse",
        required = false,
        format = { it.vitals.pulse?.toString().orEmpty() },
        parse = { str -> { it.copy(pulse = str.toIntOrNull()) } }
    ),
    BODY_WEIGHT(
        specificFieldName = "Body weight",
        required = false,
        format = { it.vitals.bodyWeight?.toString().orEmpty() },
        parse = { str -> { it.copy(bodyWeight = str.toDoubleOrNull()) } }
    ),
    BODY_TEMPERATURE(
        specificFieldName = "Body temperature",
        required = false,
        format = { it.vitals.bodyTemperature?.toString().orEmpty() },
        parse = { str -> { it.copy(bodyTemperature = str.toDoubleOrNull()) } }
    ),
    LOCATION(
        specificFieldName = "Location",
        required = false,
        format = { it.location },
        parse = { str -> { it.copy(location = str) } }
    ),
    MEMO(
        specificFieldName = "Memo",
        required = false,
        format = { it.memo },
        parse = { str -> { it.copy(memo = str) } }
    );

    val fieldName: String = this.specificFieldName ?: name.replace('_', ' ').lowercase().replaceFirstChar(Char::uppercase)
}
data class CsvItemPartial(
    val measuredAt: Instant? = null,
    val bpUpper: Int? = null,
    val bpLower: Int? = null,
    val pulse: Int? = null,
    val bodyWeight: Double? = null,
    val bodyTemperature: Double? = null,
    val location: String = "",
    val memo: String = ""
)

fun CsvItemPartial.toItem(): Item = Item(
    measuredAt = measuredAt ?: Instant.EPOCH,
    vitals = Vitals(
        bp = (bpUpper to bpLower).toBloodPressure(),
        pulse = pulse,
        bodyWeight = bodyWeight,
        bodyTemperature = bodyTemperature
    ),
    location = location,
    memo = memo
)

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
    private fun writeItemsToCsv(writer: Writer, items: List<Item>) {
        CSVWriter(writer).use { csvWriter ->
            // 1. ヘッダー行を書き出し
            val headers = CsvField.entries.map { it.fieldName }
            csvWriter.writeNext(headers.toTypedArray())

            // 2. 各 Item を CsvField で format して行を構成
            for (item in items) { // TODO: required check
                val row = CsvField.entries.map { it.format(item) }
                csvWriter.writeNext(row.toTypedArray())
            }
        }
    }


}
////////////////
class ImportDataUseCase(private val itemRepository: ItemRepository){
    suspend operator fun invoke(uri: Uri, contentResolver: ContentResolver): Result<String> = runCatching {
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
        val items = mutableListOf<Item>()
        CSVReader(reader).use { csvReader ->
            val headers = csvReader.readNext()?.map { it.trim() } ?: return emptyList()
            val fieldMap = headers.withIndex().mapNotNull { (i, name) ->
                CsvField.entries.find { it.fieldName == name }?.let { it to i }
            }.toMap()

            var line = csvReader.readNext()
            while (line != null) {
                var partial = CsvItemPartial()
                for ((field, idx) in fieldMap) {
                    val value = line.getOrNull(idx).orEmpty()
                    partial = field.parse(value)(partial)
                }

                // skip if required fields are missing
                if (CsvField.entries.any { it.required && it !in fieldMap.keys }) {
                    line = csvReader.readNext()
                    continue
                }

                items.add(partial.toItem())
                line = csvReader.readNext()
            }
        }
        return items
    }


}
