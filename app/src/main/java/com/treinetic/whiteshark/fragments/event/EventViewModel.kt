package com.treinetic.whiteshark.fragments.event

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.Event
import com.treinetic.whiteshark.services.EventService
import com.treinetic.whiteshark.services.Service

/**
 * Created by Nuwan on 2/6/19.
 */
class EventViewModel : ViewModel() {

    private var TAG = "EventViewModel"
    private val eventList: MutableLiveData<MutableList<Event>> = MutableLiveData()
    private val netException: MutableLiveData<NetException> = MutableLiveData()
    private var currentPageUrl: String? = null
    var isFetch = false

    fun getEventList(): LiveData<MutableList<Event>> {
        return eventList
    }

    fun getNetException(): LiveData<NetException> {
        return netException
    }

    fun setPageUrl(url: String?) {
        currentPageUrl = url
    }

    fun getPageUrl(): String? {
        return currentPageUrl
    }

    fun fetchEventList(refresh: Boolean) {
        EventService.getInstance().fetchEventList(
            Service.Success { result ->
                eventList.value = result.data
                result.next_page_url.let {
                    setPageUrl(it)
                }
            }, Service.Error { exception ->
                netException.value = exception
            }, refresh
        )
    }

    fun loadMore(url: String) {
        EventService.getInstance().loadMore(url, Service.Success { result ->
            eventList.value = result.data
            result.next_page_url.let {
                setPageUrl(it)
            }

        }, Service.Error { exception ->
            netException.value = exception

        })
    }

}