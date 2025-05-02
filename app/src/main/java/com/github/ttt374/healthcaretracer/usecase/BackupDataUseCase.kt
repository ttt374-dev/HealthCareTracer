package com.github.ttt374.healthcaretracer.usecase

import android.net.Uri
import android.util.Log
import com.github.ttt374.healthcaretracer.data.backup.ContentResolverWrapper
import com.github.ttt374.healthcaretracer.data.backup.CsvExporter
import com.github.ttt374.healthcaretracer.data.backup.CsvField
import com.github.ttt374.healthcaretracer.data.backup.CsvImporter
import com.github.ttt374.healthcaretracer.data.backup.CsvItemPartial
import com.github.ttt374.healthcaretracer.data.backup.toItem
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

class ExportDataUseCase @Inject constructor(private val itemRepository: ItemRepository,
                                            private val csvExporter: CsvExporter,
                                            private val contentResolverWrapper: ContentResolverWrapper,) {
    suspend operator fun invoke(uri: Uri): Result<String> = runCatching {
        val items = itemRepository.getAllItems()  // .firstOrNull() ?: emptyList()

        withContext(Dispatchers.IO) {
            contentResolverWrapper.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    csvExporter.export(writer, items)
                }
            }
        }
        "download to CSV done"
    }.onFailure { e -> Log.e("ExportDataUseCase", "CSV export failed", e) }
}
////////////////
class ImportDataUseCase @Inject constructor(private val itemRepository: ItemRepository,
                                            private val csvImporter: CsvImporter,
                                            private val contentResolverWrapper: ContentResolverWrapper){
    suspend operator fun invoke(uri: Uri): Result<String> = runCatching {
        withContext(Dispatchers.IO) {
            contentResolverWrapper.openInputStream(uri)?.use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    val importedItems = csvImporter.import(reader)
                    itemRepository.replaceAllItems(importedItems)
                }
            }
        }
        "Import successful"
    }.onFailure { e -> Log.e("ImportDataUseCase", "CSV import failed", e) }
}
