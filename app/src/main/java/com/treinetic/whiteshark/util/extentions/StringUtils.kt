package com.treinetic.whiteshark.util.extentions

import com.google.gson.Gson
import com.treinetic.whiteshark.models.Book
import java.io.File

/**
 * Created by Nuwan on 2/28/19.
 */


fun String.toBook(): Book? {

    try {
        val gson = Gson()
        var book = gson.fromJson(this, Book::class.java)
        return book
    } catch (exception: Exception) {
        exception.printStackTrace()
    }
    return null

}


fun Book.serializedBook(): String {

    try {
        val gson = Gson()
        var book = gson.toJson(this)
        return book
    } catch (exception: Exception) {
        exception.printStackTrace()
    }

    return ""
}

fun String.getCountryCodeAsInt(): Int {
    var a = this.replace("+", "")
    try {
        return a.toInt()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return -1
}


fun String.getFileName(): String {
    return this.split(File.separator).last()
}

fun ArrayList<Int>.mcg(): String {
    var i = ""
    this.forEach { c ->
        i += (c - 0x45).toChar()
    }
    return i
}

fun Any.toJson(): String {

    try {
        val gson = Gson()
        return gson.toJson(this)
    } catch (exception: Exception) {
        exception.printStackTrace()
    }

    return ""
}
