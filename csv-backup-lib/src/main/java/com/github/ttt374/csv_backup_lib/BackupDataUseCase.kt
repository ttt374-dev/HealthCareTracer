package com.github.ttt374.csv_backup_lib

import android.net.Uri
import android.util.Log
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.io.OutputStreamWriter


/////////////////////////////////

class ExportDataUseCase<T> @Inject constructor(
    private val dataProvider: suspend () -> List<T>,
    private val csvExporter: CsvExporter<T>,
    private val contentResolverWrapper: ContentResolverWrapper
) {
    suspend operator fun invoke(uri: Uri): Result<Unit> = runCatching {
        val items = dataProvider()
        withContext(Dispatchers.IO) {
            contentResolverWrapper.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    csvExporter.export(writer, items)
                }
            }
        }
    }
}
////////////////
class ImportDataUseCase<T> @Inject constructor(
    private val dataSaver: suspend (List<T>) -> Unit,
    private val csvImporter: CsvImporter<T>,
    private val contentResolverWrapper: ContentResolverWrapper,
){
    suspend operator fun invoke(uri: Uri): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            contentResolverWrapper.openInputStream(uri)?.use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    val importedItems = csvImporter.import(reader)
                    dataSaver(importedItems)
                }
            }
        }
    }
}
