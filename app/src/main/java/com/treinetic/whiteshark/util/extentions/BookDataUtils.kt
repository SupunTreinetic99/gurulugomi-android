package com.treinetic.whiteshark.util.extentions

import com.treinetic.whiteshark.models.Book
import com.treinetic.whiteshark.roomdb.models.BookData

/**
 * Created by Nuwan on 3/7/19.
 */


fun BookData.isSame(book: Book): Boolean {
    return this.id.equals(book.id)
}

