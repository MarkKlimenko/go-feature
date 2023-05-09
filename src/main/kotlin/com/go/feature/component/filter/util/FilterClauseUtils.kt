package com.go.feature.component.filter.util

import org.apache.lucene.document.DoubleField
import org.apache.lucene.index.Term
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.TermQuery

fun composeStringEqFilter(field: String, value: String?): BooleanClause {
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

fun composeNumberMoreFilter(field: String, value: Double?): BooleanClause {
    return if (value == null) {
        BooleanClause(
            DoubleField.newExactQuery(field, Double.MAX_VALUE),
            BooleanClause.Occur.MUST
        )
    } else {
        BooleanClause(
            BooleanQuery.Builder()
                .add(DoubleField.newRangeQuery(field, Double.MIN_VALUE, value), BooleanClause.Occur.SHOULD)
                .add(DoubleField.newExactQuery(field, Double.MAX_VALUE), BooleanClause.Occur.SHOULD)
                .build(),
            BooleanClause.Occur.MUST
        )
    }
}

fun composeNumberLessFilter(field: String, value: Double?): BooleanClause {
    return if (value == null) {
        BooleanClause(
            DoubleField.newExactQuery(field, Double.MIN_VALUE),
            BooleanClause.Occur.MUST
        )
    } else {
        BooleanClause(
            BooleanQuery.Builder()
                .add(DoubleField.newRangeQuery(field, value, Double.MAX_VALUE), BooleanClause.Occur.SHOULD)
                .add(DoubleField.newExactQuery(field, Double.MIN_VALUE), BooleanClause.Occur.SHOULD)
                .build(),
            BooleanClause.Occur.MUST
        )
    }
}