package com.treinetic.whiteshark.util

class FiledValidate {

    companion object {

        @JvmStatic
        val EMAIL_REGEX = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"

        fun isEmailValid(email: String): Boolean {
            return EMAIL_REGEX.toRegex().matches(email)
        }


        @JvmStatic
//        val MOBILE_REGEX = "^[0][1-9]\\d{8}\$|^[1-9]\\d{8}\$"
        val MOBILE_REGEX =
//            "^([\\+][0-9]{1,3}([ \\.\\-])?)?([\\(]{1}[0-9]{3}[\\)])?([0-9A-Z \\.\\-]{9,14})((x|ext|extension)?[0-9]{1,4}?)\$"
            "^([\\(]{1}[0-9]{3}[\\)])?([0-9A-Z \\.\\-]{9,14})\$"

        fun isMobile(mobile: String): Boolean {
            return MOBILE_REGEX.toRegex().matches(mobile)

        }
    }

}