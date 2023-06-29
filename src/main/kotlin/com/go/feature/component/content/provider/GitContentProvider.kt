package com.go.feature.component.content.provider

import mu.KLogging

// TODO: implement git loader
// git show
class GitContentProvider : ContentProvider {
    override fun getContent(location: String, fileType: String): List<ByteArray> {
        TODO("Not yet implemented")
    }

    private companion object : KLogging() {
        const val LOG_PREFIX = "GIT_CONTENT_PROVIDER:"
    }
}