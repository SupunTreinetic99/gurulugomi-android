package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Order(
    @SerializedName("id")
    var id: String,
    @SerializedName("user_customer_user_id")
    var userId: String,
    @SerializedName("total_paid")
    var totalPaid: Double,
    @SerializedName("total_discount")
    var totalDiscount: Double = 0.00,
    @SerializedName("total_amount")
    var totalAmount: Double = 0.00,
    @SerializedName("created_at")
    var createDate: String,
    @SerializedName("order_items")
    var orderItems: MutableList<OrderItem>,
    @SerializedName("date_time")
    var dateTime: String,
    @SerializedName("promotions")
    var promotions: MutableList<Promotion> = mutableListOf()
) {

    @SerializedName("status")
    var status: String? = null

    @SerializedName("loyalty")
    var loyaltyPoints: LoyaltyPoints? = null

    @SerializedName("promotion_code")
    var promotionCode: PromotionCode? = null

    @SerializedName("e_voucher")
    var eVoucher: EVoucher? = null


    fun hasPromotion(): Boolean {
        return totalDiscount != null && totalDiscount > 0.00
    }

    fun hasPromotions(): Boolean {
        return promotions.count() > 0
    }

    fun hasPromoCode(): Boolean {
        promotionCode?.let {
            return it.promotionCodeAmount > 0.0
        }
        return false
    }

    fun hasLoyalty(): Boolean {
        loyaltyPoints?.let {
            return it.loyaltyPointsAmount > 0.0
        }
        return false
    }

    fun hasEVoucher(): Boolean {
        eVoucher?.let {
            return it.eVoucherAmount > 0.0
        }
        return false
    }


    fun getOrderItemCurrency(): String {
        if (orderItems.isEmpty()) {
            return ""
        }
        return orderItems.first().book?.priceDetails?.currency ?: ""

    }





}