package com.treinetic.whiteshark.fragments.library

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.treinetic.whiteshark.constance.AppModes
import com.treinetic.whiteshark.constance.DeviceInformation
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.Book
import com.treinetic.whiteshark.models.Books
import com.treinetic.whiteshark.roomdb.models.BookData
import com.treinetic.whiteshark.services.*
import java.io.File

/**
 * Created by Nuwan on 2/15/19.
 */
class MyLibraryViewModel : ViewModel() {

    private val TAG = "MyLibraryVM"
    private var myLibrary: MutableLiveData<Books> = MutableLiveData()
    private var netException: MutableLiveData<NetException> = MutableLiveData()
    private var fetchBookException: MutableLiveData<NetException> = MutableLiveData()
    private var book: MutableLiveData<Book> = MutableLiveData()
    private var progress: MutableLiveData<Long> = MutableLiveData()
    private var downloadBook: Book? = null
    var fullBook: Book? = null
    var localBookList: ArrayList<BookData> = arrayListOf()

    var isMyLibfetched = false
    var hasError = false


    fun getMyLibrary(): MutableLiveData<Books> {
        return myLibrary
    }

    fun getException(): MutableLiveData<NetException> {
        return netException
    }

    fun getFetchBookException(): MutableLiveData<NetException> {
        return fetchBookException
    }

    fun getbook(): MutableLiveData<Book> {
        return book
    }

    fun resetBook() {
        this.book = MutableLiveData()
    }

    fun getProgress(): MutableLiveData<Long> {
        return progress
    }

    fun getDownloadBook(): Book? {
        return downloadBook
    }

    fun fetchMyLibrary() {

        hasError = false
//        BookService.getInstance().myLibrary?.let {
//            myLibrary.value = it
//
//        }

        if (UserService.isOfflineMode()) {
            getLocalBooks()
            return
        }

        BookService.getInstance().fetchMyLibrary({
            hasError = false
            it.let { data ->
                updateBooks(data.data)
            }
            myLibrary.value = it
        }, {
            netException.postValue(it)
            hasError = true
        })
    }

    fun updateBooks(books: List<Book>) {
        Books.updatePurchasedBooks(books, true)
    }

    fun getNextPage() {
        Log.d(TAG, "getNextPage calling")
        BookService.getInstance().getMyLibraryNextPage({
            Log.d(TAG, "getNextPage success")
            myLibrary.value = it
        }, {
            Log.d(TAG, "getNextPage Error")
            it?.printStackTrace()
        })

    }

    fun fetchBook(book: Book) {
        BookService.getInstance().getBookById({ b: Book ->
            b?.let {
                book.fill(it)
                this.book.postValue(book)
                this.fullBook = book
            }

        }, { exception: NetException? ->
            fetchBookException.postValue(exception)
        }, book.id)
    }

    fun isAlreadyFetched(book: Book): Boolean {
        this.fullBook?.let {
            return it.id == book.id
        }
        return false
    }


    fun downloadBook(
        book: Book,
        isPreview: Boolean,
        token: String,
        success: (path: String, isPreview: Boolean) -> Unit
    ) {
        progress.value = 0
        downloadBook = book
        Log.d(TAG, "downloadBook token :  $token")
        var deviceId = DeviceInformation().getDeviceId()
        book.downloadedEpubVersion = book.epubVersion
        BookDownloadService.getInstance().downloadBook(token, book, deviceId, false,
            { path: String ->
                Log.d(TAG, "download success")
                Log.d(TAG, "download success path $path")
                success(path, isPreview)
            },
            { error: NetException ->
                error.printStackTrace()
            },
            { downloaded: Long, total: Long ->
                Log.e(TAG, "bytesDownloaded $downloaded | totalBytes: $total")
                var p: Double

                if (total > 0) {
                    p = (downloaded.toDouble() / total.toDouble()) * 100.00
                    progress.postValue(p.toLong())
                } else {
                    p = downloaded.toDouble()
                    progress.postValue(p.toLong())
                }
                Log.e(TAG, "downloaded : ${progress.value} ")

            })
    }


    fun getLocalBooks() {

        if (!UserService.APP_MODE.equals(AppModes.OFFLINE_MODE)) {
            return
        }

        BookService.getInstance().myLibrary?.let {
            return
        }

        UserService.getInstance().getUser().user_id.let {
            LocalBookService.getInstance().getUserBooks(it, {
                localBookList = ArrayList(it)
                var booklist: ArrayList<Book> = arrayListOf()

                it?.let {
                    for (data in it) {

                        booklist.add(data.getBookObj()!!)
                    }
                }

                BookService.getInstance().myLibrary = Books().apply {
                    data = booklist
                }
                //            fetchMyLibrary()
                myLibrary.postValue(BookService.getInstance().myLibrary)
            }, {
                Log.e(TAG, "Error in locally available books")
            })
        }


    }


    fun deleteDownloadedBook(book: Book, onFinish: (isSuccess: Boolean) -> Unit) {

        book.getLocallyAvailable { isAvailable, bookData ->
            var path: String? = bookData?.getBookObj()?.localPath
            if (path == null) {
                onFinish(true)
                return@getLocallyAvailable
            }
            try {
                var file = File(path)
                file.delete()
                bookData?.let {
                    // LocalBookService.getInstance().deleteBook(bookData, onFinish)
                    book?.let {
                        it?.localPath = null
                        bookData.setBook(book)
                        LocalBookService.getInstance().updateBook(bookData, onFinish)
                    }
                    return@getLocallyAvailable
                }
                onFinish(false)
            } catch (e: Exception) {
                e.printStackTrace()
                onFinish(false)
            }
        }


    }


}