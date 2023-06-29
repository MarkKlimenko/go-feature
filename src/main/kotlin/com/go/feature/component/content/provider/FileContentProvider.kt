package com.go.feature.component.content.provider

import mu.KLogging
import java.io.File

class FileContentProvider : ContentProvider {
    override fun getContent(location: String, fileType: String): List<ByteArray> {
        return File(location)
            .listFiles { _: File, name: String -> name.endsWith(fileType) }
            ?.map { it.readBytes() }
            ?: let {
                logger.warn("$LOG_PREFIX Settings location not found; location=$location, fileType=$fileType")
                emptyList()
            }
    }

    private companion object : KLogging() {
        const val LOG_PREFIX = "FILE_CONTENT_PROVIDER:"
    }
}