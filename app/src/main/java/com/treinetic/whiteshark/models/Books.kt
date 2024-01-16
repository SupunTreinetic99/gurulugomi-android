package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlin.concurrent.thread
@Keep
class Books {

    @SerializedName("current_page")
    var current_page: Int? = 0

    @SerializedName("data")
    var data: MutableList<Book> = mutableListOf()

    @SerializedName("first_page_url")
    var first_page_url: String? = null

    @SerializedName("from")
    var from: Int? = 0

    @SerializedName("last_page")
    var last_page: Int? = 0

    @SerializedName("last_page_url")
    var last_page_url: String? = null

    @SerializedName("next_page_url")
    var next_page_url: String? = null

    @SerializedName("path")
    var path: String? = null

    @SerializedName("per_page")
    var per_page: Int? = 0

    @SerializedName("prev_page_url")
    var prev_page_url: String? = null

    @SerializedName("to")
    var to: Int? = 0

    @SerializedName("total")
    var total: Int? = 0

    init {
        allBooks.add(this)
    }


    companion object {
        var allBooks: ArrayList<Books> = arrayListOf()

        fun updateIsAtWishlist(books: List<Book>, isAtWishlist: Boolean) {
            thread(start = true) {
                books.forEach { b ->
                    allBooks.forEach {
                        it.data.forEach {
                            if (it.id.equals(b.id)) it.isAtWishlist = isAtWishlist
                        }
                    }
                }
            }

        }

        fun updatePurchasedBooks(books: List<Book>, isPurchased: Boolean) {

            thread(start = true) {
                try {
                    books.forEach { b ->
                        allBooks.forEach {
                            it.data?.forEach {
                                if (it.id.equals(b.id)) it.isPurchased = isPurchased
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        }

        fun updateFilledBooksRaings(rating: Rating, isMine: Boolean) {
            thread(start = true) {
                allBooks.forEach {
                    it.data?.forEach {
                        if (it.id.equals(rating.bookId) && it.isFill && isMine) it.ratingOfUser =
                            rating
                    }
                }
            }
        }

    }

}