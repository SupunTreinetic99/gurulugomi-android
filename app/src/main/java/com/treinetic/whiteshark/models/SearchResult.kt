package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Nuwan on 2/18/19.
 */
@Keep
data class SearchResult(
    @SerializedName("epubs")
    var epubs: Books,
    @SerializedName("authors")
    var authors: Authors,
    @SerializedName("publisher")
    var publishers: Publishers,
    @SerializedName("promotions")
    var promotions: Books

) {


    fun hasPublishers(): Boolean {
        return publishers != null && publishers.data != null && publishers.data.isNotEmpty()
    }

    fun hasEpubs(): Boolean {
        return hasBooks(epubs)
    }

    fun hasPromotions(): Boolean {
        return hasBooks(promotions)
    }

    private fun hasBooks(books: Books): Boolean {
        return books != null && books.data.isNotEmpty()
    }

    fun hasAuthors(): Boolean {
        return authors != null && authors.data != null && authors.data.isNotEmpty()
    }

    fun hasSearResults(): Boolean {
        return hasAuthors() || hasEpubs() || hasPromotions() || hasPublishers()
    }

}