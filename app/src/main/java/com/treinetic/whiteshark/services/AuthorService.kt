package com.treinetic.whiteshark.services

import android.util.Log
import com.google.gson.Gson
import com.treinetic.whiteshark.constance.ErrorCodes
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.Authors
import com.treinetic.whiteshark.network.Net

class AuthorService {

    var gson = Gson()
    private val TAG = "AuthorService"
    var authorList: Authors? = null

    companion object {
        private var instance = AuthorService()
        fun getInstance(): AuthorService {
            return instance
        }
    }


    fun getAuthorPage(
        url: String?,
        success: Service.Success<Authors>,
        error: Service.Error

    ) {
        Log.d(TAG, "getAuthorPage : $url")
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
            Log.d(TAG, "getAuthorPage success")
            Log.d(TAG, response)
            try {
                val data = gson.fromJson(response, Authors::class.java)
                success.success(data)
            } catch (e: Exception) {
                e.printStackTrace()
                error.error(NetException.jsonParseError("Something went wrong"))
            }
        }, { exception ->
            error.error(exception)
        })
    }

    fun fetchAuthorList(success: Service.Success<Authors>, error: Service.Error, refresh: Boolean) {
        if (authorList != null && !refresh) {
            success.success(authorList)
            return
        }
        val net = Net(
            Net.URL.AUTHORS,
            Net.NetMethod.GET, null, null, null, null
        )

        net.perform({ response ->
            try {
                authorList = gson.fromJson(response, Authors::class.java)
                Log.d(TAG, "authorList.data.size ${authorList!!.data.size}")
                success.success(authorList)

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
            Log.e(TAG, "Error in fetch author list")
            error.error(exception)
        })
    }

    fun loadMore(url: String, success: Service.Success<Authors>, error: Service.Error) {
        val net = Net(
            url, Net.NetMethod.GET, null, null, null, null
        )
        net.perform({ response ->
            val updateAuthors = gson.fromJson(response, Authors::class.java)
            authorList?.data?.addAll(updateAuthors.data)
            val currentData = authorList?.data
            authorList = updateAuthors
            currentData?.let {
                authorList?.data = it
            }
            success.success(authorList)
        }, { exception ->
            error.error(exception)
        })
    }

}