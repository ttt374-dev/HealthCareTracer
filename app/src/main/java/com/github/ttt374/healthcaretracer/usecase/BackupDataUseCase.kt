package com.github.ttt374.healthcaretracer.usecase

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.github.ttt374.healthcaretracer.data.item.Item
import com.github.ttt374.healthcaretracer.data.item.ItemRepository
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


class ExportDataUseCase(private val itemRepository: ItemRepository) {
    suspend operator fun invoke(filename: String? = null): Result<String> = runCatching {
        val downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val filenameFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm").withZone(ZoneId.systemDefault())
        val defaultFilename = "items-${filenameFormatter.format(Instant.now())}.csv"

        val finalFilename = filename?.let {
            if (it.endsWith(".csv")) it else "$it.csv"
        } ?: defaultFilename

        val file = File(downloadFolder, finalFilename)
        Log.d("download CSV to", file.absolutePath)

        val items = itemRepository.getAllItemsFlow().firstOrNull()
        withContext(Dispatchers.IO) {
            CSVWriter(FileWriter(file)).use { writer ->
                writer.writeNext(arrayOf("id", "measuredAt", "BP upper", "BP lower", "pulse", "body weight", "location", "memo"))
                val formatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault())

                items?.forEach { item ->
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
                    writer.writeNext(data)
                }
            }
        }
        "download to CSV done"
    }.onFailure { e -> Log.e("download csv usecase", e.toString()) }
}
////////////////
class ImportDataUseCase(
    @ApplicationContext private val context: Context,
    private val itemRepository: ItemRepository
) {
    suspend operator fun invoke(uri: Uri): Result<String> = runCatching {
        Log.d("import data", uri.toString())

        withContext(Dispatchers.IO) {
            val importedItems = mutableListOf<Item>()
            val contentResolver = context.contentResolver
            contentResolver.openInputStream(uri)?.use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    CSVReader(reader).use { csvReader ->
                        csvReader.readAll().drop(1).mapNotNull { columns ->
                            try {
                                Log.d("columns", columns.toString())
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
                                Log.d("parsed item", item.toString())
                            } catch (e: Exception){
                                Log.e("parse csv", e.message.toString())
                            }
                        }
                    }
                }
            }
            //ExportDataUseCase(itemRepository).invoke() // 一旦バックアップを取る
            itemRepository.replaceAllItems(importedItems)
        }
        ""
    }
}