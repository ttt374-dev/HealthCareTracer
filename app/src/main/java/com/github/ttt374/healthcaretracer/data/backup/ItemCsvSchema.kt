package com.github.ttt374.healthcaretracer.data.backup

import com.github.ttt374.csv_backup_lib.CsvField
import com.github.ttt374.healthcaretracer.data.item.Item
import java.time.Instant
import java.time.format.DateTimeParseException

object ItemCsvSchema {
    var fields = listOf(
        //CsvField<Item, CsvItemPartial>(
        CsvField<Item>(
            fieldName = "Measured at",
            isRequired = true,
            format = { it.measuredAt.toString() },
            parse = { value, item -> item.copy(measuredAt = value.toInstantOrNull() ?: error("invalid date format: $value")) }  // TODO
            //parse = { str -> { it.update("Measured at", str)}}
            //parse = { str -> { it.copy(measuredAt = str.toInstantOrNull())} }
        ),
        CsvField(
            fieldName = "Bp upper",
            isRequired = true,
            format = { it.vitals.bp?.upper?.toString().orEmpty() },
            parse = { str, item -> item.copy(vitals = item.vitals.copy(bp = item.vitals.bp?.copy(upper = str.toIntOrNull()))) }
            //parse = { str, item -> val vitals = item.vitals.copy(bp = item.vitals.bp?.copy(upper = str.toIntOrNull() ?: error("invalid bp value: $str")));  item.copy(vitals = vitals)}
            //parse = { str -> { it.update("Bp upper", str)}}
            //parse = { str -> { it.copy(bpUpper = str.toIntOrNull()) } }
        ),
        CsvField(
            fieldName = "Bp lower",
            isRequired = true,
            format = { it.vitals.bp?.lower?.toString().orEmpty() },
            parse = { str, item -> item.copy(vitals = item.vitals.copy(bp = item.vitals.bp?.copy(lower = str.toIntOrNull()))) }
            //parse = { str, item -> val vitals = item.vitals.copy(bp = item.vitals.bp?.copy(lower = str.toIntOrNull() ?: error("invalid bp value: $str")));  item.copy(vitals = vitals)}
            //parse = { str -> { it.update("Bp lower", str)}}
            //parse = { str -> { it.copy(bpLower = str.toIntOrNull()) } }
        ),
        CsvField(
            fieldName = "Pulse",
            format = { it.vitals.pulse?.toString().orEmpty() },
            parse = { str, item -> item.copy(vitals = item.vitals.copy(pulse = str.toIntOrNull())) }
            //parse = { str -> { it.copy(pulse = str.toIntOrNull()) } }
            //parse = { str -> { it.update("Pulse", str)}}
        ),
        CsvField(
            fieldName = "Body weight",
            format = { it.vitals.bodyWeight?.toString().orEmpty() },
            parse = { str, item -> item.copy(vitals = item.vitals.copy(bodyWeight = str.toDoubleOrNull()))},
            //parse = { str -> { it.copy(bodyWeight = str.toDoubleOrNull()) } }
            //parse = { str -> { it.update("Body weight", str)}}
        ),
        CsvField(
            fieldName = "Body temperature",
            format = { it.vitals.bodyTemperature?.toString().orEmpty() },
            parse = { str, item -> item.copy(vitals = item.vitals.copy(bodyTemperature = str.toDoubleOrNull()))},
            //parse = { str -> { it.copy(bodyTemperature = str.toDoubleOrNull()) } },
            //parse = { str -> { it.update("Body temperature", str)}}
        ),
        CsvField(
            fieldName = "Location",
            format = { it.location },
            parse = { str, item -> item.copy(location = str)},
            //parse = { str -> { it.copy(location = str) } }
            //parse = { str -> { it.update("Location", str)}}
        ),
        CsvField(
            fieldName = "Memo",
            format = { it.memo },
            parse = { str, item -> item.copy(memo = str)},
            //parse = { str -> { it.copy(memo = str) } }
            //parse = { str -> { it.update("Memo", str)}}
        )
    )
}

fun String.toInstant(): Instant {
    return Instant.parse(this)
}

fun String.toInstantOrNull(): Instant? {
    return try {
        Instant.parse(this)  // ISO 8601形式の文字列をInstantに変換
    } catch (e: DateTimeParseException) {
        null  // 変換に失敗した場合はnullを返す
    }
}
