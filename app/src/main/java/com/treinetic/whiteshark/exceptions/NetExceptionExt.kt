package com.treinetic.whiteshark.exceptions

fun NetException.isLoginRequired(): Boolean {
    return message_id == "LOGIN_REQUIRED" && code == 403

}