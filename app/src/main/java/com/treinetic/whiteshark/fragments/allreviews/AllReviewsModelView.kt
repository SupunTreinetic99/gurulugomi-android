package com.treinetic.whiteshark.fragments.allreviews

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.AllRatings
import com.treinetic.whiteshark.services.BookService
import com.treinetic.whiteshark.services.Service

class AllReviewsModelView : ViewModel() {

    private var reviews: MutableLiveData<AllRatings> = MutableLiveData()
    private var netException: MutableLiveData<NetException> = MutableLiveData()
    var isFetch = false


    fun getReviews(): MutableLiveData<AllRatings> {
        return reviews
    }

    fun getNetException(): MutableLiveData<NetException> {
        return netException
    }

    fun fetchReviews(id: String) {
        if (!isFetch) {
            BookService.getInstance().getAllReviews(id, Service.Success { result ->
                reviews.value = result

            }, Service.Error { exception ->
                netException.value = exception

            })
        }
    }

    fun loadMore(url: String, id: String) {
        BookService.getInstance().loadMore(url, id, Service.Success { result ->
            reviews.value = result
        }, Service.Error { exception ->
            netException.value = exception

        })

    }

}