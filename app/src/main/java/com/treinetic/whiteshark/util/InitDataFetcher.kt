package com.treinetic.whiteshark.util

import android.util.Log
import androidx.annotation.Keep
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.Books
import com.treinetic.whiteshark.services.*

@Keep
class InitDataFetcher {

    private val TAG = "InitDataFetcher"
    var onFinish: (() -> Unit)? = null
    var netException: NetException = NetException("Something went wrong", "ERROR", 500)
    var onErrror: ((exception: NetException) -> Unit)? = null
    var isErrorCalled = false

    var homeSuccess = false
    var homeError = false

    var categorySuccess = false
    var categoryError = false

    var cartSuccess = false
    var cartError = false

    var myLibrarySuccess = false
    var myLibraryError = false

    var wishListSuccess = false
    var wishListError = false


    fun beginFetch() {
        isErrorCalled = false
        getHomeData()
        getCategories()
        if (UserService.getInstance().isUserLogged()) {
            fetchCart()
            fetchWishlist()
            fetchMyLibrary()
        }

    }


    private fun checkProgress() {
//        onFinish?.let { it() }
//        return

        if (!UserService.getInstance().isUserLogged()) {
            Log.d(TAG, "Continue without login")
            if (homeSuccess && categorySuccess) {
                onFinish?.let { it() }
                return
            }
        }

        if (homeSuccess && categorySuccess && cartSuccess && myLibrarySuccess && wishListSuccess) {
            onFinish?.let { it() }
            return
        }

        if (isErrorCalled) return
        if (homeError || categoryError || cartError || myLibraryError || wishListError) {
            onErrror?.let { it(netException) }
            isErrorCalled = true
            return
        }

    }

    private fun getHomeData() {
        HomeService.getInstance().fetchHomePageData(Service.Success { result ->
            homeSuccess = true
            homeError = false
            checkProgress()

        }, Service.Error { exception ->
            if (exception.code == 403 && exception.message_id == "LOGIN_REQUIRED") {
                homeError = false
                homeSuccess = true
                return@Error
            }
            netException = exception
            homeSuccess = false
            homeError = true
            checkProgress()
        })
    }

    private fun getCategories() {
        CategoryService.getInstance().fetchCategoryList(Service.Success { result ->
            categorySuccess = true
            categoryError = false
            checkProgress()
        }, Service.Error { exception ->
            Log.e(TAG, exception.message?:"Category error")
            if (exception.code == 403 && exception.message_id == "LOGIN_REQUIRED") {
                categoryError = false
                categorySuccess = true
                checkProgress()
                return@Error

            }
            netException = exception
            categorySuccess = false
            categoryError = true
            checkProgress()
        })
    }

    private fun fetchCart() {
        LocalCartService.instance.updateUserCartWithLocalCartItems(success = {
            cartSuccess = true
            cartError = false
            checkProgress()
        },error = {exception ->
            if (exception!!.code == 403 && exception.message_id == "LOGIN_REQUIRED") {
                cartError = false
                cartSuccess = true
                return@updateUserCartWithLocalCartItems
            }
            netException = exception
            cartSuccess = false
            cartError = true
            checkProgress()
        })
        /*CartService.getInstance().loadCartRequest(Service.Success { result ->
            cartSuccess = true
            cartError = false
            checkProgress()

        }, Service.Error { exception ->
            if (exception.code == 403 && exception.message_id == "LOGIN_REQUIRED") {
                cartError = false
                cartSuccess = true
                return@Error
            }
            netException = exception
            cartSuccess = false
            cartError = true
            checkProgress()
        })*/
    }

    private fun fetchMyLibrary() {
        BookService.getInstance().fetchMyLibrary(Service.Success { result ->
            cacheBookCovers(result)
            myLibrarySuccess = true
            myLibraryError = false
            checkProgress()

        }, Service.Error { exception ->
            if (exception.code == 403 && exception.message_id == "LOGIN_REQUIRED") {
                myLibraryError = false
                myLibrarySuccess = true
                return@Error
            }
            netException = exception
            myLibrarySuccess = false
            myLibraryError = true
            checkProgress()
        })
    }

    private fun fetchWishlist() {
        WishListService.getInstance().fetchWishList(Service.Success { result ->
            wishListSuccess = true
            wishListError = false
            checkProgress()
        }, Service.Error { exception ->
            if (exception.code == 403 && exception.message_id == "LOGIN_REQUIRED") {
                wishListError = false
                wishListSuccess = true
                return@Error
            }
            netException = exception
            wishListSuccess = false
            wishListError = true
            checkProgress()
        })
    }

    fun cacheBookCovers(books: Books) {
        try {
            books?.data?.let {
                BookService.getInstance().cacheMyLibraryBookCovers(it.toTypedArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


}