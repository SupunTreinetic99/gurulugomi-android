package com.treinetic.google.androidx

import android.util.Log

import java.security.KeyStore
import java.util.*
import android.security.keystore.KeyProperties
import com.treinetic.whiteshark.MyApp.context
import android.security.KeyPairGeneratorSpec
import android.util.Base64
import com.treinetic.whiteshark.BuildConfig
import com.treinetic.whiteshark.services.LocalBookService
import tgio.rncryptor.RNCryptorNative
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.math.BigInteger
import java.security.KeyPairGenerator
import javax.crypto.*
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.x500.X500Principal
import kotlin.collections.ArrayList
import kotlin.math.abs


/**
 * Created by Nuwan on 2/28/19.
 */
public class gms {

    private val TAG = "EncryptUtil"

    init {
        phrase = getKey()
    }

    companion object {
        private const val KEY_ALIAS = BuildConfig.APPLICATION_ID
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        var bookEncryptKey = ""
        private val RSA_MODE = KeyProperties.KEY_ALGORITHM_RSA + File.separator +
                KeyProperties.BLOCK_MODE_ECB + File.separator +
                KeyProperties.ENCRYPTION_PADDING_NONE

        lateinit var phrase: String

        fun get(): gms {
            return gms()
        }
    }


    /**
     *  generate a RSA key pair to encrypt/decrypt the key which used to encrypt epub
     * */
    fun generateAlias() {

        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore.load(null)

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            Log.d(TAG, "Clear all data before create a new key")
            LocalBookService.getInstance().clearAll()

            // Generate a key pair for encryption
            val start = Calendar.getInstance()
            val end = Calendar.getInstance()
            end.add(Calendar.YEAR, 30)
            val spec = KeyPairGeneratorSpec.Builder(context)
                .setAlias(KEY_ALIAS)
                .setSubject(X500Principal("CN=$KEY_ALIAS"))
                .setSerialNumber(BigInteger.TEN)
                .setStartDate(start.time)
                .setEndDate(end.time)
                .build()
            val kpg = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA,
                ANDROID_KEY_STORE
            )
            kpg.initialize(spec)
            kpg.generateKeyPair()
        }

    }


    fun getKey(): String {
        try {
            generateAlias()
            val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
            keyStore.load(null)

//            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
//                val privateKeyStoreEntry = keyStore.getKey(KEY_ALIAS, null) as PrivateKey
//                return Base64.encodeToString(privateKeyStoreEntry.encoded, Base64.DEFAULT)
//            }
            val privateKeyStoreEntry =
                keyStore.getEntry(KEY_ALIAS, null) as KeyStore.PrivateKeyEntry
            return Base64.encodeToString(privateKeyStoreEntry.certificate.encoded, Base64.DEFAULT)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Something went wrong")
            return Base64.encodeToString(KEY_ALIAS.toByteArray(), Base64.DEFAULT)
        }

    }

    fun generateSecretKey(): String {
        val secretKey = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES).generateKey()
        val key = Base64.encodeToString(secretKey.encoded, Base64.DEFAULT)
        return key

    }


    fun encryptRSA(secret: ByteArray): ByteArray {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore.load(null)
        val privateKeyStoreEntry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.PrivateKeyEntry
//        Log.e(TAG, "encryptRSA RSA_MODE : $RSA_MODE")
        var inputCipher = Cipher.getInstance(RSA_MODE, "AndroidOpenSSL")
        inputCipher.init(Cipher.ENCRYPT_MODE, privateKeyStoreEntry.certificate.publicKey)

        val outputStream = ByteArrayOutputStream()
        val cipherOutputStream = CipherOutputStream(outputStream, inputCipher)
        cipherOutputStream.write(secret)
        cipherOutputStream.close()

        val byteArray = outputStream.toByteArray()
        return byteArray
    }


    fun decryptRSA(encrypted: ByteArray): ByteArray {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore.load(null)
        val privateKeyStoreEntry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.PrivateKeyEntry
//        Log.e(TAG, "decryptRSA RSA_MODE : $RSA_MODE")
        var outputCipher = Cipher.getInstance(RSA_MODE, "AndroidOpenSSL")
        outputCipher.init(Cipher.DECRYPT_MODE, privateKeyStoreEntry.privateKey)
        val cipherInputStream = CipherInputStream(ByteArrayInputStream(encrypted), outputCipher)
        return cipherInputStream.readBytes()

    }

    fun buildKey(token: String): String {
        Log.d(TAG, "buildKey : $token")
        var ints = arrayListOf<Int>()
        var key: String = ""
        var keyArray = arrayOf("a", "e", "o", "u")

        for (c in token) {
            if (keyArray.contains(c.toString().toLowerCase())) {
                ints.add(1)
            }
        }

        ints = ints.chunked(60)[0] as ArrayList<Int>

        ints.forEachIndexed { index, element ->
            var absXor = abs(index.xor(ints.size - index))
            val char = token.toCharArray()[absXor]
            var ascii = abs(char.toInt() - index)
            if (ascii == 0) ascii = index

            key += token.toCharArray()[ascii].toString()
            key += token.toCharArray()[token.length - ascii]
        }

        key = key.replace(".", "")
        key = key.replace(" ", "")

        return key
    }


    fun decryptEpubFile(input: String, password: String): String? {
        Log.d(TAG, "decryptEpubFile CALLING")
        Log.d(TAG, input)
        val rnCryptorNative = RNCryptorNative()
        var data = rnCryptorNative.decrypt(input, password)
        Log.d(TAG, "decrypted content")
        Log.d(TAG, data)

        data?.let {
            return it
        }
        return input
    }

    fun e(input: String): String {
        val rnCryptorNative = RNCryptorNative()
        var data = String(
            rnCryptorNative.encrypt(
                input,
                phrase
            )
        )

        return data
    }

    fun d(input: String): String {
        val rnCryptorNative = RNCryptorNative()
        var data = rnCryptorNative.decrypt(
            input,
            phrase
        )
        Log.d(TAG, "decrypted Data")
        Log.d(TAG, data)
        return data
    }


//    fun encrypt(input: ByteArray): String {
//        var c = Cipher.getInstance("AES/CBC/PKCS5Padding")
//        c.init(Cipher.ENCRYPT_MODE, getSecretKeySpec(), IvParameterSpec(ByteArray(16)))
//        var bytes = c.doFinal(input)
//        var base64String = Base64.encodeToString(bytes, Base64.DEFAULT)
//        return base64String
//    }
//
//    fun decrypt(encrypted: ByteArray): ByteArray {
//        var c = Cipher.getInstance("AES/CBC/PKCS5Padding")
//        c.iv
//        c.init(Cipher.DECRYPT_MODE, getSecretKeySpec(), IvParameterSpec(ByteArray(16)))
//
//        var byteArray = c.doFinal(encrypted)
//        return byteArray
//    }


    fun getSecretKeySpec(): SecretKeySpec {
        val pw = "123456"
        val salt = "nuwan"
        val iterations = 10
        val secKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val spec = PBEKeySpec(pw.toCharArray(), salt.toByteArray(), iterations, 128)
        val pbeSecretKey = secKeyFactory.generateSecret(spec)
        return SecretKeySpec(pbeSecretKey.getEncoded(), "AES")
    }


}