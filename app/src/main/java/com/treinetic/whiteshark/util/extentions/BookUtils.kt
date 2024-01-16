package com.treinetic.whiteshark.util.extentions

import com.google.gson.Gson
import com.treinetic.whiteshark.models.Book

/**
 * Created by Nuwan on 3/7/19.
 */


fun Book.isSame(book: Book):Boolean{

    return this.id.equals(book)
}

fun Book.toJson(): String? {
    return Gson().toJson(this)
}
