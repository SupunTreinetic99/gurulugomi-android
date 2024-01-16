package com.treinetic.whiteshark.services

import com.google.gson.Gson
import com.treinetic.whiteshark.models.Promotions
import com.treinetic.whiteshark.network.Net

class PromotionsService {

    companion object {
        private val newInstance = PromotionsService()
        fun getInstance(): PromotionsService {
            return newInstance
        }
    }


    fun fetchPromotions(success: Service.Success<Promotions>, error: Service.Error) {

        val net = Net(
            Net.URL.PROMOTIONS,
            Net.NetMethod.GET,
            null,
            null,
            null,
            null
        )
        net.perform({ response ->
            try {
                val promotions = Gson().fromJson<Promotions>(response, Promotions::class.java)
                success.success(promotions)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, { exception ->
            exception.printStackTrace()
        })

    }
}