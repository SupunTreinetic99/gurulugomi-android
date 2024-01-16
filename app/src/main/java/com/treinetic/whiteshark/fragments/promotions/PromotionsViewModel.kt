package com.treinetic.whiteshark.fragments.promotions

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.Promotions
import com.treinetic.whiteshark.services.PromotionsService
import com.treinetic.whiteshark.services.Service

class PromotionsViewModel : ViewModel() {

    private var promotions: MutableLiveData<Promotions> = MutableLiveData()
    private var netException: MutableLiveData<NetException> = MutableLiveData()


    fun getPromotions(): MutableLiveData<Promotions> {
        return promotions
    }

    fun getNetException(): MutableLiveData<NetException> {
        return netException
    }

    fun fetchPromotions() {
        PromotionsService.getInstance().fetchPromotions(Service.Success { result ->
            promotions.value = result

        }, Service.Error { exception ->
            netException.value = exception

        })
    }


}