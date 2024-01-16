package com.treinetic.whiteshark.fragments.bookprofile

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.treinetic.whiteshark.constance.DeviceInformation
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.Book
import com.treinetic.whiteshark.models.WishListItem
import com.treinetic.whiteshark.services.*
import com.treinetic.google.androidx.gms
import com.treinetic.whiteshark.models.Rating
import com.treinetic.whiteshark.models.ShareUrl
import com.treinetic.whiteshark.viewmodels.BaseViewModel

/**
 * Created by Nuwan on 2/7/19.
 */
class BookProfileViewModel : BaseViewModel() {

    private var book: MutableLiveData<Book> = MutableLiveData()
    private var netException: MutableLiveData<NetException> = MutableLiveData()
    var callBack: ((message: String) -> Unit)? = null
    private var TAG = "BookProfileVM"
    var currentBook: Book? = null
    var progress: MutableLiveData<Long> = MutableLiveData()
    var fullBook: Book? = null
    var refreshedBook: MutableLiveData<Book> = MutableLiveData()
    var addToLibrary: MutableLiveData<Book> = MutableLiveData()
    var addToLibraryError: MutableLiveData<NetException> = MutableLiveData()
    var currentTask: BookProfileFragment.Actions? = null
    var share_link: MutableLiveData<ShareUrl> = MutableLiveData()
    var ratingSuccess: MutableLiveData<MutableList<Rating>> = MutableLiveData()
    var ratingFailed: MutableLiveData<NetException> = MutableLiveData()
    var removeWishListItemLiveData: MutableLiveData<String> = MutableLiveData()
    var wishListItemSuccess: MutableLiveData<String> = MutableLiveData()
    var openBookAfterFetch=false
    var loadingCallBack: ((isLoad :Boolean) -> Unit)? = null
    //is user logged when book fetching
    var isUserLogged: Boolean = false
    var refreshBookInProgress = false

    companion object {}

    fun getNetException(): MutableLiveData<NetException> {
        return netException
    }

    private var isRemovedFromWishList = false
    private var isAddToWishList = false
    var isFetchBookCalled: Boolean = false
    private var isBookLoaded: Boolean = false

    fun bookInit(b: Book?) {
        book.value = b
    }

    fun getBook(): MutableLiveData<Book> {
        return book
    }

    fun clearGetBook(){
        book.value = null
    }

    fun setRatingsToBook(rating: MutableList<Rating>) {
        book.value?.apply {
            ratings = rating
        }
    }

    fun fetchBook(id: String, refrehRequired: Boolean = false) {
        if (isFetchBookCalled && !refrehRequired) {
            Log.e(TAG, "Book fetched called earlier")
            return
        }
        Log.d(TAG, "Book fetch calling")
        isFetchBookCalled = true

        isUserLogged = UserService.getInstance().isUserLogged()

        BookService.getInstance().getBookById(
            { response ->
                Log.d(TAG, "Book fetch success")
                // Logger.json(response.serializedBook())
                response.let {
                    getRatings(response.id)
                    if (refrehRequired) {
                        refreshedBook.postValue(it)
                        fullBook = it
                        fillBook(it)
                        return@let
                    }
                    it.isFill = true
                    it.isFetchedBeforeLogin = !UserService.getInstance().isUserLogged()
                    fillBook(it)
                    book.postValue(it)
                    isBookLoaded = true
                    fullBook = response

                }

            }, { exception ->
                exception.printStackTrace()
                netException.value = exception
                Log.e(TAG, "Book fetch error")
            },
            id
        )
    }

    fun fillBook(b: Book?) {
        if (b != null && BookProfileFragment.initbook.id == b.id) {
            Log.d(TAG, "Book fill called")
            BookProfileFragment.initbook.fill(b)
        } else {
            Log.e(TAG, "Book fill NOT called")
        }
    }

    fun getRatings(id: String) {

        loadingCallBack?.let {
            it(true)
        }

        BookService.getInstance()
            .getBookProfileReviews(id, { result ->
                getBook().value?.ratings = result
                getBook().value?.ratingOfUser?.let {
                    getBook().value?.ratings = addMyReviewToList(it, result)
                }
                ratingSuccess.postValue(getBook().value?.ratings)

            }, { exception ->
                ratingFailed.value = exception
            })
    }

    fun addMyReviewToList(rating: Rating, list: MutableList<Rating>): MutableList<Rating> {
        val newList = mutableListOf<Rating>()
        newList.add(rating)
        list.forEach { t ->
            if (!t.isMyReview) {
                newList.add(t)
            }
        }
        return newList
    }

    fun addToWishList(book: Book, error: (exception: NetException) -> Unit) {
        WishListService.getInstance().addToWishList(book,
            { result ->
                book.isAtWishlist = true
                isAddToWishList = true
                isRemovedFromWishList = false
                wishListItemSuccess.postValue("Success")
            }, { exception ->
                error(exception)
            })
    }

    fun removeFromToWishList(book: Book, error: (exception: NetException) -> Unit) {
        WishListService.getInstance().removeFromWishList(
            listOf(book),
            { result ->
                book.isAtWishlist = false
                isAddToWishList = false
                isRemovedFromWishList = true
                removeWishListItemLiveData.postValue("success")
            }, { exception ->
                error(exception)
            })
    }

    fun isAddedToWishList(): Boolean {
        return isAddToWishList && !isRemovedFromWishList
    }

    fun isRemovedFromWishList(): Boolean {
        return !isAddToWishList && isRemovedFromWishList
    }

    fun deleteReview(id: String, ratingId: String) {
        BookService.getInstance()
            .deleteRating(id, ratingId, { result ->
                callBack?.let { it("ok") }

            }, { exception ->
                netException.value = exception

            })
    }


    fun saveBook(key: String, userId: String) {

        LocalBookService.getInstance()
            .saveOrUpdateBook(BookProfileFragment.initbook, userId, key) { isSuccess: Boolean ->
                Log.d(TAG, "Book saved : $isSuccess")
            }
    }


    fun downloadBook(
        book: Book,
        isPreview: Boolean,
        success: (path: String, isPreview: Boolean) -> Unit,
        token: String? = null
    ) {
        var deviceId = DeviceInformation().getDeviceId()

        BookDownloadService.getInstance().downloadBook(token, book, deviceId, isPreview,
            { path: String ->

                Log.d(TAG, "download success")

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
                    Log.d(TAG, "pecentage p = $p")
                    progress.postValue(p.toLong())
                } else {
                    p = downloaded.toDouble()
                    Log.d(TAG, "downloaded p = $p")
                    progress.postValue(p.toLong())
                }
                Log.e(TAG, "downloaded : ${progress.value} | p=$p")

            })
    }


    fun getEncryptionKey(token: String): String {
        val encryptUtil = gms()
        var key = encryptUtil.buildKey(token)
        gms.bookEncryptKey = key
        return key
    }


    fun addToLibrary(book: Book) {

        BookService.getInstance().addToLibrary(
            book,
            {
                addToLibrary.postValue(book)
            }, {
                addToLibraryError.postValue(it)
            })
    }

    fun getShareLink(book: Book, error: (exception: NetException) -> Unit) {

        BookService.getInstance().getShareLink(book,
            { result ->
                share_link.postValue(result)
            }, { exception ->
                error(exception)
            })
    }


    /*fun getLocalCartItemCount() {
        LocalCartService.instance.getAllCartItems(success = {
            cartItemCount.postValue(it.size)
            Log.d(TAG, "cart item count = ${it.size}")
        })
    }*/

}