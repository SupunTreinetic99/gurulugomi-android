package com.treinetic.whiteshark.services

import android.util.Log
import com.google.gson.Gson
import com.treinetic.whiteshark.constance.ErrorCodes
import com.treinetic.whiteshark.constance.SearchTypes
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.Books
import com.treinetic.whiteshark.models.SearchResult
import com.treinetic.whiteshark.network.Net

/**
 * Created by Nuwan on 2/18/19.
 */
class SearchService {

    private val TAG = "SearchService"
    var searchData: SearchResult? = null
    var authorBooks: Books? = null

    companion object {

        private val instance = SearchService()
        fun getInstance(): SearchService = instance
    }


    fun search(term: String, success: Service.Success<SearchResult>, error: Service.Error) {

        val map = mutableMapOf<String, Any>()
        map["searchTerm"] = term

        val net = Net(
            Net.URL.SEARCH,
            Net.NetMethod.GET,
            null,
            map,
            null,
            null
        )
        net.perform({ response ->
            run {
                Log.d(TAG, "Success search")
                try {
                    searchData = Gson().fromJson(response, SearchResult::class.java)
                    success.success(searchData)
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                    error.error(
                        NetException(
                            "Something went",
                            "json_error",
                            ErrorCodes.JSON_ERROR
                        )
                    )
                }
            }
        }, { exception ->
            error.error(exception)
            Log.e(TAG, exception.toString())
        })

    }


    fun getSearchSection(type: String): Any? {

        searchData?.let {
            when (type) {
                SearchTypes.SEARCH_BOOKS -> {
                    return it.epubs
                }
                SearchTypes.SEARCH_PROMOSTION -> {
                    return it.promotions
                }
                SearchTypes.SEARCH_AUTHORS -> {
                    return it.authors
                }
                SearchTypes.SEARCH_PUBLISHERS -> {
                    return it.publishers
                }
                else -> {
                    return null
                }
            }
        }

        return null
    }

    fun clearSearchData() {
        searchData = null
    }

    fun getNextBooksNextPage() {

    }


}