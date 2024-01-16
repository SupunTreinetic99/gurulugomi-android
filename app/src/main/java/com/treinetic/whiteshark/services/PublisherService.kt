package com.treinetic.whiteshark.services

import android.util.Log
import com.google.gson.Gson
import com.treinetic.whiteshark.constance.ErrorCodes
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.Publishers
import com.treinetic.whiteshark.network.Net

class PublisherService {

    private val TAG = "PublishersService"
    private val gson = Gson()
    var publisherList: Publishers? = null

    companion object {

        private var instance = PublisherService()
        fun getInstance(): PublisherService {
            return instance
        }

    }

    fun getPublisherPage(
        url: String?,
        success: Service.Success<Publishers>,
        error: Service.Error

    ) {
        Log.d(TAG, "getPublisherPage : $url")
        url ?: return
        val net = Net(
            url,
            Net.NetMethod.GET,
            null,
            null,
            null,
            null
        )
        net.perform({ response ->
            Log.d(TAG, "getPublisherPage success")
            Log.d(TAG, response)
            try {
                val data = gson.fromJson(response, Publishers::class.java)
                success.success(data)
            } catch (e: Exception) {
                e.printStackTrace()
                error.error(NetException.jsonParseError("Something went wrong"))
            }
        }, { exception ->
            error.error(exception)
        })
    }

    fun fetchPublisherList(
        success: Service.Success<Publishers>,
        error: Service.Error,
        refresh: Boolean
    ) {
        if (publisherList != null && !refresh) {
            success.success(publisherList)
            return
        }
        val net = Net(
            Net.URL.PUBLISHERS,
            Net.NetMethod.GET, null, null, null, null
        )

        net.perform({ response ->
            try {
                publisherList = gson.fromJson(response, Publishers::class.java)
                Log.d(TAG, "publisherList.data.size ${publisherList!!.data.size}")
                success.success(publisherList)

            } catch (e: Exception) {
                e.printStackTrace()
                error.error(
                    NetException(
                        "Something went wrong",
                        "JSON_ERROR",
                        ErrorCodes.JSON_ERROR
                    )
                )
            }

        }, { exception ->
            Log.e(TAG, "Error in fetch publisher list")
            error.error(exception)
        })
    }

    fun loadMore(url: String, success: Service.Success<Publishers>, error: Service.Error) {
        val net = Net(
            url, Net.NetMethod.GET, null, null, null, null
        )
        net.perform({ response ->
            val updatePublisher = gson.fromJson(response, Publishers::class.java)
            publisherList?.data?.addAll(updatePublisher.data)
            val currentData = publisherList?.data
            publisherList = updatePublisher
            currentData?.let {
                publisherList?.data = it
            }
            success.success(publisherList)
        }, { exception ->
            error.error(exception)
        })
    }
}