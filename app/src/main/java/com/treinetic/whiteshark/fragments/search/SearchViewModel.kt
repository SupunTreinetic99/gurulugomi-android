package com.treinetic.whiteshark.fragments.search

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.SearchResult
import com.treinetic.whiteshark.services.SearchService
import com.treinetic.whiteshark.services.Service

/**
 * Created by Nuwan on 2/18/19.
 */
class SearchViewModel : ViewModel() {

    private var TAG = "SearchViewModel"
    var searchResult: MutableLiveData<SearchResult> = MutableLiveData()
    var error: MutableLiveData<NetException> = MutableLiveData()
    var previousSearchTerm = ""

    init {
        SearchService.getInstance().searchData?.let {
            searchResult.postValue(it)
        }
    }
//
//    fun getSearchResult(): MutableLiveData<SearchResult> {
//        return searchResult
//    }

    fun clearError() {
        error.value = null
    }

    fun resetSearchData() {
        searchResult.value = null
        previousSearchTerm = ""
//        searchResult = MutableLiveData()
    }


    fun search(term: String) {
        previousSearchTerm = term
        SearchService.getInstance().search(term, Service.Success {
            Log.d(TAG, "search success")
            searchResult.postValue(it)
        }, Service.Error {
            error.postValue(it)
        })

    }


}