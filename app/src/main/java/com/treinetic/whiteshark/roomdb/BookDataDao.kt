package com.treinetic.whiteshark.roomdb

import androidx.annotation.Keep
import androidx.room.*
import com.treinetic.whiteshark.roomdb.models.BookData
import com.treinetic.whiteshark.roomdb.models.CartItemData

/**
 * Created by Nuwan on 2/28/19.
 */
@Keep
@Dao
interface BookDataDao {


    @Query("SELECT * FROM Book WHERE id =(:bookId) AND userId=(:userId) LIMIT 1")
    fun getBookById(bookId: String, userId: String): BookData?

    @Query("SELECT * FROM Book WHERE userId=(:userId)")
    fun getUserBooks(userId: String): List<BookData>

    @Update
    fun updateBook(vararg book: BookData): Int

    @Insert
    fun insertBook(vararg book: BookData)

    @Delete
    fun deleteBook(vararg book: BookData)

    @Query("SELECT * FROM Book")
    fun getAllBooks(): List<BookData>



}