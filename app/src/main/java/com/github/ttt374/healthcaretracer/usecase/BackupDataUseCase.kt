package com.github.ttt374.healthcaretracer.usecase

import android.content.Context
import android.net.Uri
import android.util.Log
import com.github.ttt374.healthcaretracer.data.item.Item
import com.github.ttt374.healthcaretracer.data.item.ItemRepository
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Reader
import java.io.Writer
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


class ExportDataUseCase( @ApplicationContext private val context: Context, private val itemRepository: ItemRepository) {
//    suspend operator fun invoke(filename: String? = null): Result<String> = runCatching {
    suspend operator fun invoke(uri: Uri): Result<String> = runCatching {
        val items = itemRepository.getAllItemsFlow().firstOrNull() ?: emptyList()

        withContext(Dispatchers.IO) {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writeToCsv(writer, items)
                }
            }
        }
        "download to CSV done"
    }.onFailure { e -> Log.e("download csv usecase", e.toString()) }

    ///////////
    private fun writeToCsv(writer: Writer, items: List<Item>){
        CSVWriter(writer).use { csvWriter ->
            csvWriter.writeNext(arrayOf("id", "measuredAt", "BP upper", "BP lower", "pulse", "body weight", "location", "memo"))
            val formatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault())

            items.forEach { item ->
                val data = arrayOf(
                    item.id.toString(),
                    formatter.format(item.measuredAt),
                    item.bpUpper.toString(),
                    item.bpLower.toString(),
                    item.pulse.toString(),
                    item.bodyWeight.toString(),
                    item.location,
                    item.memo,
                )
                csvWriter.writeNext(data)
            }
        }
    }
}
////////////////
class ImportDataUseCase(
    @ApplicationContext private val context: Context,
    private val itemRepository: ItemRepository
) {
    suspend operator fun invoke(uri: Uri): Result<String> = runCatching {
        Log.d("import data", uri.toString())

        withContext(Dispatchers.IO) {
            val contentResolver = context.contentResolver
            contentResolver.openInputStream(uri)?.use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    val importedItems = readCsv(reader)
                    itemRepository.replaceAllItems(importedItems)
                }
            }
        }
        ""
    }
    private fun readCsv(reader: Reader): List<Item>{
        val importedItems = mutableListOf<Item>()
        CSVReader(reader).use { csvReader ->
            csvReader.readAll().drop(1).forEachIndexed { index, columns ->
                try {
                    val item = Item(measuredAt = Instant.parse(columns[1])
                        .atOffset(ZoneOffset.UTC)
                        .toInstant(),
                        bpUpper = columns[2].toIntOrNull(),
                        bpLower = columns[3].toIntOrNull(),
                        pulse = columns[4].toIntOrNull(),
                        bodyWeight = columns[5].toFloat(),
                        location = columns[6],
                        memo = columns[7]
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
