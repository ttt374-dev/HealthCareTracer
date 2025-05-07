package com.github.ttt374.healthcaretracer.data.backup

import com.github.ttt374.healthcaretracer.data.item.Item
import java.time.Instant
import java.time.format.DateTimeParseException

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
data class CsvField<T>(
    val fieldName: String,
    val isRequired: Boolean = false,
    val format: (T) -> String,
    val parse: (String, T) -> T
)

object ItemCsvSchema {
    var fields = listOf(
        //CsvField<Item, CsvItemPartial>(
        CsvField<Item>(
            fieldName = "Measured at",
            isRequired = true,
            format = { it.measuredAt.toString() },
            parse = { value, item -> item.copy(measuredAt = value.toInstant()) }  // TODO
            //parse = { str -> { it.update("Measured at", str)}}
            //parse = { str -> { it.copy(measuredAt = str.toInstantOrNull())} }
        ),
        CsvField(
            fieldName = "Bp upper",
            isRequired = true,
            format = { it.vitals.bp?.upper?.toString().orEmpty() },
            parse = { str, item -> val vitals = item.vitals.copy(bp = item.vitals.bp?.copy(upper = str.toInt()));  item.copy(vitals = vitals)}
            //parse = { str -> { it.update("Bp upper", str)}}
            //parse = { str -> { it.copy(bpUpper = str.toIntOrNull()) } }
        ),
        CsvField(
            fieldName = "Bp lower",
            isRequired = true,
            format = { it.vitals.bp?.lower?.toString().orEmpty() },
            parse = { str, item -> val vitals = item.vitals.copy(bp = item.vitals.bp?.copy(lower = str.toInt()));  item.copy(vitals = vitals)}
            //parse = { str -> { it.update("Bp lower", str)}}
            //parse = { str -> { it.copy(bpLower = str.toIntOrNull()) } }
        ),
        CsvField(
            fieldName = "Pulse",
            format = { it.vitals.pulse?.toString().orEmpty() },
            parse = { str, item -> val vitals = item.vitals.copy(pulse = str.toInt());  item.copy(vitals = vitals)}
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
//interface CsvPartial<T> {
//    fun toItem(): T
//    //fun update(field: CsvField<T, out CsvPartial<T>>, value: String): CsvPartial<T>
//    fun update(fieldName: String, value: String) : CsvPartial<T>
//}
//
//data class CsvItemPartial(
//    val measuredAt: Instant? = null,
//    val bpUpper: Int? = null,
//    val bpLower: Int? = null,
//    val pulse: Int? = null,
//    val bodyWeight: Double? = null,
//    val bodyTemperature: Double? = null,
//    val location: String = "",
//    val memo: String = ""
//): CsvPartial<Item>  {
//    override fun toItem(): Item = Item(
//        measuredAt = measuredAt ?: Instant.EPOCH,
//        vitals = Vitals(
//            bp = (bpUpper to bpLower).toBloodPressure(),
//            pulse = pulse,
//            bodyWeight = bodyWeight,
//            bodyTemperature = bodyTemperature
//        ),
//        location = location,
//        memo = memo
//    )
//
//    //override fun update(field: CsvField<Item, out CsvPartial<Item>>, value: String): CsvPartial<Item> {
//    override fun update(fieldName: String, value: String): CsvPartial<Item> {
//        return when (fieldName) {
//            "Measured at" -> copy(measuredAt = value.toInstantOrNull())
//            "Bp upper" -> copy(bpUpper = value.toIntOrNull())
//            "Bp lower" -> copy(bpLower = value.toIntOrNull())
//            "Pulse" -> copy(pulse = value.toIntOrNull())
//            "Body weight" -> copy(bodyWeight = value.toDoubleOrNull())
//            "Body Temperature" -> copy(bodyTemperature = value.toDoubleOrNull())
//            else -> this // デフォルトはそのまま  // TODO
//        }
//    }
//}

//data class CsvField<T, P>(
//    val fieldName: String,
//    val isRequired: Boolean = false,
//    val format: (T) -> String,
//    val parse: (String) -> (P) -> P
//)

//data class CsvField(
//    val fieldName: String,
//    val isRequired: Boolean = false,
//    val format: (Item) -> String,
//    val parse: (String) -> (CsvItemPartial) -> CsvItemPartial
//)

//fun CsvItemPartial.toItem(): Item = Item(
//    measuredAt = measuredAt ?: Instant.EPOCH,
//    vitals = Vitals(
//        bp = (bpUpper to bpLower).toBloodPressure(),
//        pulse = pulse,
//        bodyWeight = bodyWeight,
//        bodyTemperature = bodyTemperature
//    ),
//    location = location,
//    memo = memo
//)


//enum class CsvField(
//    val isRequired: Boolean = false,
//    val format: (Item) -> String,
//    val parse: (String) -> (CsvItemPartial) -> CsvItemPartial,
//    val specificFieldName: String? = null,
//) {
//    MEASURED_AT(
//        isRequired = true,
//        specificFieldName = "Measured at",
//        format = { it.measuredAt.toString() },
//        parse = { str -> { it.copy(measuredAt = str.toInstantOrNull()) } }
//    ),
//    BP_UPPER(
//        isRequired = true,
//        specificFieldName = "Bp upper",
//        format = { it.vitals.bp?.upper?.toString().orEmpty() },
//        parse = { str -> { it.copy(bpUpper = str.toIntOrNull()) } }
//    ),
//    BP_LOWER(
//        isRequired = true,
//        specificFieldName = "Bp lower",
//        format = { it.vitals.bp?.lower?.toString().orEmpty() },
//        parse = { str -> { it.copy(bpLower = str.toIntOrNull()) } }
//    ),
//    PULSE(
//        specificFieldName = "Pulse",
//        format = { it.vitals.pulse?.toString().orEmpty() },
//        parse = { str -> { it.copy(pulse = str.toIntOrNull()) } }
//    ),
//    BODY_WEIGHT(
//        specificFieldName = "Body weight",
//        format = { it.vitals.bodyWeight?.toString().orEmpty() },
//        parse = { str -> { it.copy(bodyWeight = str.toDoubleOrNull()) } }
//    ),
//    BODY_TEMPERATURE(
//        specificFieldName = "Body temperature",
//        format = { it.vitals.bodyTemperature?.toString().orEmpty() },
//        parse = { str -> { it.copy(bodyTemperature = str.toDoubleOrNull()) } }
//    ),
//    LOCATION(
//        specificFieldName = "Location",
//        format = { it.location },
//        parse = { str -> { it.copy(location = str) } }
//    ),
//    MEMO(
//        specificFieldName = "Memo",
//        format = { it.memo },
//        parse = { str -> { it.copy(memo = str) } }
//    );
//
//    val fieldName: String = this.specificFieldName ?: name.replace('_', ' ').lowercase().replaceFirstChar(Char::uppercase)
//}
