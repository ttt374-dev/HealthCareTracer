package com.github.ttt374.csv_backup_lib

import com.opencsv.CSVWriter
import java.io.Writer

class CsvExporter<T> (private val fields: List<CsvField<T>>) {
    fun export(writer: Writer, items: List<T>) {
        CSVWriter(writer).use { csvWriter ->
            val headers = fields.map { it.fieldName }
            csvWriter.writeNext(headers.toTypedArray())

            for (item in items) {
                val row = fields.map { it.format(item) }
                csvWriter.writeNext(row.toTypedArray())
            }
        }
    }
}
