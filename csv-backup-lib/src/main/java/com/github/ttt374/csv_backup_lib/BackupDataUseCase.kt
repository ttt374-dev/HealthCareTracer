package com.github.ttt374.csv_backup_lib

import android.net.Uri
import android.util.Log
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.io.OutputStreamWriter


/////////////////////////////////

class ExportDataUseCase<T> @Inject constructor(//private val itemRepository: ItemRepository,
    private val dataProvider: suspend () -> List<T>,
    private val csvExporter: CsvExporter<T>,
    private val contentResolverWrapper: ContentResolverWrapper
) {
    suspend operator fun invoke(uri: Uri): Result<String> = runCatching {
        //val items = itemRepository.getAllItems()  // .firstOrNull() ?: emptyList()
        val items = dataProvider()
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
class ImportDataUseCase<T> @Inject constructor(//private val itemRepository: ItemRepository,
    private val dataSaver: suspend (List<T>) -> Unit,
    private val csvImporter: CsvImporter<T>,
    private val contentResolverWrapper: ContentResolverWrapper
){
    suspend operator fun invoke(uri: Uri): Result<String> = runCatching {
        withContext(Dispatchers.IO) {
            contentResolverWrapper.openInputStream(uri)?.use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    val importedItems = csvImporter.import(reader)
                    dataSaver(importedItems)
                    //itemRepository.replaceAllItems(importedItems)
                }
            }

        }
        "Import successful"
    }.onFailure { e -> Log.e("ImportDataUseCase", "CSV import failed", e) }
}
