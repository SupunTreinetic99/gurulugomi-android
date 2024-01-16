package com.treinetic.whiteshark.services

import android.util.Log
import com.google.gson.Gson
import com.treinetic.whiteshark.constance.ErrorCodes
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.Book
import com.treinetic.whiteshark.models.Books
import com.treinetic.whiteshark.models.WishListItem
import com.treinetic.whiteshark.network.Net

/**
 * Created by Nuwan on 2/11/19.
 */
class WishListService {

    private val TAG = "WishListService"
    private val gson = Gson()
    var wishList: Books? = null

    companion object {

        private var instance = WishListService()
        fun getInstance(): WishListService {
            return instance
        }

    }

    fun fetchWishList(success: Service.Success<Books>, error: Service.Error) {

        val net = Net(
            Net.URL.WISH_LIST,
            Net.NetMethod.GET, null, null, null, null
        )

        net.perform({ response ->

            try {
                wishList = gson.fromJson(response, Books::class.java)
                success.success(wishList)

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, { exception ->
            Log.e(TAG, "Error in fetch wishlist")
            error.error(exception)
        })
    }

    fun addToWishList(book: Book, success: Service.Success<WishListItem>, error: Service.Error) {

        var map: MutableMap<String, Any> = mutableMapOf()
        map.put("epubId", book.id)

        var net = Net(
            Net.URL.ADD_TO_WISH_LIST,
            Net.NetMethod.POST,
            null,
            map,
            null,
            null
        )

        net.perform({ response ->
            Log.d(TAG, "Success add to wish list")
            try {
                updateWishListInBackground()
                var WishListItem = gson.fromJson(response, WishListItem::class.java)
                success.success(WishListItem)

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, { exception ->
            Log.e(TAG, "Error in add to wishlist")
            error.error(exception)
        })
    }

    fun removeFromWishList(books: List<Book>, success: Service.Success<String>, error: Service.Error) {

        var idList = books.map { it.id }
        var map: MutableMap<String, Any> = mutableMapOf()
        map.put("epubIds", idList)

        var net = Net(
            Net.URL.REMOVE_FROM_WISH_LIST,
            Net.NetMethod.POST,
            map,
            null,
            null,
            null
        )

        net.perform({ response ->
            updateWishListInBackground()
            success.success(response)
        }, { exception ->
            Log.e(TAG, "Error in remove from wishlist")
            error.error(exception)
        })
    }

    fun updateList(books: List<Book>) {

        wishList?.data?.let {
            it.addAll(books)
            return
        }

        wishList?.let {
            wishList?.data = books.toCollection(ArrayList())
        }


    }


    fun getNextPage(success: Service.Success<String>, error: Service.Error) {

        wishList?.next_page_url?.let {
            Log.d(TAG, "Has next page url $it")
            var net = Net(
                it,
                Net.NetMethod.POST,
                null,
                null,
                null,
                null
            )

            net.perform({ response ->

                try {
                    val data = gson.fromJson(response, Books::class.java)
                    data?.data?.let {
                        updateList(it)
                    }
                    success.success(response)

                } catch (e: Exception) {
                    e.printStackTrace()
                    error.error(
                        NetException(
                            "Somwthign went Wrong",
                            "JSON_ERROR",
                            ErrorCodes.JSON_ERROR
                        )
                    )
                }
            }, { exception ->
                Log.e(TAG, "Error in ;fetching next page wishlist")
                error.error(exception)
            })

            return
        }

        error.error(NetException("next page not found", "PAGE_END", 404))

    }
    private fun updateWishListInBackground() {
        fetchWishList({
            Log.d(TAG, "update wish in background list success")
        }, {
            Log.e(TAG, "update wish in background list ERROR")
        })

    }

    fun clearAllSelected() {
        wishList?.data?.forEach { book: Book ->
            book.isClick = false
        }
    }

    fun selectionCount(bookList: MutableList<Book>): Int {
        return bookList.count { it.isClick && (it.price!! > 0.0) }
    }

    fun clear() {
        wishList = null
    }

}