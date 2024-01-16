package com.treinetic.whiteshark.fragments.publishers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.Publisher
import com.treinetic.whiteshark.services.PublisherService
import com.treinetic.whiteshark.services.Service


class PublishersViewModel : ViewModel() {

    val TAG = "PublishersViewModel"

    val publisherList: MutableLiveData<MutableList<Publisher>> = MutableLiveData()
    val netException: MutableLiveData<NetException> = MutableLiveData()
    var currentPageUrl: String? = null
    var isFetch = false


    fun fetchPublisherList(refresh: Boolean) {
        PublisherService.getInstance().fetchPublisherList(
            { result ->
                publisherList.value = result.data
                result.next_page_url.let {
                    currentPageUrl = it
                }
            }, { exception ->
                netException.value = exception
            }, refresh
        )
    }

    fun loadMore(url: String) {
        PublisherService.getInstance().loadMore(url, { result ->
            publisherList.value = result.data
            result.next_page_url.let {
                currentPageUrl = it
            }

        }, { exception ->
            netException.value = exception

        })
    }

}