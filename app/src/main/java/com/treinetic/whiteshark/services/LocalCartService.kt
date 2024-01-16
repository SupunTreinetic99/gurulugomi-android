package com.treinetic.whiteshark.services

import android.util.Log
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.Book
import com.treinetic.whiteshark.roomdb.AppDatabase
import com.treinetic.whiteshark.roomdb.models.CartItemData
import com.treinetic.whiteshark.util.extentions.toArrayList
import com.treinetic.whiteshark.util.extentions.toBook
import com.treinetic.whiteshark.util.extentions.toJson
import kotlin.concurrent.thread

class LocalCartService {

    private val tag = "LocalCartService"

    companion object {
        var instance = LocalCartService()
    }


    fun addToCart(list: ArrayList<Book>, success: () -> Unit, error: (() -> Unit)? = null) {

        thread(start = true) {
            try {
                list.forEach { item ->
                    if (AppDatabase.getInstance().cartItemDao().getCartItem(item.id).isNotEmpty()) {
                        success()
                        return@thread
                    }
                    var cartItemData = CartItemData(
                        bookId = item.id,
                        book = item.toJson()!!
                    )
                    AppDatabase.getInstance().cartItemDao().insertCartItem(cartItemData)
                    success()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                error?.let { it() }
            }
        }
    }

    fun deleteFromCart(
        list: ArrayList<Book>,
        success: (list: ArrayList<Book>) -> Unit,
        error: (() -> Unit)? = null
    ) {
        thread(start = true) {
            try {
                list.forEach { item ->
                    var cartItemData = CartItemData(
                        bookId = item.id,
                        book = item.toJson()!!
                    )
                    AppDatabase.getInstance().cartItemDao().deleteCartItem(cartItemData)
                }

                var cartItems = AppDatabase.getInstance().cartItemDao().getAllCartItems()
                if (cartItems.isNullOrEmpty()) {
                    success(arrayListOf())
                    return@thread
                }
                var bookList = cartItems.map { cartItemData -> cartItemData.book.toBook()!! }
                success(bookList.toArrayList())
            } catch (e: Exception) {
                e.printStackTrace()
                error?.let { it() }
            }
        }
    }

    fun getAllCartItems(
        success: (cartItems: List<CartItemData>) -> Unit,
        error: (() -> Unit)? = null
    ) {
        thread(start = true) {
            try {
                success(AppDatabase.getInstance().cartItemDao().getAllCartItems())
            } catch (e: Exception) {
                e.printStackTrace()
                error?.let { it() }
            }
        }
    }

    fun deleteAllCartItems(success: () -> Unit, error: (() -> Unit)? = null) {
        thread(start = true) {
            try {
                AppDatabase.getInstance().cartItemDao().deleteAllCartItems()
                success()
            } catch (e: Exception) {
                e.printStackTrace()
                error?.let { it() }
            }
        }
    }


    fun updateUserCartWithLocalCartItems(
        success: () -> Unit,
        error: ((exception: NetException?) -> Unit)
    ) {

        getAllCartItems(success = { list ->
            if (list.isNullOrEmpty()) {
                Log.d(tag, "No local cart items Available")
                fetchUserCart(success = success, error = error)
                return@getAllCartItems
            }
            var bookList = list.map { item ->
                item.book.toBook()!!
            }
            Log.d(tag, "update cart with local items")
            updateCart(bookList = bookList.toMutableList(), success = success, error = error)
        }, error = {
            error(NetException("Failed to fetch", "ERROR", 404))
        })

    }

    private fun fetchUserCart(
        success: () -> Unit,
        error: ((exception: NetException?) -> Unit)
    ) {
        CartService.getInstance().loadCartRequest(
            isRefresh = true,
            success = {
                deleteAllCartItems(
                    success = { Log.d(tag, "delete all Items success") },
                    error = { Log.e(tag, "delete all Items Failed") })
                success()
            },
            error = {
                error(it)
            }
        )
    }

    private fun updateCart(
        bookList: MutableList<Book>,
        success: () -> Unit,
        error: ((exception: NetException?) -> Unit)? = null
    ) {
        deleteAllCartItems(success = {}, error = {})
        CartService.getInstance().addToCartRequest(
            bookList = bookList,
            success = {
                Log.d(tag, "updateCart success")
                success()
            }, error = { exception ->
                Log.e(tag, "updateCart error")
                exception.printStackTrace()
                error?.let { it(exception) }
            }
        )
    }


}