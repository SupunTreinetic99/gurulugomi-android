package com.treinetic.whiteshark.services

import android.util.Log
import com.google.gson.Gson
import com.treinetic.whiteshark.BuildConfig
import com.treinetic.whiteshark.models.Book
import com.treinetic.whiteshark.roomdb.AppDatabase
import com.treinetic.whiteshark.roomdb.models.BookData
import com.treinetic.google.androidx.gms

import com.treinetic.whiteshark.util.extentions.deleteFileOrDirectory
import com.treinetic.whiteshark.util.extentions.serializedBook
import com.treinetic.whiteshark.util.extentions.toBook
import java.io.File

import kotlin.Exception
import kotlin.concurrent.thread

/**
 * Created by Nuwan on 3/7/19.
 */
class LocalBookService {

    val TAG = "LocalBookService"


    companion object {
        private val instance = LocalBookService()
        private val encryptionUtil = gms.get()
        fun getInstance(): LocalBookService {
            return instance
        }

        val VALIDATION_KEY = BuildConfig.APPLICATION_ID + "-MQ=="
    }


    fun getUserBooks(
        userId: String,
        success: (books: List<BookData>) -> Unit,
        error: () -> Unit = {}
    ) {

        thread(start = true) {
            val data = AppDatabase.getInstance().bookDataDao().getUserBooks(userId)
            success(data)
        }

    }


    fun getBook(bookId: String, userId: String, onFinish: (books: BookData?) -> Unit) {

        thread(start = true) {
            val book = AppDatabase.getInstance().bookDataDao().getBookById(bookId, userId)
            onFinish(book)

        }

    }

    fun addBook(book: Book, userId: String, key: String, onFinish: (isSuccess: Boolean) -> Unit) {

        thread(start = true) {
            val bookJson: String = Gson().toJson(book)
            val data = BookData(book.id, userId, bookJson, 0, key)
            data.setBook(book)
            data.key = key
            val res = AppDatabase.getInstance().bookDataDao().insertBook(data)

            Log.d(TAG, "Book Added : " + data.id)
            onFinish(true)
        }

    }

    fun updateBook(data: BookData, onFinish: (isSuccess: Boolean) -> Unit) {
        thread(start = true) {
            val res = AppDatabase.getInstance().bookDataDao().updateBook(data)
            if (res > 0) {
                Log.d(TAG, "Book Added : " + data.id)
                onFinish(true)
            } else {
                Log.d(TAG, "Book NOT Added : " + data.id)
                onFinish(false)
            }
        }
    }


    fun saveOrUpdateBook(
        book: Book,
        userId: String,
        key: String,
        onFinish: (isSuccess: Boolean) -> Unit
    ) {
        thread(start = true) {
            val b = AppDatabase.getInstance().bookDataDao().getBookById(book.id, userId)
            b?.let {
                var data = it.getBookObj()
                it.key = key
                it.bookmark = 0
                it.setBook(book)
                updateBook(it, onFinish)
                Log.d(TAG, "book updated name: ${book.title} id: ${book.id}")
                return@thread
            }
            addBook(book, userId, key, onFinish)
        }

    }


    fun checkAvailablity(
        userId: String,
        bookId: String,
        availability: (isAvailable: Boolean) -> Unit
    ) {
        thread(start = true) {
            val book = AppDatabase.getInstance().bookDataDao().getBookById(bookId, userId)
            var available = false
            book?.let {
                isBookAvailableLocally(it.book.toBook()!!)
            }
        }
    }

    fun isBookAvailableLocally(book: Book): Boolean {
        book.localPath?.let {
            val file = File(book.localPath)
            return file.exists()
        }
        return false
    }

    fun clearAll() {

        thread(start = true) {

            try {
                AppDatabase.getInstance().clearAllTables()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }


    fun deleteBook(book: BookData, onFinish: (isSuccess: Boolean) -> Unit) {
        thread(start = true) {
            try {
                AppDatabase.getInstance().bookDataDao().deleteBook(book)
                onFinish(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onFinish(false)
            }
        }
    }


    fun syncMyLibrary(userId: String) {

        thread(start = true) {
            try {
                BookService.getInstance().myLibrary?.let { books ->
                    books?.data?.let {
                        for (book in it) {
                            val localBook =
                                AppDatabase.getInstance().bookDataDao().getBookById(book.id, userId)

                            if (localBook != null) continue
                            if (localBook != null && book.serializedBook().isNotEmpty()) {
                                localBook.book.toBook()?.fill(book)?.serializedBook()?.let {
                                    localBook.book = it
                                    updateBook(
                                        localBook,
                                        onFinish = { Log.d(TAG, "syncMyLibrary book updated") })
                                }
                                continue
                            }
                            addBook(book, userId, "") {
                                Log.d(TAG, "Book Synced ${book.id}")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }

    fun validateDownloadedBooks(onFinish: () -> Unit) {

        thread(start = true) {
            val books = AppDatabase.getInstance().bookDataDao().getAllBooks()

            books.forEach {
                it.getBookObj()?.let { book ->
                    if (!book.isValidDownloadedCopy() && book.localPath != null) {
                        var file= File(book.localPath)
                        file.deleteFileOrDirectory()
                        book.localPath = null
                        it.key = ""
                        it.setBook(book)
                        updateBook(it, onFinish = {
                            Log.d(TAG, "Book deleted and updated name: ${book.title} \n id:${book.id}")
                        })
                    }
                }
            }
            onFinish()
        }

    }


}