package com.treinetic.whiteshark.constance

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Environment
import android.security.KeyPairGeneratorSpec
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.treinetic.whiteshark.BuildConfig
import com.treinetic.whiteshark.MyApp
import com.treinetic.whiteshark.MyApp.context
import com.treinetic.whiteshark.roomdb.AppDatabase
import com.treinetic.whiteshark.roomdb.models.DeviceInfo
import java.io.ByteArrayOutputStream
import java.io.File
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.util.*
import javax.security.auth.x500.X500Principal
import kotlin.concurrent.thread

class DeviceInformation {

    private var uniqueID: String? = null
    private val PREF_UNIQUE_ID = "com.treinetic.whiteshark.uuid"
    private var TAG: String = "DeviceInformation"
    private var UUIDFromFile: String? = null

    companion object {
        private val instance = DeviceInformation()
        fun getInstance(): DeviceInformation {
            return instance
        }
    }

    private fun initTelePhonyManager(context: Context): TelephonyManager {
        return context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }

    @SuppressLint("MissingPermission", "HardwareIds")
    fun getDeviceId(): String {
/*

        try {
            val manager = initTelePhonyManager(context)
            val deviceId: String? = manager.deviceId
            deviceId?.let {
                FirebaseCrashlytics.getInstance().setCustomKey("identifier-imei", it)
                Log.d("DeviceInformation", "deviceId : $it")
                return deviceId
            }
        } catch (e: Exception) {
            Log.e(TAG, "IMEI is is not available")
//            e.printStackTrace()
        }

        try {
            val androidId: String? =
                Settings.Secure.getString(MyApp.context.contentResolver, Settings.Secure.ANDROID_ID)
            androidId?.let {
                FirebaseCrashlytics.getInstance().setCustomKey("identifier-android-id", it)
                Log.d("DeviceInformation", "androidId : $it")
                return it
            }
        } catch (e: Exception) {
            Log.e(TAG, "Android Id not available")
//            e.printStackTrace()
        }
*/

//        return BuildConfig.APPLICATION_ID

        var deviceIdentifier = getUUID(context)
        Log.d(TAG, "UUID generatd for identifier :$deviceIdentifier ")
        FirebaseCrashlytics.getInstance().setCustomKey("identifier-uuid", deviceIdentifier)
        return deviceIdentifier
    }


    @Synchronized
    fun getUUID(context: Context): String {
        if (uniqueID == null) {
            val sharedPrefs: SharedPreferences = context.getSharedPreferences(
                PREF_UNIQUE_ID, Context.MODE_PRIVATE
            )
/*
            uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null)*/
            uniqueID = getUUIdFormSharedPreference()

            UUIDFromFile?.let {
                if (uniqueID == null) {
                    uniqueID = UUIDFromFile
                    return uniqueID!!
                }
            }

            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString()
                Log.d(TAG, "new UUID generated!")
                val editor: SharedPreferences.Editor = sharedPrefs.edit()
                editor.putString(PREF_UNIQUE_ID, uniqueID)
                editor.apply()
                saveUUIDToFile(uniqueID!!)
                thread(start = true) {
                    saveToDB(uniqueID!!)
                }

            }
        }
        Log.d(TAG, "UUID found ${uniqueID}!")
        return uniqueID!!
    }

    fun getUUIdFormSharedPreference(): String? {
        val sharedPrefs: SharedPreferences = context.getSharedPreferences(
            PREF_UNIQUE_ID, Context.MODE_PRIVATE
        )
        return sharedPrefs.getString(PREF_UNIQUE_ID, null)
    }

    private fun saveUUIDToFile(uuid: String) {
        thread(start = true) {
            try {
                val directory = getFileDirectory()
                // Although you can define your own key generation parameter specification, it's
                // recommended that you use the value specified here.
                /*val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
                val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)*/
                val masterKeyAlias = createMasterKey(MyApp.getAppContext())

                // Creates a file with this name, or replaces an existing file
                // that has the same name. Note that the file name cannot contain
                // path separators.
                val fileToWrite = getFileName()
                val file = File(directory, fileToWrite)
                if (file.exists()) {
                    file.delete()
                    Log.d(TAG, "File deleted!")
                }
                Log.d(TAG, "saveUUIDToFile directory: $directory  fileToWrite:$fileToWrite")
                val encryptedFile = EncryptedFile.Builder(
                    File(directory, fileToWrite),
                    context,
                    masterKeyAlias,
                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
                ).build()

                val fileContent = uuid
                    .toByteArray(StandardCharsets.UTF_8)
                encryptedFile.openFileOutput().apply {
                    write(fileContent)
                    flush()
                    close()
                }
                Log.d(TAG, "UUID Saved $uuid ")
            } catch (e: Exception) {
                Log.e(TAG, "${e.message}")
            }
        }


    }


    fun getUUUIDFromFile(): String? {
        val directory = getFileDirectory()
        // Although you can define your own key generation parameter specification, it's
        // recommended that you use the value specified here.
        /* val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
         val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)*/
        val masterKeyAlias = createMasterKey(MyApp.getAppContext())


        val context = MyApp.getAppContext()
        val fileToRead = getFileName()
        val encryptedFile = EncryptedFile.Builder(
            File(directory, fileToRead),
            context,
            masterKeyAlias,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        try {
            val inputStream = encryptedFile.openFileInput()
            val byteArrayOutputStream = ByteArrayOutputStream()
            var nextByte: Int = inputStream.read()
            while (nextByte != -1) {
                byteArrayOutputStream.write(nextByte)
                nextByte = inputStream.read()
            }

            val plaintext: ByteArray = byteArrayOutputStream.toByteArray()
            val uuid = plaintext.toString(StandardCharsets.UTF_8)
            Log.d(TAG, "UUID From Local $uuid")
            return uuid
        } catch (e: Exception) {
            Log.e(TAG, "${e.message}")
            return null
        }
    }

    private fun createMasterKey(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        } else {
            val alias = "as8sFd@a98sua#sn8dA"
            val start: Calendar = GregorianCalendar()
            val end: Calendar = GregorianCalendar()
            end.add(Calendar.YEAR, 100)

            val spec =
                KeyPairGeneratorSpec.Builder(context)
                    .setAlias(alias)
                    .setSubject(X500Principal("CN=$alias"))
                    .setSerialNumber(
                        BigInteger.valueOf(
                            Math.abs(alias.hashCode()).toLong()
                        )
                    )
                    .setStartDate(start.time).setEndDate(end.time)
                    .build()

            val kpGenerator: KeyPairGenerator = KeyPairGenerator.getInstance(
                "RSA",
                "AndroidKeyStore"
            )
            kpGenerator.initialize(spec)
            val kp: KeyPair = kpGenerator.generateKeyPair()
            kp.public.toString()
        }
    }

    fun isUUDIExistsInFile(): Boolean {
        val f = File(getFileDirectory() + File.separator + getFileName())

        return f.exists()
    }

    fun loadUUIDFromFileOrDB(onFinish: () -> Unit) {
        thread(start = true) {

            val UUIDFromSharePref = getUUIdFormSharedPreference()
            UUIDFromSharePref?.let {
                uniqueID = it
                saveToDB(it)
                Log.d(TAG, "have UUID in Share pref id : $uniqueID")
                onFinish()
                return@thread
            }

            if (isUUDIExistsInFile()) {
                Log.d(TAG, "have UUID in locally")
                UUIDFromFile = getUUUIDFromFile()
                UUIDFromFile?.let {
                    saveToDB(it)
                    onFinish()
                    return@thread
                }

            }

            val UUDIFromDB: String? = getFromDB()
            if (UUDIFromDB != null) {
                Log.d(TAG, "have UUID in DOB:$UUDIFromDB")
                UUIDFromFile = UUDIFromDB
                onFinish()
                return@thread
            }
            onFinish()
            Log.e(TAG, "Do not have UUID in locally")
        }
    }

    fun isUUIDAvailableInSharedPreference(): Boolean {
        val sharedPrefs: SharedPreferences = context.getSharedPreferences(
            PREF_UNIQUE_ID, Context.MODE_PRIVATE
        )
        uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null)
        return uniqueID != null
    }

    private fun getFileDirectory(): String {
        val basePath =
            MyApp.getAppContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.absolutePath
        var dd = ContextCompat.getExternalFilesDirs(MyApp.getAppContext(), null)
        dd?.forEach { file ->
            file?.let {
                Log.d(TAG, "file paths : ${it.absolutePath}")
            }
        }


        val mainDirFile = File(basePath + File.separator + BuildConfig.APPLICATION_ID)
        mainDirFile.mkdirs()
        return mainDirFile.absolutePath
    }

    private fun getFileName(): String = "asdjmnasbd.txt"


    private fun getFromDB(): String? {
        AppDatabase.getInstance().deviceInfoDao().getAll().forEach {
            if (it.id == "1") return it.dif
        }
        return null
    }

    fun saveToDB(uudi: String) {
        AppDatabase.getInstance().deviceInfoDao().deleteAll()
        AppDatabase.getInstance().deviceInfoDao().save(DeviceInfo("1", uudi))
    }


}