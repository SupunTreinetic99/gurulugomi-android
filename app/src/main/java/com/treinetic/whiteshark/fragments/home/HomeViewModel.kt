package com.treinetic.whiteshark.fragments.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.BookCategory
import com.treinetic.whiteshark.services.HomeService
import com.treinetic.whiteshark.services.LocalCartService
import com.treinetic.whiteshark.services.Service
import com.treinetic.whiteshark.viewmodels.BaseViewModel

class HomeViewModel : BaseViewModel() {

    private val TAG = "HomeViewModel"
    private var homeData: MutableLiveData<BookCategory> = MutableLiveData()
    private var isLoad = false
    private var isNextPageFinished: MutableLiveData<Boolean> = MutableLiveData()
    var netException: MutableLiveData<NetException> = MutableLiveData()
    var pageNetException: MutableLiveData<NetException> = MutableLiveData()
//    var cartItemCount: MutableLiveData<Int> = MutableLiveData()

    fun getHomeData(): LiveData<BookCategory> {
        return homeData
    }

    fun fetchHomeData(refresh: Boolean = false) {
        HomeService.getInstance().fetchHomePageData({ result ->
            homeData.value = result

        }, { exception ->
            exception.printStackTrace()
            netException.postValue(exception)
        }, refresh)


    }

    fun loadMoreData() {
        Log.e(TAG, " calling loadMoreData: ")
        HomeService.getInstance().getNextPageUrl()?.let {
            Log.e(TAG, " calling next page : " + it)
            HomeService.getInstance().getNextPageHome(it, { result ->
                homeData.value = result
                isNextPageFinished.postValue(true)

            }, {
                isNextPageFinished.postValue(true)
                pageNetException.postValue(it)
            })

            return
        }
        Log.e(TAG, " No page found : ")
        pageNetException.postValue(
            NetException(
                "No page found",
                "END_OF_LIST",
                404
            )
        )
    }

    fun setIsLoad(value: Boolean) {
        this.isLoad = value
    }

    fun getIsLoad(): Boolean {
        return this.isLoad
    }
//    fun getLocalCartItemCount() {
//        LocalCartService.instance.getAllCartItems(success = {
//            cartItemCount.postValue(it.size)
//            Log.d(TAG, "cart item count = ${it.size}")
//        })
//    }

}