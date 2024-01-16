package com.treinetic.whiteshark.constance


class Screens {


    companion object {
        private var instance = Screens()
        fun getInstance(): Screens {
            return instance
        }

        const val WISH_LIST = "wish_list"
        const val HOME = "home"
        const val SEARCH = "search"
        const val BOOK_PROFILE = "book_profile"
        const val LOGIN = "login"
        const val ABOUT = "about"
        const val ADD_REVIEW = "add_review"
        const val ALL_REVIEW = "all_review"
        const val BILLING_DETAILS = "billing_details"
        const val BILLING_HISTORY = "billing_history"
        const val BOOK_PUBLISH = "book_publish"
        const val EVENT_DETAILS = "event_details"
        const val EVENTS = "events"
        const val MY_LIBRARY = "my_library"
        const val PASSWORD_RESET = "password_reset"
        const val PROMOTION = "promotion"
        const val PURCHASE_HISTORY = "purchase_history"
        const val REGISTER = "register"
        const val SEARCH_RESULT = "search_result"
        const val USER_PROFILE = "user_profile"
        const val BOOK_CATEGORY = "book_category"
        const val CART = "cart"
        const val ORDER_CONFIRM = "order_confirm"
        const val CATEGORY = "category"
        const val PAYMENT = "payment"
        const val AUTHOR_BOOKS = "author_books"
        const val DEVICES = "device"
        const val REMOVE_DEVICES = "remove_device"
        const val PUBLISHER = "publishers"
        const val AUTHORS = "authors"
        const val PUBLISHER_BOOK = "publisher book"
        const val PROFILE_CATEGOREY = "profile category"
        const val REQUEST_OTP = "request_otp"
        const val REDEEM_LOYALTY_POINTS = "redeem_loyalty_points"
        const val OTP_CONFIRM_SHEET = "OtpConfirmBottomSheet"
        const val CHANGE_NUM_SHEET = "ChangeNumberBottomSheet"
        const val ADD_PROMO_SHEET = "AddPromoBottomSheet"
        const val PAY_NOW_SHEET = "PayNowBottomSheet"
    }


}