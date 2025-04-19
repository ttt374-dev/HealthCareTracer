package com.github.ttt374.healthcaretracer.ui.common

fun <T: Number> List<T?>.averageOrNull(): Double? {
    val filtered = this.filterNotNull()
    return if (filtered.isEmpty()) null else filtered.map { it.toDouble() }.average()
}
