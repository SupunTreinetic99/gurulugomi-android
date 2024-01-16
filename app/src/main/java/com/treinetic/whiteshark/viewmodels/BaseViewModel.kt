package com.treinetic.whiteshark.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.treinetic.whiteshark.services.LocalCartService

open class BaseViewModel : ViewModel() {
    var cartItemCount: MutableLiveData<Int> = MutableLiveData()
    fun getLocalCartItemCount() {
        LocalCartService.instance.getAllCartItems(success = {
            cartItemCount.postValue(it.size)
            Log.d("BaseViewModel", "cart item count = ${it.size}")
        })
    }
}