package com.github.ttt374.healthcaretracer.data.backup

import com.github.ttt374.healthcaretracer.data.item.Item
import com.opencsv.CSVWriter
import java.io.Writer

class CsvExporter (private val schema: ItemCsvSchema) {
    fun export(writer: Writer, items: List<Item>) {
        CSVWriter(writer).use { csvWriter ->
            val headers = schema.fields.map { it.fieldName }
            csvWriter.writeNext(headers.toTypedArray())

            for (item in items) {
                val row = schema.fields.map { it.format(item) }
                csvWriter.writeNext(row.toTypedArray())
            }
        }
    }
}
