package com.go.feature.service.index

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.go.feature.controller.dto.featuretoggle.FeatureToggleRequest
import com.go.feature.persistence.entity.Feature
import com.go.feature.persistence.entity.Filter
import com.go.feature.persistence.entity.IndexVersion
import com.go.feature.persistence.entity.Namespace
import mu.KLogging
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.LongField
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.Term
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.TermQuery
import org.apache.lucene.store.ByteBuffersDirectory
import org.apache.lucene.store.Directory
import org.springframework.stereotype.Service

@Service
class IndexService(
    val objectMapper: ObjectMapper
) {
    private val namespaceIdToIndexMapper: MutableMap<String, IndexStorage> = mutableMapOf()

    fun getFeaturesFromIndex(namespaceId: String, data: List<FeatureToggleRequest.DataItem>): List<String> {
        val storage: IndexStorage = namespaceIdToIndexMapper[namespaceId]
            ?: return emptyList()

        val parameterToDataMapper: Map<String, FeatureToggleRequest.DataItem> = data.associateBy { it.parameter }

        val mainQuery: BooleanQuery.Builder = BooleanQuery.Builder()

        storage.indexFilters.forEach {
            val value: String? = parameterToDataMapper[it.parameter]?.value

            // TODO: refactor
            when (it.operator) {
                Filter.Operator.EQ, Filter.Operator.LIST_EQ -> {
                    mainQuery.add(composeStringEqFilter(it.column, value))
                }
                Filter.Operator.MORE -> {
                    val longValue: Long? = try {
                        value?.toLong()
                    } catch (e: NumberFormatException) {
                        throw IllegalArgumentException("Value is not compatible with filter type; filter=${it.column}, value=${value}")
                    }

                    mainQuery.add(composeNumberMoreFilter(it.column, longValue))
                }
            }
        }

        return storage.indexSearcher
            .search(mainQuery.build(), 1000) // TODO: return more than 1000
            .scoreDocs
            .map {
                storage.indexSearcher.storedFields()
                    .document(it.doc)
                    .getField(FT_INDEX)
                    .stringValue()
            }
    }

    fun createIndex(index: IndexVersion, namespace: Namespace, filters: List<Filter>, features: List<Feature>) {
        if (filters.isEmpty() || features.isEmpty()) {
            logger.debug("${LOG_PREFIX} Filters or features are empty for namespace=${namespace.name}")
            return
        }

        val memoryIndex: Directory = ByteBuffersDirectory()
        val writer = IndexWriter(memoryIndex, IndexWriterConfig(StandardAnalyzer()))

        features.forEach { feature: Feature ->
            val document = Document()
            document.add(StringField(FT_INDEX, feature.name, Field.Store.YES))


            val featureFilterIdToFilterMap: Map<String, Feature.Filter> =
                objectMapper.readValue<List<Feature.Filter>>(feature.filters)
                    .associateBy { it.id }

            filters.forEach { filter: Filter ->
                val fieldName = "${filter.parameter}_${filter.operator}"
                val value: String? = featureFilterIdToFilterMap[filter.id]?.value

                //TODO: refactor
                when (filter.operator) {
                    Filter.Operator.EQ -> {
                        if (value != null) {
                            document.add(StringField(fieldName, value, Field.Store.NO))
                        } else {
                            document.add(
                                StringField(fieldName, FILTER_DISABLED_VALUE, Field.Store.NO)
                            )
                        }
                    }
                    Filter.Operator.LIST_EQ -> {
                        if (value != null) {
                            document.add(TextField(fieldName, value, Field.Store.NO))
                        } else {
                            document.add(
                                TextField(fieldName, FILTER_DISABLED_VALUE, Field.Store.NO)
                            )
                        }
                    }
                    Filter.Operator.MORE -> {
                        if (value != null) {
                            val longValue: Long = try {
                                value.toLong()
                            } catch (e: NumberFormatException) {
                                throw IllegalArgumentException("Value is not compatible with filter type; filter=${filter.name}, value=${value}")
                            }

                            document.add(LongField(fieldName, longValue))
                        } else {
                            document.add(LongField(fieldName, Long.MAX_VALUE))
                        }
                    }
                }
            }

            writer.addDocument(document)
        }

        writer.close()

        namespaceIdToIndexMapper[namespace.id] = IndexStorage(
            internalIndexVersion = index.indexVersionValue,
            indexFilters = filters.map {
                IndexFilter(
                    column = "${it.parameter}_${it.operator}",
                    parameter = it.parameter,
                    operator = it.operator,
                )
            },
            indexSearcher = IndexSearcher(DirectoryReader.open(memoryIndex))
        )
    }

    fun getInternalIndexVersion(namespaceId: String): String? {
        return namespaceIdToIndexMapper[namespaceId]?.internalIndexVersion
    }

    private data class IndexStorage(
        val internalIndexVersion: String,
        val indexFilters: List<IndexFilter>,
        val indexSearcher: IndexSearcher
    )

    private data class IndexFilter(
        val column: String,
        val parameter: String,
        val operator: Filter.Operator,
    )


    // TODO: refactor
    private fun composeStringStrictEqFilter(field: String, value: String): BooleanClause {
        return BooleanClause(
            TermQuery(Term(field, value)),
            BooleanClause.Occur.MUST
        )
    }

    private fun composeStringEqFilter(field: String, value: String?): BooleanClause {
        return if (value == null) {
            BooleanClause(
                TermQuery(Term(field, FILTER_DISABLED_VALUE)),
                BooleanClause.Occur.MUST
            )
        } else {
            BooleanClause(
                BooleanQuery.Builder()
                    .add(TermQuery(Term(field, value)), BooleanClause.Occur.SHOULD)
                    .add(TermQuery(Term(field, FILTER_DISABLED_VALUE)), BooleanClause.Occur.SHOULD)
                    .build(),
                BooleanClause.Occur.MUST
            )
        }
    }

    private fun composeNumberMoreFilter(field: String, value: Long?): BooleanClause {
        return if (value == null) {
            BooleanClause(
                LongField.newExactQuery(field, Long.MAX_VALUE),
                BooleanClause.Occur.MUST
            )
        } else {
            BooleanClause(
                BooleanQuery.Builder()
                    .add(LongField.newRangeQuery(field, Long.MIN_VALUE, value), BooleanClause.Occur.SHOULD)
                    .add(LongField.newExactQuery(field, Long.MAX_VALUE), BooleanClause.Occur.SHOULD)
                    .build(),
                BooleanClause.Occur.MUST
            )
        }
    }


    private companion object : KLogging() {
        const val LOG_PREFIX = "INDEX_LOADER:"

        const val FT_INDEX = "ft"

        const val FILTER_DISABLED_VALUE = "disfft"
    }
}