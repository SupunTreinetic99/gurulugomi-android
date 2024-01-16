package com.treinetic.whiteshark.fragments.wishlist

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.Book
import com.treinetic.whiteshark.models.Books
import com.treinetic.whiteshark.services.CartService
import com.treinetic.whiteshark.services.Service
import com.treinetic.whiteshark.services.WishListService
import com.treinetic.whiteshark.util.extentions.clear

/**
 * Created by Nuwan on 2/11/19.
 */
class WishListViewModel : ViewModel() {


    private var wishList: MutableLiveData<List<Book>> = MutableLiveData()
    var updatedList: MutableList<Book> = mutableListOf()
    var isFetchedWishlist: Boolean = false
    private var deleteResponse: MutableLiveData<String> = MutableLiveData()
    var deleteWishListError: MutableLiveData<NetException> = MutableLiveData()
    var fetchWishlistError: MutableLiveData<NetException> = MutableLiveData()
    var addToCartError: MutableLiveData<NetException> = MutableLiveData()
    var addToCartSuccess: MutableLiveData<String> = MutableLiveData()
    var fetchWishListError: MutableLiveData<NetException> = MutableLiveData()
    val TAG = "WishListViewModel"


    fun clearDeleteResponse() {
        deleteResponse.value = null
    }

    fun getWishList(): MutableLiveData<List<Book>> {
        return wishList
    }

    fun getResponse(): MutableLiveData<String> {
        return deleteResponse
    }

    fun fetchWishList() {

        WishListService.getInstance().wishList?.let { books ->
            books.data.let {
                wishList.postValue(it)
                updatedList = it
                return
            }
        }
        WishListService.getInstance().fetchWishList({
            it?.let {
                isFetchedWishlist = true
                wishList.postValue(it.data)
                updatedList = it.data
            }
        }, {
            isFetchedWishlist = true
            fetchWishListError.postValue(it)
        })

    }

    fun getNextPage(success: () -> Unit, error: (exception: NetException) -> Unit) {

        Log.d(TAG, "callign wishlis next page ")
        WishListService.getInstance().getNextPage({
            success()
        }, {
            error(it)
        })

    }

    fun deleteFromWishList(list: List<Book>, error: (exception: NetException) -> Unit) {

        WishListService.getInstance().removeFromWishList(list, {
            updateWishList(list)
            Books.updateIsAtWishlist(list, false)
            deleteResponse.value = ""

        }, {
            error(it)
            deleteWishListError.postValue(it)
        })

    }

    fun getSelectedBooks(): List<Book> {

        getWishList().value?.let {
            return it.filter {
                Log.e(TAG, "price = ${it.price} ")
                it.isClick}
        }

        return listOf()
    }


    fun getSelectedBooksForAddToCard(): List<Book> {

        getWishList().value?.let {
            return it.filter {
                Log.e(TAG, "price = ${it.price} ")
                it.isClick && (it.price!! > 0.0)}
        }

        return listOf()
    }

    private fun updateWishList(list: List<Book>) {

        val data = getWishList().value?.filter { book ->
            list.filter {
                it.id == book.id
            }.isEmpty()
        }

        updatedList = data as MutableList<Book>

        getWishList().postValue(data)

    }

    fun addToCart(list: List<Book>, error: (exception: NetException) -> Unit, success: () -> Unit) {

        CartService.getInstance().addToCartRequest(list as MutableList<Book>,
            {
                Log.d(TAG, "addToCart SUCCESS ")
                success()
                addToCartSuccess.postValue("Add to cart success")
            }, {
                it.printStackTrace()
                error(it)
                addToCartError.postValue(it)
            }
        )


    }


}