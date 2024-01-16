package com.treinetic.whiteshark.activity.splash

import android.util.Base64
import android.util.Log
import com.treinetic.google.androidx.gms
import tgio.rncryptor.RNCryptorNative
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

fun SplashActivity.encrypt(input: ByteArray): String {
    var c = Cipher.getInstance("AES/CBC/PKCS5Padding")
    c.init(Cipher.ENCRYPT_MODE, getSecretKeySpec(), IvParameterSpec(ByteArray(16)))
    var bytes = c.doFinal(input)
    var base64String = Base64.encodeToString(bytes, Base64.DEFAULT)
    return base64String
}


fun SplashActivity.decrypt(encrypted: ByteArray): ByteArray {
    var c = Cipher.getInstance("AES/CBC/PKCS5Padding")
    c.iv
    c.init(Cipher.DECRYPT_MODE, getSecretKeySpec(), IvParameterSpec(ByteArray(16)))

    var byteArray = c.doFinal(encrypted)
    return byteArray
}


fun SplashActivity.getSecretKeySpec(): SecretKeySpec {
    val pw = "123456"
    val salt = "nuwan"
    val iterations = 10
    val secKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    val spec = PBEKeySpec(pw.toCharArray(), salt.toByteArray(), iterations, 128)
    val pbeSecretKey = secKeyFactory.generateSecret(spec)
    return SecretKeySpec(pbeSecretKey.getEncoded(), "AES")
}


/*
fun SplashActivity.testKeysEncryption() {
    val encryptUtil = EncryptUtil()
    val myString = "nuwan"
    val a = myString.toByteArray()
    val b = String(a)
    Log.e("testKeysEncryption", "string from byteArray : " + b)

    var enc = encrypt(myString.toByteArray())
    println(" encrypted : " + enc)

    var x = Base64.decode(enc, Base64.DEFAULT)
    var decy = String(decrypt(x))
    println(" encrypted : " + decy)

    val password =
        "DCNhi0JcDmNJju0WWjds3iJfTTEa11Z1LIOJi0cgNJNpNpMaZlLy3iQ-Z1Jfx0e2jCdx6iICFzB4oI-D2iswVi-"
    encryptUtil.decryptEpubFile(encryptUtil.testHtml, password)
}
*/


fun SplashActivity.getEncryptionPrivateKey() {

    val encryptUtil = gms()
    val key = encryptUtil.getKey()

    val a = RNCryptorNative().encrypt("Nuwan", key)
    Log.e(TAG, " generated Key : $key")
    Log.e(TAG, " Processed : $a")

}