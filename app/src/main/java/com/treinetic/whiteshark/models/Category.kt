package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.treinetic.whiteshark.network.Net
@Keep
data class Category(

    @SerializedName("id")
    var id: String? = null,

    @SerializedName("category")
    var category: String? = null,

    @SerializedName("url")
    var url: String? = null,

    @SerializedName("books")
    var books: Books? = null,

    var showBackground: Boolean = false
) {
    @SerializedName("books_count")
    var booksCount: Int = 0

    fun setBooksUrl() {
        url = Net.URL.EPUB + "categoryId= $id"
    }

    fun isCategoryFilled(): Boolean {
        return books != null
    }


    fun fill(category: Category) {
        books = category.books
        showBackground = category.showBackground
        url = category.url

    }
}





