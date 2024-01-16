package com.treinetic.whiteshark.fragments.billingdetails

import android.util.Log
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.treinetic.whiteshark.FragmentNavigation
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.constance.ErrorCodes
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.BillingDetails
import com.treinetic.whiteshark.services.OrderService
import com.treinetic.whiteshark.services.Service
import com.treinetic.whiteshark.services.UserService

class BillingViewModel : ViewModel() {
    private val TAG = "BillingViewModel"
    private val billingDetails: MutableLiveData<BillingDetails> = MutableLiveData()
    private val netException: MutableLiveData<NetException> = MutableLiveData()
    private var billingDataSent: BillingDetails? = null

    init {
        loadBillingDetails()
    }

    fun getBillingDetails(): LiveData<BillingDetails> {
        return billingDetails
    }

    fun getNetException(): LiveData<NetException> {
        return netException
    }

    private fun loadBillingDetails() {
        OrderService.getInstance().getBillingDetails({ result ->
            if (result != null) {
                updateBillingData(result)
            }
            billingDetails.value = result
        }, { it ->
            NetException(
                "Something went  wrong",
                it.message,
                ErrorCodes.REQUEST_ERROR
            )

        })
    }

    fun updateBillingData(result: BillingDetails) {
        result.contact = UserService.getInstance().getUser()?.contact_number ?: ""
        result.countryCode = UserService.getInstance().getUser()?.country_code ?: ""
    }

    fun addBillingDetails(billingData: BillingDetails, fragmentManager: FragmentManager) {
        this.billingDataSent = billingData
        OrderService.getInstance().addBillingDetails(billingData, { result ->
            result.countryCode = billingDataSent!!.countryCode
            billingDetails.value = result
            Log.d(TAG, "addBillingDetails success")
            // FragmentNavigation.getInstance().startOrderConfirm(fragmentManager, R.id.fragment_view)
            fragmentManager.popBackStack()
        }, { exception ->
            netException.value = exception

        })

    }

}