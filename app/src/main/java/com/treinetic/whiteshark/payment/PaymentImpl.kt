package com.treinetic.whiteshark.payment

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment

interface PaymentImpl {


    fun prepare(data:Any?)
    fun doPayment()
    fun doPayment(activity: Activity)
    fun doPayment(fragment: Fragment)
    fun clear()

}