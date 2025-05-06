package com.github.ttt374.healthcaretracer.data.backup

import com.github.ttt374.healthcaretracer.data.item.Item
import com.github.ttt374.healthcaretracer.shared.Logger
import com.opencsv.CSVReader
import java.io.Reader

class CsvImporter(private val logger: Logger, private val schema: ItemCsvSchema) {
    fun import(reader: Reader): List<Item> {
        val items = mutableListOf<Item>()
        CSVReader(reader).use { csvReader ->
            val headers = csvReader.readNext()?.map { it.trim() } ?: return emptyList()
            val fieldMap = headers.withIndex().mapNotNull { (i, name) ->
                schema.fields.find { it.fieldName == name }?.let { it to i }
            }.toMap()

            var line = csvReader.readNext()
            var rowIndex = 1
            while (line != null) {
                var partial = CsvItemPartial()
                var hasMissingRequiredField = false
                for ((field, idx) in fieldMap) {
                    val value = line.getOrNull(idx)
                    if (field.isRequired && (value == null || value.isEmpty())) {
                        logger.e("csv reader", "Missing required field '${field.fieldName}' in line $rowIndex")
                        hasMissingRequiredField = true
                        break
                    }
                    partial = field.parse(value.orEmpty())(partial)
                }
                if (!hasMissingRequiredField) {
                    items.add(partial.toItem())
                }
                line = csvReader.readNext()
                rowIndex++
            }
        }
        return items
    }
}
