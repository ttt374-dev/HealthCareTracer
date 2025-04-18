package com.github.ttt374.healthcaretracer.usecase

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.github.ttt374.healthcaretracer.data.item.Item
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


class ExportDataUseCase( private val itemRepository: ItemRepository) {
    suspend operator fun invoke(uri: Uri, contentResolver: ContentResolver): Result<String> = runCatching {
        val items = itemRepository.getAllItems()  // .firstOrNull() ?: emptyList()

        withContext(Dispatchers.IO) {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writeToCsv(writer, items)
                }
            }
        }
        "download to CSV done"
    }.onFailure { e -> Log.e("ExportDataUseCase", "CSV export failed", e) }

    ///////////
    private fun writeToCsv(writer: Writer, items: List<Item>){
        CSVWriter(writer).use { csvWriter ->
            csvWriter.writeNext(arrayOf("id", "measuredAt", "BP upper", "BP lower", "pulse", "body weight", "body temperature", "location", "memo"))
            val formatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault())

            items.forEach { item ->
                val data = arrayOf(
                    item.id.toString(),
                    formatter.format(item.measuredAt),
                    item.bpUpper.toString(),
                    item.bpLower.toString(),
                    item.pulse.toString(),
                    item.bodyWeight.toString(),
                    item.bodyTemperature.toString(),
                    item.location,
                    item.memo,
                )
                csvWriter.writeNext(data)
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
                    val importedItems = readCsv(reader)
                    itemRepository.replaceAllItems(importedItems)
                }
            }
        }
        "Import successful"
    }.onFailure { e -> Log.e("ImportDataUseCase", "CSV import failed", e) }

    private fun readCsv(reader: Reader): List<Item>{
        val importedItems = mutableListOf<Item>()
        CSVReader(reader).use { csvReader ->
            csvReader.readAll().drop(1).forEachIndexed { index, columns ->
                if (columns.size < 9) {
                    Log.e("parse csv", "Row $index skipped: insufficient columns")
                    return@forEachIndexed
                }
                try {
                    val item = Item(measuredAt = Instant.parse(columns[1]),
                        bpUpper = columns[2].toIntOrNull(),
                        bpLower = columns[3].toIntOrNull(),
                        pulse = columns[4].toIntOrNull(),
                        bodyWeight = columns[5].toDoubleOrNull(),
                        bodyTemperature = columns[6].toDoubleOrNull(),
                        location = columns[7],
                        memo = columns[8]
                    )
                    importedItems.add(item)
                } catch (e: Exception){
                    Log.e("parse csv", "Row $index failed: ${e.message}")
                }
            }
        }
        return importedItems.toList()
    }
}
