package com.treinetic.whiteshark.fragments.login

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.Login
import com.treinetic.whiteshark.services.*
import com.treinetic.whiteshark.util.InitDataFetcher


class LoginViewModel : ViewModel() {

    private var initLogin: MutableLiveData<Login> = MutableLiveData()
    private var netException: MutableLiveData<NetException> = MutableLiveData()
    private val TAG = "LoginViewModel"
    private lateinit var logindata: Login
    private var initDataFetcher = InitDataFetcher().apply {
        onFinish = ::onFinishAllRequests
        onErrror = ::onError
    }

    fun getInitLogin(): MutableLiveData<Login> {
        return initLogin
    }

    fun getNetException(): MutableLiveData<NetException> {
        return netException
    }

    fun normalLogin(email: String, pwd: String, deviceId: String, deviceName: String) {
        UserService.getInstance()
            .userNormalLogin(email, pwd, deviceId, deviceName, { result ->
                logindata = result
//                getHomeData()
                initDataFetcher.beginFetch()
            }, { exception ->
                netException.value = exception

            })

    }

    fun appleLogin(activity: AppCompatActivity, token: String) {
        Log.e(TAG, "appleLogin time : ${System.currentTimeMillis()}")
        AppleLoginService.getInstance().doLogin(token, activity, { result ->
            Log.e(TAG, "appleLogin time : ${System.currentTimeMillis()}")
            logindata = result
            initDataFetcher.beginFetch()
        }, { exception ->
            netException.value = exception
        })
    }

    fun fBLogin(activity: AppCompatActivity) {
        Log.e(TAG, "FbLogin time in VM : ${System.currentTimeMillis()}")
        FacebookLoginService.getInstance().doLogin(activity, { result ->
            Log.e(TAG, "FbLogin time : ${System.currentTimeMillis()}")
//            getHomeData()
            logindata = result
            initDataFetcher.beginFetch()
        }, { exception ->
            netException.value = exception
        })
    }

    fun gPlusLogin(activity: AppCompatActivity) {
        Log.e(TAG, "gPlusLogin time : ${System.currentTimeMillis()}")
        GPlusLoginService.getInstance().doLogin(activity, { result ->
            Log.e(TAG, "gPlusLogin time : ${System.currentTimeMillis()}")
            logindata = result
//            getHomeData()
            initDataFetcher.beginFetch()
        }, { exception ->
            netException.value = exception

        })

    }

    private fun getHomeData() {
        HomeService.getInstance().fetchHomePageData({ result ->

            getCategories()
        }, { exception ->
            netException.value = exception
        })
    }

    private fun getCategories() {
        CategoryService.getInstance().fetchCategoryList({ result ->
            fetchCart()
        }, { exception ->
            exception.printStackTrace()
            netException.postValue(exception)
        })
    }

    private fun fetchCart() {
        LocalCartService.instance.updateUserCartWithLocalCartItems({
            fetchMyLibrary()

        }, { netException.postValue(it) })
    }

    private fun fetchMyLibrary() {
        BookService.getInstance().fetchMyLibrary({ result ->
            fetchWishlist()
        }, { exception ->
            netException.value = exception
        })
    }

    private fun fetchWishlist() {
        WishListService.getInstance().fetchWishList({ result ->
            initLogin.value = logindata
        }, { exception ->
            netException.value = exception
        })
    }


    fun onFinishAllRequests() {
        initLogin.value = logindata
    }

    fun onError(exception: NetException) {
        netException.postValue(exception)
    }

//    private fun fetchLogin(provider: String, token: String) {
//        val deviceId = DeviceInformation.getInstance().getDeviceId()
//        val deviceName = DeviceInformation.getInstance().getDeviceName()
//
//        UserService.getInstance().socialLogin(provider, token, deviceId, deviceName, Service.Success { result ->
//            initLogin.value = result
//        },
//            Service.Error { exception ->
//
//            })
//    }


}