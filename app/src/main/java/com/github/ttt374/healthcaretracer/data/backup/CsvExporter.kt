package com.github.ttt374.healthcaretracer.data.backup

import com.github.ttt374.healthcaretracer.data.item.Item
import com.opencsv.CSVWriter
import java.io.Writer

class CsvExporter {
    fun export(writer: Writer, items: List<Item>) {
        CSVWriter(writer).use { csvWriter ->
            val headers = CsvField.entries.map { it.fieldName }
            csvWriter.writeNext(headers.toTypedArray())

            for (item in items) {
                val row = CsvField.entries.map { it.format(item) }
                csvWriter.writeNext(row.toTypedArray())
            }
        }
    }
}
