package com.treinetic.whiteshark.util.extentions

import android.util.Log
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception


fun File.deleteFileOrDirectory() {
    try {
        if (!this.exists()) return
        if (this.isDirectory) {
            this.deleteRecursively()
            return
        }
        this.delete()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


fun File.save(): String? {
    try {
        var fos = FileOutputStream(this)
        fos.flush()
        fos.close()
        return this.absolutePath
    } catch (e: Exception) {
        return null
    }
}

fun File.saveBytes(bytes: ByteArray) {
    try {
        var bis = ByteArrayInputStream(bytes)
        var fos = FileOutputStream(this)
        bis.copyTo(fos)
        fos.flush()
        fos.close()
    } catch (e: Exception) {
        e.printStackTrace()

    }
}

fun File.moveTo(path: String, fileName: String): String? {
    if (!this.exists()) return null
    var destionationDir = File(path)
    destionationDir.mkdirs()
    var outPutFile = File(path + File.separator + fileName)
    Log.d("File.moveTo", "File.moveTo before file moved Time : " + System.currentTimeMillis())
    try {
        var fin = FileInputStream(this)
        var fos = FileOutputStream(outPutFile)
        fin.copyTo(out = fos)
        fos.flush()
        fos.close()
        Log.d("File.moveTo", "file moved to ${outPutFile.absolutePath}")
        Log.d("File.moveTo", "File.moveTo after file moved Time : " + System.currentTimeMillis())
        return outPutFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

