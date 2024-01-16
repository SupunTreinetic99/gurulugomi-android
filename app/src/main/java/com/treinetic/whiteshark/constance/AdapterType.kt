package com.treinetic.whiteshark.constance

class AdapterType {

    companion object {
        private val instance = AdapterType()
        fun getInstance(): AdapterType {
            return instance
        }

        val WISHLIST = "wish_list"
        val CART = "cart"
        val ORDER_CONFIRM = "order_confirm"
    }
}