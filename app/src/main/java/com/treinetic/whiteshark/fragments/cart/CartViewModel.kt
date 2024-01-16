package com.treinetic.whiteshark.fragments.cart

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.Book
import com.treinetic.whiteshark.services.CartService
import com.treinetic.whiteshark.services.LocalCartService
import com.treinetic.whiteshark.services.Service
import com.treinetic.whiteshark.services.UserService
import com.treinetic.whiteshark.util.extentions.toArrayList
import com.treinetic.whiteshark.util.extentions.toBook

class CartViewModel : ViewModel() {

    private val TAG = "CartViewModel"

    private var items: MutableLiveData<MutableList<Book>> = MutableLiveData()
    private var netException: MutableLiveData<NetException> = MutableLiveData()
    var deleteResponse: MutableLiveData<String> = MutableLiveData()
    var successCartCreation: MutableLiveData<MutableList<Book>> = MutableLiveData()


    fun getCart(): LiveData<MutableList<Book>> {
        return items
    }

    fun getResponse(): LiveData<String> {
        return deleteResponse
    }

    fun setResponse(response: MutableLiveData<String>) {
        deleteResponse = response
    }

    fun resetDeleteResponse() {
        deleteResponse = MutableLiveData()
    }

    fun loadCart() {

        if (!UserService.getInstance().isUserLogged()) {
            loadLocalCart()
            return
        }

        CartService.getInstance().loadCartRequest({ result ->

            items.postValue(result.map { it.book }.toMutableList())

        }, { exception ->

            netException.value = exception
        }, isRefresh = true)
    }

    fun loadLocalCart() {
        LocalCartService.instance.getAllCartItems(success = {
            items.postValue(it.map { it.book.toBook()!! }.toMutableList())
        }, error = {
            netException.postValue(
                NetException(
                    "Something went wrong while loading cart items",
                    "ERROR",
                    400
                )
            )
        })
    }

    fun deleteCartItem() {
        if (!UserService.getInstance().isUserLogged()) {
            deleteItemsFromLocalCart()
            return
        }
        CartService.getInstance().deleteCartItems(
            { result ->
                items.postValue(result.map { it.book }.toMutableList())
                deleteResponse.value = ""
            }, { exception ->
                netException.value = exception

            })


    }

    private fun deleteItemsFromLocalCart() {
        LocalCartService.instance.deleteFromCart(
            list = items.value!!.filter { it.isClick }.toArrayList(),
            success = {
                items.postValue(it)
            }, error = {
                netException.value = NetException("Something went wrong", "LOCAL_CART_FAILED", 404)
            }
        )
    }

    fun getNetException(): MutableLiveData<NetException> {
        return netException
    }


    fun createCartFromLocalCart() {

        LocalCartService.instance.getAllCartItems(success = { cartItemDataList ->
            if (cartItemDataList.isNullOrEmpty()) {
                loadCart()
                return@getAllCartItems
            }
            addItemsToCart(cartItemDataList.map { it.book.toBook()!! }.toMutableList())
        }, error = {

        })
    }


    fun addItemsToCart(list: MutableList<Book>) {
        CartService.getInstance().addToCartRequest(bookList = list,
            success = {
                items.postValue(it.map { it.book }.toMutableList())
            },
            error = {
                netException.postValue(it)
            }
        )
    }

    fun getIdList(): MutableList<String> {
        val idList = mutableListOf<String>()
        for (item in CartService.getInstance().getSelectedItemList()) {
            idList.add(item.book.id)
            Log.e(TAG, "Componant = ${item.book.id}")
        }
        return idList
    }


}