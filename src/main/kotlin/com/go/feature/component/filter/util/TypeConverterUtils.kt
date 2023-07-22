package com.go.feature.component.filter.util

import com.go.feature.util.exception.client.ClientException

fun parseDouble(field: String, value: String): Double {
    return try {
        value.toDouble()
    } catch (e: NumberFormatException) {
        throw ClientException("Value is not compatible with filter; field=$field, value=$value")
    }
}

val VERSION_REGEX: Regex = "(^[0-9]{1,4})(\\.[0-9]{1,4}){0,2}".toRegex()
private const val DEFAULT_VERSION_GROUP_VALUE = "0000"
private const val VERSION_LENGTH = 4
private val DEFAULT_MINOR_PATCH_GROUPS = listOf(DEFAULT_VERSION_GROUP_VALUE, DEFAULT_VERSION_GROUP_VALUE)

fun parseVersion(field: String, value: String): Double {
    if (!value.matches(VERSION_REGEX)) {
        throw ClientException("Version value format exception; field=$field, value=$value")
    }

    val versionList: List<String> = value.split(".")
        .map { it.padStart(VERSION_LENGTH, '0') }
        .toMutableList()
        .let {
            if (it.size == 1) {
                it.addAll(DEFAULT_MINOR_PATCH_GROUPS)
            } else if (it.size == 2) {
                it.add(DEFAULT_VERSION_GROUP_VALUE)
            }
            it
        }

    return try {
        versionList.joinToString("").toDouble()
    } catch (e: NumberFormatException) {
        throw ClientException("Value is not compatible with filter; field=$field, value=$value")
    }
}