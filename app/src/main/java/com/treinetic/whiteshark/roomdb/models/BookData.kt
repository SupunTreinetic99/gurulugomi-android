package com.treinetic.whiteshark.roomdb.models

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import com.google.gson.Gson
import com.treinetic.whiteshark.models.Book
import com.treinetic.google.androidx.gms
import com.treinetic.whiteshark.util.extentions.toBook

/**
 * Created by Nuwan on 2/28/19.
 */
@Keep
@Entity(tableName = "Book", primaryKeys = ["id", "userId"])
data class BookData(
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "userId") val userId: String,
    @ColumnInfo(name = "book") var book: String,
    @ColumnInfo(name = "bookmark") var bookmark: Int? = 0,
    @ColumnInfo(name = "key") var key: String
) {
//        key = ""
//        set(value) {
//            field = gms.get().e(value)
//        }
//        get() = gms.get().d(field)


    fun getBookObj(): Book? {
        return book.toBook()
    }

//    fun putKey(value: String) {
//
//    }
//
//    fun getKey() {
//
//    }



    fun setBook(bookObj: Book) {
        val gson = Gson()
        book = gson.toJson(bookObj)
    }
}