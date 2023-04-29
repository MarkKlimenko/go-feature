package com.go.feature.service.index

import mu.KLogging
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.LongField
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.Term
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.TermQuery
import org.apache.lucene.search.TopDocs
import org.apache.lucene.store.ByteBuffersDirectory
import org.apache.lucene.store.Directory
import java.lang.Long.parseLong

class IndexTest {
    val memoryIndex: Directory = ByteBuffersDirectory()


    fun go() {
        logger.info("start $COUNT")
        generate()
        logger.info("finish indexing")
        find()
        logger.info("finish searching")
    }

    fun find() {
        /*
        17:38:26.758 [main] INFO com.go.feature.service.index.IndexTest - start 100
        17:38:27.118 [main] INFO com.go.feature.service.index.IndexTest - finish indexing
        17:38:29.964 [main] INFO com.go.feature.service.index.IndexTest - finish searching

        17:39:00.836 [main] INFO com.go.feature.service.index.IndexTest - start 1000
        17:39:01.642 [main] INFO com.go.feature.service.index.IndexTest - finish indexing
        17:39:05.071 [main] INFO com.go.feature.service.index.IndexTest - finish searching

        17:39:34.947 [main] INFO com.go.feature.service.index.IndexTest - start 10000
        17:39:38.696 [main] INFO com.go.feature.service.index.IndexTest - finish indexing
        17:39:42.939 [main] INFO com.go.feature.service.index.IndexTest - finish searching

        17:40:20.858 [main] INFO com.go.feature.service.index.IndexTest - start 100000
        17:40:36.183 [main] INFO com.go.feature.service.index.IndexTest - finish indexing
        17:40:49.627 [main] INFO com.go.feature.service.index.IndexTest - finish searching


        17:23:43.626 [main] INFO com.go.feature.service.index.IndexTest - start 1000000
        17:25:23.921 [main] INFO com.go.feature.service.index.IndexTest - finish indexing
        17:25:39.133 [main] INFO com.go.feature.service.index.IndexTest - finish searching

        17:41:29.633 [main] INFO com.go.feature.service.index.IndexTest - start 1000000 - with user short list - 15
        17:43:22.748 [main] INFO com.go.feature.service.index.IndexTest - finish indexing
        17:43:40.449 [main] INFO com.go.feature.service.index.IndexTest - finish searching

        17:55:40.202 [main] INFO com.go.feature.service.index.IndexTest - start 1000000 - with user long list - 1000
        18:02:49.845 [main] INFO com.go.feature.service.index.IndexTest - finish indexing
        18:03:04.827 [main] INFO com.go.feature.service.index.IndexTest - finish searching
         */

        val indexReader: IndexReader = DirectoryReader.open(memoryIndex)
        val searcher = IndexSearcher(indexReader)

        (1..1000).forEach {
            val documents: MutableList<Document> = findOne("${(1..COUNT).random()}", searcher)
            // logger.debug("documents=${documents}")
        }
    }

    fun findOne(index: String, searcher: IndexSearcher): MutableList<Document> {
        val topDocs: TopDocs = searcher.search(
            composeQuery(index),
            10
        )

        val documents: MutableList<Document> = ArrayList<Document>()
        for (scoreDoc in topDocs.scoreDocs) {
            documents.add(searcher.doc(scoreDoc.doc))
        }

        return documents
    }

    fun composeQuery(index: String): Query {
        val mainQuery: BooleanQuery.Builder = BooleanQuery.Builder()
            .add(composeStringStrictEqFilter("isEnabled", "true"))
            .add(composeStringEqFilter("os_eq", "ios"))

            .add(composeStringEqFilter("user_eqList", "user${index}"))

            .add(composeStringEqFilter("osVersion_eq", index))
            .add(composeNumberMoreFilter("osVersion_more", index))
            .add(composeNumberMoreFilter("osVersionDisabled_more", index))

            .add(composeStringEqFilter("aUser_eq", "true"))
            .add(composeStringEqFilter("bUser_eq", "true"))
            .add(composeStringEqFilter("cUser_eq", "true"))
            .add(composeStringEqFilter("dUser_eq", null))
            .add(composeStringEqFilter("eUser_eq", "false"))
            .add(composeStringEqFilter("fUser_eq", "true"))
            .add(composeStringEqFilter("gUser_eq", "false"))

        (1..50).map { genericIt ->
            mainQuery.add(composeStringEqFilter("genericFt_${genericIt}_eq", "${genericIt}"))
        }

        (1..50).map { genericIt ->
            mainQuery.add(composeStringEqFilter("generic1Ft_${genericIt}_eq", "user${genericIt}${index}"))
        }

        return mainQuery.build()
    }

    fun composeStringStrictEqFilter(field: String, value: String): BooleanClause {
        return BooleanClause(
            TermQuery(Term(field, value)),
            BooleanClause.Occur.MUST
        )
    }

    fun composeStringEqFilter(field: String, value: String?): BooleanClause {
        return if (value == null) {
            return BooleanClause(
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

    fun composeNumberMoreFilter(field: String, value: String): BooleanClause {
        return BooleanClause(
            BooleanQuery.Builder()
                .add(
                    LongField.newRangeQuery(field, Long.MIN_VALUE, parseLong(value)),
                    BooleanClause.Occur.SHOULD
                )
                .add(LongField.newExactQuery(field, Long.MAX_VALUE), BooleanClause.Occur.SHOULD)
                .build(),
            BooleanClause.Occur.MUST
        )
    }

    fun generate() {
        val writter = IndexWriter(memoryIndex, IndexWriterConfig(StandardAnalyzer()))

        (1..COUNT).forEach {
            val document = Document()
            document.add(StringField("ft", "superUser_${it}", Field.Store.YES))
            document.add(StringField("isEnabled", "true", Field.Store.NO))

            val users: String = (1..15).map { userCount ->
                if (userCount == 10) {
                    "user${it}"
                } else {
                    "user${userCount}"
                }
            }.joinToString(" ")

            document.add(TextField("user_eqList", users, Field.Store.NO))

            document.add(StringField("os_eq", "ios", Field.Store.NO))
            document.add(StringField("osVersion_eq", "${it}", Field.Store.NO))
            document.add(LongField("osVersion_more", it.toLong() - 10))
            document.add(LongField("osVersionDisabled_more", Long.MAX_VALUE))

            document.add(StringField("aUser_eq", "true", Field.Store.NO))
            document.add(StringField("bUser_eq", "true", Field.Store.NO))
            document.add(StringField("cUser_eq", "disfft", Field.Store.NO))
            document.add(StringField("dUser_eq", "disfft", Field.Store.NO))
            document.add(StringField("eUser_eq", "disfft", Field.Store.NO))
            document.add(StringField("fUser_eq", "disfft", Field.Store.NO))
            document.add(StringField("gUser_eq", "disfft", Field.Store.NO))

            (1..50).forEach { genericIt ->
                document.add(StringField("genericFt_${genericIt}_eq", "disfft", Field.Store.NO))
            }

            (1..50).forEach { genericIt ->
                document.add(StringField("generic1Ft_${genericIt}_eq", "user${genericIt}${it}", Field.Store.NO))
            }

            document.add(StringField("group_eq", "vip${it}", Field.Store.NO))

            document.add(StringField("isBoss_eq", "disfft", Field.Store.NO))
            writter.addDocument(document)
        }

        writter.close()
    }

    companion object : KLogging() {
        const val FILTER_DISABLED_VALUE = "disfft"
        const val COUNT = 1000000
    }
}