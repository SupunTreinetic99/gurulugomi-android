package com.treinetic.whiteshark.util


import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.treinetic.whiteshark.MyApp
import com.treinetic.whiteshark.MyApp.context
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class Connections() {

    var networkListerner: ((connectivity: Connectivity?) -> Unit)? = null
    var connectivity: MutableLiveData<Connectivity> = MutableLiveData()
    var isConnected = true

    companion object {
        private val instance = Connections()
        fun getInstance(): Connections {
            return instance
        }

    }

    fun isNetworkConnected(): Boolean {

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo

        return activeNetwork?.isConnectedOrConnecting == true

    }

    @SuppressLint("CheckResult")
    fun listenToNetwork() {
        ReactiveNetwork.observeNetworkConnectivity(MyApp.getAppContext())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { connectivity: Connectivity? ->
                connectivity?.let {
                    Log.e("Connections", " state : " + it.state())
                    isConnected = isConnected(connectivity)

                }
                this.connectivity.postValue(connectivity)
                networkListerner?.let {
                    it(connectivity)
                }
            }
    }

    fun isConnected(connectivity: Connectivity): Boolean {
        return connectivity.state() == NetworkInfo.State.CONNECTED
    }

}