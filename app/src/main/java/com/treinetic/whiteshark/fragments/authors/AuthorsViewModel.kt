package com.treinetic.whiteshark.fragments.authors

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.Author
import com.treinetic.whiteshark.services.AuthorService
import com.treinetic.whiteshark.services.Service

class AuthorsViewModel : ViewModel() {

    val TAG = "AuthorsViewModel"


    val authorList: MutableLiveData<MutableList<Author>> = MutableLiveData()
    val netException: MutableLiveData<NetException> = MutableLiveData()
    var currentPageUrl: String? = null
    var isFetch = false


    fun fetchAuthorList(refresh: Boolean) {
        AuthorService.getInstance().fetchAuthorList(
            { result ->
                authorList.value = result.data
                result.next_page_url.let {
                    currentPageUrl = it
                }
            }, { exception ->
                netException.value = exception
            }, refresh
        )
    }

    fun loadMore(url: String) {
        AuthorService.getInstance().loadMore(url, { result ->
            authorList.value = result.data
            result.next_page_url.let {
                currentPageUrl = it
            }

        }, { exception ->
            netException.value = exception

        })
    }
}