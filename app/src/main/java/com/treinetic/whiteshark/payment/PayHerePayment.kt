package com.treinetic.whiteshark.payment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.treinetic.whiteshark.MyApp
import com.treinetic.whiteshark.models.PaymentData
import com.treinetic.whiteshark.util.extentions.mcg
import com.treinetic.whiteshark.util.extentions.toJson
import lk.payhere.androidsdk.PHConfigs
import lk.payhere.androidsdk.PHConstants
import lk.payhere.androidsdk.PHMainActivity
import lk.payhere.androidsdk.PHResponse
import lk.payhere.androidsdk.model.InitRequest
import lk.payhere.androidsdk.model.StatusResponse

@Keep
class PayHerePayment : PaymentImpl {
    var req = InitRequest()
    val PAYHERE_REQUEST = 11010
    val TAG = "PayHerePayment"

    var merchantId: String = "21572821"
    var g = arrayListOf(121, 148, 154, 172, 120, 181, 150, 178, 137, 150, 183, 121, 153, 184, 166, 171, 175, 184, 135, 145, 137, 144, 125, 173, 174, 187, 181, 168, 139, 185, 170, 152, 149, 121, 166, 126, 185, 159, 118, 152, 138, 141, 151, 169)
    var currency: String = "LKR"
    var amount: Double = 0.00
    var orderId: String = "000"
    var itemsDescription: String = "Sasd"
    var custom1: String = "asd1"
    var custom2: String = "asd2"

    var firstName: String = ""
    var lastName: String = ""
    var email: String = ""
    var phone: String = ""
    var address: String = ""
    var city: String = ""
    var country: String = ""

    var success: ((message: String?) -> Unit)? = null
    var error: ((message: String?) -> Unit)? = null
    var cancelled: ((message: String?) -> Unit)? = null
    var response: PHResponse<StatusResponse>? = null

    var orderData: Any? = null

    override fun prepare(data: Any?) {
        data?.let {
            if (it !is PaymentData) {
                Log.e(TAG, "prepare(data: Any?)->Type mismatch")
                return
            }
            orderData = it
            req.merchantId = it.paymentInfo.merchantId
            req.merchantSecret = g.mcg()
            req.currency = it.paymentInfo.currency
            req.amount = it.paymentInfo.amount
            req.orderId = it.paymentInfo.orderId.toString()
            req.itemsDescription = it.paymentInfo.items
            req.custom1 = "ANDROID"
            req.custom2 = ""
            req.customer.firstName = it.paymentInfo.firstName
            req.customer.lastName = it.paymentInfo.lastName
            req.customer.email = it.paymentInfo.email
            req.customer.phone = it.paymentInfo.phone
            req.customer.address.address = it.paymentInfo.address
            req.customer.address.city = it.paymentInfo.city
            req.customer.address.country = it.paymentInfo.country
            req.returnUrl = it.paymentInfo.returnUrl
            req.cancelUrl = it.paymentInfo.cancelUrl
            req.notifyUrl = it.paymentInfo.notifyUrl
            Log.d(
                TAG,
                "prepare(data: Any?)-> merchantId =${req.merchantId} merchantSecret=${req.merchantSecret}"
            )
            Log.d(TAG, "prepare(data: Any?)->Invoice data set done..")
            return
        }

        Log.e(TAG, "prepare(data: Any?)->Invoice data not set..")

    }

    override fun doPayment() {
        TODO("Do not use in PayHere Payment")
    }

    override fun doPayment(activity: Activity) {
        Log.d(TAG, "doPayment(activity: Activity) -> called")
        val intent = Intent(activity, PHMainActivity::class.java)
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req)
        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL)
        activity.startActivityForResult(intent, PAYHERE_REQUEST)
    }

    override fun doPayment(fragment: Fragment) {
        Log.d(TAG, "doPayment(fragment: Fragment) -> called")
        val intent = Intent(fragment.activity, PHMainActivity::class.java)
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req)
        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL)
        fragment.startActivityForResult(intent, PAYHERE_REQUEST)
    }


    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PAYHERE_REQUEST && data != null
            && data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)
        ) {
            response =
                data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT) as PHResponse<StatusResponse>?
            Log.d(TAG, "onActivityResult -> result : +${response?.toString()} ")
            if (resultCode == Activity.RESULT_OK) {

                if (response == null) {
                    Log.d(TAG, "Response is null...")
                    error?.let { it("Something went wrong") }
                    FirebaseCrashlytics.getInstance().recordException(
                        Exception("PayHerePayment -> onActivityResult Response data null. order ${(orderData as PaymentData).toJson()}")
                    )
                }

                if (response!!.isSuccess) {
                    paymentSuccessEventSubmit()
                    success?.let { it(response?.data?.message ?: "Payment success") }

                    return
                }
                error?.let { it(response!!.data.toString()) }

                return
            }

            if (resultCode == Activity.RESULT_CANCELED) {
                var message = "Payment request cancelled"

                response?.data?.message?.let {
                    message = it
                }

                if (response?.data == null && response?.status == -1) {
                    response.toString().split("message='")?.let { list->
                        if (list.size>1) {
                           message= list[1].split("',").first()
                        }
                    }
                }
                cancelled?.let { it(message) }
            }
        }
    }


    private fun paymentSuccessEventSubmit() {
        val item = Bundle()
        orderData as PaymentData
        val paymentInfo = (orderData as PaymentData).paymentInfo
        item.putString(FirebaseAnalytics.Param.ITEM_ID, paymentInfo.orderId.toString())
        item.putDouble(FirebaseAnalytics.Param.VALUE, paymentInfo.amount)
        item.putString(FirebaseAnalytics.Param.CURRENCY, paymentInfo.currency)
        item.putString(FirebaseAnalytics.Param.PAYMENT_TYPE, "PAYHERE")
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.PURCHASE, item)
    }

    override fun clear() {
        success = null
        error = null
        cancelled = null
        response = null
    }

}