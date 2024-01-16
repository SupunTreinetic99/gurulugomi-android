package com.treinetic.google.androidx

import androidx.annotation.Keep

@Keep
class Shifter {

    fun countAN(key: String) = key.toCharArray().filter {
        it.isDigit()
    }.size

    fun buildOriginal(bytes: ByteArray, key: String): ByteArray {
        var count = countAN(key)
        var n = 9
        var pureBytes = bytes.takeLast(bytes.size - n).toTypedArray()

        var firstSet = pureBytes.take(count).reversed().toTypedArray()
        var lastSet = pureBytes.takeLast(count).toTypedArray()
        for (x in 0 until count) {
            pureBytes[x] = firstSet[x]
            pureBytes[pureBytes.size-1 - x] = lastSet[x]
        }

        var backPart = pureBytes.take((pureBytes.size / 2)).toTypedArray()
        var frontPart = pureBytes.takeLast(pureBytes.size - ((pureBytes.size / 2))).toTypedArray()
        var finalOutput = frontPart + backPart
        return finalOutput.toByteArray()
    }
}