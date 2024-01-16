package com.treinetic.whiteshark.util.extentions

import java.text.DecimalFormat

/**
 * Created by Nuwan on 2/20/19.
 */

fun Double.toCurrency(currency: String): String {
    return currency + " " + ("%.2f".format(this))
}

fun Double.toOffer(beforeSign: String = "", afterSign: String = ""): String {
    val dec = DecimalFormat("0.##")
//    if (this % 1 == 0.00) {
//
//        return "$beforeSign${dec.format(this)}$afterSign"
//    }
    return "$beforeSign${dec.format(this)}$afterSign"
//    return "$beforeSign$this$afterSign"
}

