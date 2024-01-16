package com.treinetic.whiteshark.services

//import com.orhanobut.logger.Logger

import android.net.Uri
import android.util.Log
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.DownloadListener
import com.bumptech.glide.Glide
import com.folioreader.Config
import com.folioreader.util.AppUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.treinetic.whiteshark.MyApp
import com.treinetic.whiteshark.MyApp.context
import com.treinetic.whiteshark.constance.ErrorCodes
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.*
import com.treinetic.whiteshark.network.Net
import com.treinetic.whiteshark.util.extentions.toArrayList
import okhttp3.OkHttpClient
import java.io.File
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class BookService {

    val TAG: String = "BookService"
    private val downloadedFonts: MutableMap<String, Font> = mutableMapOf()

    var allReviews = mutableMapOf<String, AllRatings>()
    var bookList = mutableMapOf<String, Book>()
    var myLibrary: Books? = null
    val gson = Gson()

    companion object {
        private var newInstance = BookService()
        fun getInstance(): BookService {
            return newInstance
        }
    }

    fun getBookById(success: Service.Success<Book>, error: Service.Error, bookId: String) {

        val pathParams = mutableMapOf<String, Any>()
        pathParams["id"] = bookId

        val net = Net(
            Net.URL.BOOK_BYID,
            Net.NetMethod.GET,
            null,
            null,
            pathParams,
            null
        )
        net.perform({ response ->
            Log.d(TAG, "getBookById book data")
//            Logger.json(response)
            try {
                val currentBook = Gson().fromJson(response, Book::class.java)
//                currentBook.ratingOfUser?.let {
                //currentBook.ratings = addMyReviewToList(it, currentBook.ratings)
//                }

                addBookToList(currentBook.id, currentBook)
                success.success(currentBook)
            } catch (e: Exception) {
                NetException(
                    "Something went wrong",
                    e.message,
                    ErrorCodes.JSON_ERROR
                )
            }

        },
            { exception ->
                error.error(exception)
            })

    }

    fun getBookProfileReviews(
        bookId: String,
        success: Service.Success<MutableList<Rating>>,
        error: Service.Error
    ) {
        val pathParams = mutableMapOf<String, Any>()
        pathParams["id"] = bookId

        val net = Net(
            Net.URL.BOOK_PROFILE_REVIEWS,
            Net.NetMethod.GET,
            null,
            null,
            pathParams,
            null
        )

        net.perform({ response ->
            try {
                val ratings_list =
                    gson.fromJson(response, Array<Rating>::class.java).toMutableList()
                success.success(ratings_list)

            } catch (e: Exception) {

            }
        },
            { exception ->
                error.error(exception)
            })
    }

    fun getAllReviews(bookId: String, success: Service.Success<AllRatings>, error: Service.Error) {

        if (allReviews.containsKey(bookId)) {
            success.success(allReviews[bookId])
            return
        }

        val pathParams = mutableMapOf<String, Any>()
        pathParams["id"] = bookId

        val net = Net(
            Net.URL.BOOK_REVIEWS,
            Net.NetMethod.GET,
            null,
            null,
            pathParams,
            null
        )
        net.perform({ response ->
            try {
                val ratings = Gson().fromJson(response, AllRatings::class.java)
                success.success(ratings)
                allReviews[bookId] = ratings
            } catch (e: Exception) {

            }

        },
            { exception ->
                error.error(exception)
            })

    }

    fun loadMore(
        url: String,
        bookId: String,
        success: Service.Success<AllRatings>,
        error: Service.Error
    ) {

        val pathParams = mutableMapOf<String, Any>()
        pathParams["id"] = bookId

        val net = Net(
            url, Net.NetMethod.GET, null, null, pathParams, null
        )
        net.perform({ response ->
            try {
                val ratings = Gson().fromJson(response, AllRatings::class.java)
                allReviews[bookId]?.data?.addAll(ratings.data)
                allReviews[bookId]?.data?.let {
                    ratings.data = it
                }
                success.success(ratings)

            } catch (e: Exception) {

            }

        },
            { exception ->
                error.error(exception)
            })

    }

    fun fetchMyLibrary(success: Service.Success<Books>, error: Service.Error) {

        val pathParams = mutableMapOf<String, Any>()
        pathParams["type"] = "MY_LIBRARY"
        val net = Net(
            Net.URL.MY_LIBRARY,
            Net.NetMethod.GET,
            null,
            null,
            null,
            null
        )

        net.perform({ response ->
            try {
                myLibrary = gson.fromJson(response, Books::class.java)
                LocalBookService.getInstance()
                    .syncMyLibrary(UserService.getInstance().getUser().user_id)
                myLibrary?.data?.let {
                    Books.updatePurchasedBooks(it, true)
                }
                success.success(myLibrary)
            } catch (e: Exception) {
                e.printStackTrace()
                error.error(
                    NetException(
                        "Something went wrong",
                        "json_error",
                        ErrorCodes.JSON_ERROR
                    )
                )
            }
        }, { exception ->
            error.error(exception)
        })

    }

    fun getMyLibraryNextPage(success: Service.Success<Books>, error: Service.Error) {

        myLibrary?.next_page_url?.let {
            Log.d(TAG, "getMyLibraryNextPage has url")

            val net = Net(
                it,
                Net.NetMethod.GET,
                null,
                null,
                null,
                null
            )
            net.perform({ response ->
                try {
                    val data = gson.fromJson(response, Books::class.java)
                    myLibrary?.data?.addAll(data.data)
                    myLibrary?.next_page_url = data.next_page_url
                    success.success(data)
                } catch (e: Exception) {
                    e.printStackTrace()
                    error.error(
                        NetException(
                            "Something went wrong",
                            "json_error",
                            ErrorCodes.JSON_ERROR
                        )
                    )
                }
            }, { exception ->
                error.error(exception)
            })
        }

    }

    fun isPurchasedBook(bookId: String): Boolean {

        myLibrary?.data?.forEach {
            if (it.id == bookId && it.isPurchased) return true
        }

        return false
    }

    fun getAuthorList(): List<Author> {
        return listOf()
    }

    fun getAuthorBooks(
        authorId: String,
        success: Service.Success<Books>,
        error: Service.Error
    ) {

        val map = mapOf("authorId" to authorId)

        val net = Net(
            Net.URL.EPUB,
            Net.NetMethod.GET,
            null,
            map,
            null,
            null
        )
        net.perform({ response ->
            try {
                val data = gson.fromJson(response, Books::class.java)
                success.success(data)
            } catch (e: Exception) {
                e.printStackTrace()
                error.error(
                    NetException(
                        "Something went wrong",
                        "json_error",
                        ErrorCodes.JSON_ERROR
                    )
                )
            }
        }, { exception ->
            error.error(exception)
        })
    }

    fun getPublishersBooks(
        publisherId: String,
        success: Service.Success<Books>,
        error: Service.Error
    ) {

        val map = mapOf("publisherId" to publisherId)

        val net = Net(
            Net.URL.EPUB,
            Net.NetMethod.GET,
            null,
            map,
            null,
            null
        )
        net.perform({ response ->
            try {
                val data = gson.fromJson(response, Books::class.java)
                success.success(data)
            } catch (e: Exception) {
                e.printStackTrace()
                error.error(
                    NetException(
                        "Something went wrong",
                        "json_error",
                        ErrorCodes.JSON_ERROR
                    )
                )
            }
        }, { exception ->
            error.error(exception)
        })
    }

    fun getNextPage(url: String?, success: Service.Success<Books>, error: Service.Error) {
        url?.let {

            val net = Net(
                it,
                Net.NetMethod.GET,
                null,
                null,
                null,
                null
            )
            net.perform({ response ->
                try {
                    val data = gson.fromJson(response, Books::class.java)
                    success.success(data)
                } catch (e: Exception) {
                    e.printStackTrace()
                    error.error(
                        NetException(
                            "Something went wrong",
                            "json_error",
                            ErrorCodes.JSON_ERROR
                        )
                    )
                }
            }, { exception ->
                error.error(exception)
            })
        }
    }

    fun saveReview(
        id: String,
        rating: Double,
        review: String,
        success: Service.Success<Rating>,
        error: Service.Error
    ) {

        val body = mutableMapOf<String, Any>()
        body["rating"] = rating
        body["review"] = review

        val pathParams = mutableMapOf<String, Any>()
        pathParams["id"] = id

        val net = Net(
            Net.URL.SAVE_REVIEW,
            Net.NetMethod.POST,
            body,
            null,
            pathParams,
            null
        )

        net.perform({ response ->
            val newRating = Gson().fromJson<Rating>(response, Rating::class.java)
            bookList[id]?.ratingOfUser = newRating
            success.success(newRating)
        }, { exception ->
            error.error(exception)
        })

    }

    private fun addBookToList(bookId: String, book: Book) {
        bookList[bookId] = book
    }

    fun addMyReviewToList(rating: Rating, list: MutableList<Rating>): MutableList<Rating> {

        val newList = mutableListOf<Rating>()
        newList.add(rating)
        list.forEach { t ->
            if (t.isMyReview) {

            } else {
                newList.add(t)
            }
        }
        return newList
    }

    fun getUserRating(id: String): Rating? {
        return if (bookList.containsKey(id)) {
            bookList[id]?.ratingOfUser
        } else {
            null
        }
    }

    fun deleteRating(
        bookId: String,
        ratingId: String,
        success: Service.Success<Rating>,
        error: Service.Error
    ) {

        val pathParam = mutableMapOf<String, Any>()
        pathParam["id"] = bookId
        pathParam["ratingId"] = ratingId

        val net = Net(
            Net.URL.DELETE_REVIEW,
            Net.NetMethod.DELETE,
            null,
            null,
            pathParam,
            null
        )

        net.perform({ response ->

            try {
                val rating = Gson().fromJson<Rating>(response, Rating::class.java)
                if (bookList.containsKey(bookId)) {
                    bookList[bookId]?.ratingOfUser = null
                }
                success.success(rating)
            } catch (e: java.lang.Exception) {
                e.stackTrace
            }


        }, { exception ->

            error.error(exception)

        })

    }

    fun clear() {
        allReviews = mutableMapOf<String, AllRatings>()
        bookList = mutableMapOf<String, Book>()
        myLibrary = null

    }

    fun setHighLightFont(currentBook: Book) {
        try {
            var config = AppUtil.getSavedConfig(context)
            if (config == null) {
                config = Config()
            } else {
                currentBook.bookFont.first().fontUrl.let { url: String ->
                    Uri.parse(url).lastPathSegment?.let {
                        val fileExt = it.split(".")[1]
                        val fontName = currentBook.bookFont.first().id + "." + fileExt

                        val file = File(context?.filesDir, fontName)
                        config.fontPath = file.path
                    }
                }

            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun downloadFont(font: Font) {
        Uri.parse(font.fontUrl).lastPathSegment?.let {
            val fileExt = it.split(".")[1]
            val fileName = font.id + "." + fileExt

            if (downloadedFonts.containsKey(fileName)) {
                return
            }

            try {
                val okHttpClient = OkHttpClient().newBuilder()
                    .connectTimeout(240, TimeUnit.SECONDS)
                    .readTimeout(240, TimeUnit.SECONDS)
                    .writeTimeout(240, TimeUnit.SECONDS)
                    .build()
                AndroidNetworking.download(font.fontUrl, context.filesDir.absolutePath, fileName)
                    .setTag("downloadTest")
                    .setPriority(Priority.MEDIUM)
                    .setOkHttpClient(okHttpClient)
                    .build()
                    .startDownload(object : DownloadListener {
                        override fun onDownloadComplete() {
                            Log.i(TAG, "downloaded")
                            downloadedFonts[fileName] = font
                        }

                        override fun onError(anError: ANError?) {
                            Log.i(TAG, anError.toString())
                        }
                    })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun cacheMyLibraryBookCovers(bookList: Array<Book>) {

        thread(start = true) {
            bookList.forEach { book: Book ->
                try {
                    Glide.with(MyApp.getAppContext())
                        .downloadOnly()
                        .load(book.getBookImage())
                        .submit()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e(TAG, "Error in cacheMyLibraryBookCovers")
                }

            }
        }


    }


    fun addToLibrary(
        book: Book, success: Service.Success<Book?>,
        error: Service.Error
    ) {

        val body = mutableMapOf<String, Any>()
        body["epubId"] = book.id

        val net = Net(
            Net.URL.LIBRARY,
            Net.NetMethod.POST,
            body,
            null,
            null,
            null
        )

        net.perform({ response ->
            try {
                val book = Gson().fromJson<Book>(response, Book::class.java)
                success.success(book)
            } catch (e: Exception) {
                e.printStackTrace()
                error.error(
                    NetException(
                        "Something went wrong",
                        "json_error",
                        ErrorCodes.JSON_ERROR
                    )
                )
            }

        }, { exception ->
            exception.printStackTrace()
            error.error(exception)
        })


    }

    fun getShareLink(book: Book, success: Service.Success<ShareUrl>, error: Service.Error) {
        var net = Net(
            Net.URL.SHARE_LINK.replace("{id}", book.id),
            Net.NetMethod.GET,
            null,
            null,
            null,
            null
        )

        net.perform({ response ->
            Log.d(TAG, "Success share link")
            try {
                val url = Gson().fromJson<ShareUrl>(response, ShareUrl::class.java)
                success.success(url)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, { exception ->
            Log.e(TAG, "Error share link")
            error.error(exception)
        })
    }

}