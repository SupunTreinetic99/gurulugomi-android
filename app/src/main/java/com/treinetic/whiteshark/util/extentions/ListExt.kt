package com.treinetic.whiteshark.util.extentions


fun <T> List<T>.toArrayList(): ArrayList<T> {
    return ArrayList(this)
}
