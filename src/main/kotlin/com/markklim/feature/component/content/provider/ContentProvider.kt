package com.markklim.feature.component.content.provider

interface ContentProvider {
    fun getContent(location: String, fileType: String): List<ByteArray>
}