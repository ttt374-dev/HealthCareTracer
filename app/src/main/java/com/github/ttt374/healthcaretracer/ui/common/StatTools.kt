package com.github.ttt374.healthcaretracer.ui.common

import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressure

fun <T: Number> List<T?>.averageOrNull(): Double? {
    val filtered = this.filterNotNull()
    return if (filtered.isEmpty()) null else filtered.map { it.toDouble() }.average()
}



@Suppress("UNCHECKED_CAST")
fun <E> List<E>.averageOrNull(): E? {
    val nonNullList = this.filterNotNull()
    if (nonNullList.isEmpty()) return null

    return when {
        nonNullList.all { it is Number } -> {
            if (nonNullList.isEmpty()) return null
            val avg = (nonNullList as List<Number>).map { it.toDouble() }.average()
            avg as E
        }

        nonNullList.all { it is BloodPressure } -> {
            if (isEmpty()) return null
            val bpList = this as List<BloodPressure>
            val avgUpper = bpList.map { it.upper }.filterNotNull().average()
            val avgLower = bpList.map { it.lower }.filterNotNull().average()
            BloodPressure(avgUpper.toInt(), avgLower.toInt()) as E
        }
//        nonNullList.all { it is Double } -> {
//            null
//        }
        //else -> { null }
        else -> throw IllegalArgumentException("Unsupported type for averageOrNull: ${firstOrNull()?.let { it::class.simpleName }}")
    }
}

@Suppress("UNCHECKED_CAST")
fun <E> List<E>.minOrNull(): E? {
    val nonNullList = this.filterNotNull()
    return when {
        nonNullList.all { it is Number } -> {
            if (isEmpty()) return null
            val min = (this as List<Number>).minOf { it.toDouble() }
            min as E
        }

        nonNullList.all { it is BloodPressure } -> {
            if (isEmpty()) return null
            val minBp = (this as List<BloodPressure>).minWithOrNull(
                compareBy<BloodPressure> { it.upper }.thenBy { it.lower }
            )
            minBp as E
        }
        else -> { null }
        //else -> throw IllegalArgumentException("Unsupported type for minOrNull: ${firstOrNull()?.let { it::class.simpleName }}")
    }
}
@Suppress("UNCHECKED_CAST")
fun <E> List<E>.maxOrNull(): E? {
    val nonNullList = this.filterNotNull()
    return when {
        nonNullList.all { it is Number } -> {
            if (isEmpty()) return null
            val max = (this as List<Number>).maxOf { it.toDouble() }
            max as E
        }

        nonNullList.all { it is BloodPressure } -> {
            if (isEmpty()) return null
            val maxBp = (this as List<BloodPressure>).maxWithOrNull(
                compareBy<BloodPressure> { it.upper }.thenBy { it.lower }
            )
            maxBp as E
        }
        else -> { null }
        //else -> throw IllegalArgumentException("Unsupported type for maxOrNull: ${firstOrNull()?.let { it::class.simpleName }}")
    }
}
