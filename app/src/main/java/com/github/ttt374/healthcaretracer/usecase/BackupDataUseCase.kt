package com.github.ttt374.healthcaretracer.usecase

import android.net.Uri
import android.util.Log
import android.util.Log.e
import com.github.ttt374.healthcaretracer.data.item.Item
import com.github.ttt374.healthcaretracer.data.repository.ItemRepository
import com.github.ttt374.healthcaretracer.shared.Logger
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Reader
import java.io.Writer


/////////////////////////////////

class ExportDataUseCase @Inject constructor(private val itemRepository: ItemRepository, private val contentResolverWrapper: ContentResolverWrapper) {
    suspend operator fun invoke(uri: Uri): Result<String> = runCatching {
        val items = itemRepository.getAllItems()  // .firstOrNull() ?: emptyList()

        withContext(Dispatchers.IO) {
            contentResolverWrapper.openOutputStream(uri)?.use { outputStream ->
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
            for (item in items) {
                val row = CsvField.entries.map { it.format(item) }
                csvWriter.writeNext(row.toTypedArray())
            }
        }
    }
}
////////////////
class ImportDataUseCase @Inject constructor(private val itemRepository: ItemRepository, private val contentResolverWrapper: ContentResolverWrapper, private val logger: Logger){
    suspend operator fun invoke(uri: Uri): Result<String> = runCatching {
        withContext(Dispatchers.IO) {
            contentResolverWrapper.openInputStream(uri)?.use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    val importedItems = readItemsFromCsv(reader)
                    itemRepository.replaceAllItems(importedItems)
                }
            }
        }
        "Import successful"
    }.onFailure { e -> Log.e("ImportDataUseCase", "CSV import failed", e) }

    private fun readItemsFromCsv(reader: Reader): List<Item> {
        val items = mutableListOf<Item>()
        CSVReader(reader).use { csvReader ->
            val headers = csvReader.readNext()?.map { it.trim() } ?: return emptyList()
            val fieldMap = headers.withIndex().mapNotNull { (i, name) ->
                CsvField.entries.find { it.fieldName == name }?.let { it to i }
            }.toMap()

            var line = csvReader.readNext()
            var rowIndex = 1
            while (line != null) {
                var partial = CsvItemPartial()
                var hasMissingRequiredField = false
                for ((field, idx) in fieldMap) {
                    val value = line.getOrNull(idx) // nullable String?
                    if (field.isRequired && (value == null || value.isEmpty())) {
                        logger.e("csv reader", "Missing required field '${field.name}' in line $rowIndex")
                        hasMissingRequiredField = true
                        break // この行はスキップ対象にする
                    }
                    partial = field.parse(value.orEmpty())(partial)
                }
//                // skip if required fields are missing
//                val missingFields = CsvField.entries.filter { it.required && it !in fieldMap.keys }.map { it.name }
//                if (missingFields.isNotEmpty()) {
//                    Log.e("csv reader", "Required fields missing in line $rowIndex: ${missingFields.joinToString(", ")}")
//                    line = csvReader.readNext()
//                    continue
//                }
                items.add(partial.toItem())
                line = csvReader.readNext()
                rowIndex++
            }
        }
        return items
    }
}
