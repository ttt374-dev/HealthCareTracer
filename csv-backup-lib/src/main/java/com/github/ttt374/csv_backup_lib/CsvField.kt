package com.github.ttt374.csv_backup_lib


data class CsvField<T>(
    val fieldName: String,
    val isRequired: Boolean = false,
    val format: (T) -> String,
    val parse: (String, T) -> T
)

