package com.go.feature.test.utils

import org.apache.commons.io.IOUtils
import java.io.InputStream
import java.nio.charset.Charset

fun fileToString(fileName: String): String =
    IOUtils.toString(getFileAsInputStream(fileName), Charset.defaultCharset())

fun getFileAsInputStream(fileName: String): InputStream {
    val path = "/files/$fileName"

    return {}.javaClass.getResourceAsStream(path)
        ?: throw IllegalArgumentException("File not found, check resource=$path")
}
