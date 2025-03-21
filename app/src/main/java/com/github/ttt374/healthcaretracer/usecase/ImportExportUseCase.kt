package com.github.ttt374.healthcaretracer.usecase

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.ttt374.healthcaretracer.data.Item
import com.github.ttt374.healthcaretracer.data.ItemRepository
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.InputStreamReader
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


class ExportDataUseCase(private val itemRepository: ItemRepository) {
    suspend operator fun invoke(): Result<String> = runCatching {
        val downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val filenameFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_hh_mm").withZone(ZoneId.systemDefault())
        val file = File(downloadFolder, "items-${filenameFormatter.format(Instant.now())}.csv")
        Log.d("download CSV to", file.absolutePath)

        val items = itemRepository.retrieveItemsFlow().firstOrNull()
        withContext(Dispatchers.IO) {
            CSVWriter(FileWriter(file)).use { writer ->
                writer.writeNext(arrayOf("id", "measuredAt", "high BP", "low BP", "pulse"))
                val formatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault())

                items?.forEach { item ->
                    val data = arrayOf(
                            item.id.toString(),
                            formatter.format(item.measuredAt),
                            item.bpHigh.toString(),
                            item.bpLow.toString(),
                            item.pulse.toString(),
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
    private val itemRepository: ItemRepository) {
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
                                    bpHigh = columns[2].toInt(),
                                    bpLow = columns[3].toInt(),
                                    pulse = columns[4].toInt()
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