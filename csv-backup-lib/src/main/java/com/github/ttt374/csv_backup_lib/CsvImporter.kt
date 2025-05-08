package com.github.ttt374.csv_backup_lib

import com.opencsv.CSVReader
import java.io.Reader


class CsvImporter<T>(private val fields: List<CsvField<T>>, private val createItem: () -> T) {
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
                var item = createItem()
                var hasMissingRequiredField = false
                for ((field, idx) in fieldMap) {
                    val value = line.getOrNull(idx)
                    if (field.isRequired && value.isNullOrEmpty()) {
                        //logger.e("csv reader", "Missing required field '${field.fieldName}' in line $rowIndex")
                        hasMissingRequiredField = true
                        break
                    }
                    try {
                        item = field.parse(value.orEmpty(), item)
                    } catch (err: IllegalStateException){
                        if (field.isRequired){
                            hasMissingRequiredField = true // TODO:
                        }
                    }
                }
                if (!hasMissingRequiredField) {
                    items.add(item)  // 直接 Item (T) を追加
                }
                line = csvReader.readNext()
                rowIndex++
            }
        }
        return items
    }
}
