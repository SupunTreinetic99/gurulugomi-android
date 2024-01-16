package com.treinetic.whiteshark.services

import com.google.gson.Gson
import com.treinetic.whiteshark.constance.ErrorCodes
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.Book
import com.treinetic.whiteshark.models.CartItem
import com.treinetic.whiteshark.network.Net

class CartService {

    private var cartItems = mutableListOf<CartItem>()

    companion object {
        private val newInstance = CartService()
        fun getInstance(): CartService {
            return newInstance
        }
    }

    fun getCartSize(): Int {
        return if (cartItems.isNotEmpty()) {
            cartItems.size
        } else {
            0
        }

    }

    fun loadCartRequest(
        success: Service.Success<MutableList<CartItem>>,
        error: Service.Error,
        isRefresh: Boolean = false
    ) {

        if (cartItems.isNotEmpty() && !isRefresh) {
            success.success(cartItems)
            return
        }
        val net = Net(
            Net.URL.CART,
            Net.NetMethod.GET,
            null,
            null,
            null,
            null
        )

        net.perform({ response ->

            try {
                val items: MutableList<CartItem> =
                    Gson().fromJson(response, Array<CartItem>::class.java).toMutableList()
                cartItems = items
                success.success(cartItems)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, { exception ->
            error.error(exception)

        })

    }


    fun getSelectedItemList(): MutableList<CartItem> {
        val items = mutableListOf<CartItem>()
        for (item in cartItems) {
            if (item.book.isClick) {
                items.add(item)
            }
        }
        return items

    }

    fun addToCartRequest(
        bookList: MutableList<Book>,
        success: Service.Success<MutableList<CartItem>>,
        error: Service.Error
    ) {


        val body = mutableMapOf<String, List<String>>()
        body["epubIds"] = getBookIdList(bookList)

        val net = Net(
            Net.URL.ADD_CART,
            Net.NetMethod.POST,
            body,
            null,
            null,
            null
        )

        net.perform({ response ->
            try {
                val updatedCart: MutableList<CartItem> =
                    Gson().fromJson(response, Array<CartItem>::class.java).toMutableList()
                updatedCart.let {
                    cartItems = it
                }
                success.success(updatedCart)
            } catch (e: java.lang.Exception) {
                error.error(
                    NetException(
                        "Something went wrong",
                        "json_error",
                        ErrorCodes.JSON_ERROR
                    )
                )
            }
        }, { Exception ->
            error.error(Exception)

        })

    }

    fun deleteCartItems(
        success: Service.Success<MutableList<CartItem>>,
        error: Service.Error
    ) {

        val body = mutableMapOf<String, List<String>>()
        body["epubIds"] = getBookListId()

        val net = Net(
            Net.URL.DELETE_CART_ITEMS,
            Net.NetMethod.POST,
            body,
            null,
            null,
            null
        )
        net.perform({ response ->
            try {
                val updatedCart: MutableList<CartItem> =
                    Gson().fromJson(response, Array<CartItem>::class.java).toMutableList()
                cartItems = updatedCart
                success.success(updatedCart)
            } catch (e: Exception) {
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
        })
    }

    fun selectionCount(bookList: MutableList<Book>): Int {
        return bookList.count { it.isClick }
    }

    fun getCartValue(): Double {
        var total = 0.00
        val selectedBook = getSelectedItemList()
        val priceList = selectedBook.map { it.book.priceDetails.visiblePrice }.toMutableList()
        for (price in priceList) {
            price.let {
                total += it
            }
        }
        return total
    }

    private fun getBookIdList(books: MutableList<Book>): List<String> {
        return books.map { it.id }
    }

    private fun getBookListId(): List<String> {
        val list = getSelectedItemList()
        return list.map { it.book.id }
    }

    fun deSelectAll() {
        cartItems.map { t -> t.book.isClick = false }
    }

    fun clear() {
        cartItems = mutableListOf()
    }

    fun refreshCart() {
        val net = Net(
            Net.URL.CART,
            Net.NetMethod.GET,
            null,
            null,
            null,
            null
        )

        net.perform({ response ->
            try {
                val items: MutableList<CartItem> =
                    Gson().fromJson(response, Array<CartItem>::class.java).toMutableList()
                cartItems = items

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, { exception ->


        })
    }
}