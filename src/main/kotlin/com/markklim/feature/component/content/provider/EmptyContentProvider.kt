package com.markklim.feature.component.content.provider

class EmptyContentProvider : ContentProvider {

    override fun getContent(location: String, fileType: String): List<ByteArray> = emptyList()
}