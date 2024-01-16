package com.treinetic.whiteshark.services

import android.util.Log
import com.google.gson.Gson
import com.treinetic.whiteshark.constance.ErrorCodes
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.Event
import com.treinetic.whiteshark.models.Events
import com.treinetic.whiteshark.network.Net
import java.lang.Exception

/**
 * Created by Nuwan on 2/6/19.
 */
class EventService {

    private var gson = Gson()
    var eventList: Events? = null

    private var TAG = "EventService"

    companion object {

        private var instance = EventService()
        fun getInstance(): EventService {
            return instance
        }
    }

    fun fetchEventList(success: Service.Success<Events>, error: Service.Error, refresh: Boolean) {
        if (eventList != null && !refresh) {
            success.success(eventList)
            return
        }
        val net = Net(
            Net.URL.EVENT,
            Net.NetMethod.GET,
            null,
            null,
            null,
            null
        )

        net.perform({ response ->
            try {
                eventList = gson.fromJson(response, Events::class.java)
                success.success(eventList)
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
            Log.e(TAG, " Error in fetch events")
            error.error(exception)
        })

    }


    fun getEvent(id: String?): Event? {
        if (id == null) return null

        eventList?.data?.forEach { event ->
            if (event.id.equals(id)) return event
        }
        return null
    }

    fun loadMore(url: String, success: Service.Success<Events>, error: Service.Error) {
        val net = Net(
            url, Net.NetMethod.GET, null, null, null, null
        )
        net.perform({ response ->
            val updateEvents = gson.fromJson(response, Events::class.java)
            eventList?.data?.addAll(updateEvents.data)
            val currentData = eventList?.data
            eventList = updateEvents
            currentData?.let {
                eventList?.data = it
            }
            success.success(eventList)
        }, { exception ->
            error.error(exception)
        })
    }

}