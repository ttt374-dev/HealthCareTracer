package com.github.ttt374.healthcaretracer.data.backup

import com.github.ttt374.healthcaretracer.data.item.Item
import com.github.ttt374.healthcaretracer.shared.Logger
import com.opencsv.CSVReader
import java.io.Reader
class CsvImporter<T, P : CsvPartial<T>>(
    private val logger: Logger,
    val newPartial: () -> P,
    private val fields: List<CsvField<T, P>>
) {
    fun import(reader: Reader): List<T> {
        val items = mutableListOf<T>()
        CSVReader(reader).use { csvReader ->
            val headers = csvReader.readNext()?.map { it.trim() } ?: return emptyList()
            val fieldMap = headers.withIndex().mapNotNull { (i, name) ->
                fields.find { it.fieldName == name }?.let { it to i }
            }.toMap()

            var line = csvReader.readNext()
            var rowIndex = 1
            while (line != null) {
                var partial = newPartial()
                var hasMissingRequiredField = false
                for ((field, idx) in fieldMap) {
                    val value = line.getOrNull(idx)
                    if (field.isRequired && value.isNullOrEmpty()) {
                        logger.e("csv reader", "Missing required field '${field.fieldName}' in line $rowIndex")
                        hasMissingRequiredField = true
                        break
                    }
                    partial = field.parse(value.orEmpty())(partial)
                }
                if (!hasMissingRequiredField) {
                    items.add(partial.toItem())  // toItem()を呼び出す
                }
                line = csvReader.readNext()
                rowIndex++
            }
        }
        return items
    }
}
