package com.treinetic.whiteshark.services


import android.content.Context
import android.util.Log
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.facebook.login.LoginManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.treinetic.whiteshark.activity.LoginActivity
import com.treinetic.whiteshark.constance.AppModes
import com.treinetic.whiteshark.constance.DeviceInformation
import com.treinetic.whiteshark.constance.ErrorCodes
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.*
import com.treinetic.whiteshark.network.Net
import com.treinetic.whiteshark.notification.NotificationManager
import org.json.JSONObject
import java.io.File
import java.lang.reflect.Type
import java.util.*

class UserService {

    var initUser: User? = null
    lateinit var initLogin: Login
    lateinit var devices: MutableList<Device>
    lateinit var userObj: User
    private val TAG = "UserService"
     var tempToken: String? = null

    companion object {
        private var instance = UserService()
        fun getInstance(): UserService {
            return instance
        }

        var APP_MODE: String = AppModes.ONLINE_MODE

        fun isOfflineMode(): Boolean {
            return APP_MODE == AppModes.OFFLINE_MODE
        }
    }

    fun registerUser(user: User, success: Service.Success<User>, error: Service.Error) {

        val body = mutableMapOf<String, String>()
        body["name"] = user.name
        body["email"] = user.email
        body["password"] = user.password

        val net = Net(
            Net.URL.REGISTER_USER,
            Net.NetMethod.POST, body, null, null, null
        )
        net.perform({ response ->

            try {
                val registerUser = Gson().fromJson(response, User::class.java)
                initUser = registerUser
                success.success(registerUser)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, { exception ->
            if (exception.code == 500) exception.setMessage("Something went wrong")
            error.error(exception)
        })

    }

    fun getUser(): User {
        Log.i(TAG, "inside get user")

        initUser?.let {
            return it
        }
        return getFromLocalStorage()
    }

    fun userNormalLogin(
        email: String,
        password: String,
        id: String,
        name: String,
        success: Service.Success<Login>,
        error: Service.Error
    ) {

        val body = mutableMapOf<String, String>()

        body["email"] = email
        body["password"] = password
        body["deviceId"] = id//"12345"
        body["deviceName"] = name


        val net = Net(
            Net.URL.LOGIN,
            Net.NetMethod.POST, body, null, null, null
        )
        net.perform({ response ->
            try {
                val login = Gson().fromJson(response, Login::class.java)
                initLogin = login
                initUser = login.user
                initLogin.subscribeForNotificationTopics()
                initUser?.let {
                    saveCurrentUser(it)
                    setCrashlyticsUserData(it)
                }
                OrderService.getInstance().fillBillingDetails(login)
                success.success(login)
            } catch (e: Exception) {
                e.printStackTrace()

                error.error(
                    NetException(
                        "Something went worong",
                        "JSON_ERROR",
                        ErrorCodes.JSON_ERROR
                    )
                )
                FirebaseCrashlytics.getInstance().log(response)
            }

        }, { exception ->
            tempToken = exception.response.header("Authorization")
            error.error(exception)
        })

    }


    fun socialLogin(
        provider: String,
        token: String,
        deviceId: String,
        deviceName: String,
        success: Service.Success<Login>,
        error: Service.Error
    ) {
        val body = mutableMapOf<String, String>()
        body["provider"] = provider
        body["token"] = token
        body["deviceId"] = deviceId //"12345"
        body["deviceName"] = deviceName

        val net = Net(
            Net.URL.SOCIAL_LOGIN,
            Net.NetMethod.POST, body, null, null, null
        )
        net.perform({ response ->
            try {
                val initLoginData = Gson().fromJson<Login>(response, Login::class.java)
                initLogin = initLoginData
                initUser = initLogin.user
                initLogin.subscribeForNotificationTopics()
                initUser?.let {
                    saveCurrentUser(it)
                    setCrashlyticsUserData(it)
                }
                success.success(initLogin)
            } catch (ex: Exception) {
                ex.printStackTrace()
                error.error(
                    NetException(
                        "Something went wrong",
                        "JSON_ERROR",
                        ErrorCodes.JSON_ERROR
                    )
                )
                FirebaseCrashlytics.getInstance().log(response)
            }

        }, { exception ->
            tempToken = exception.response.header("Authorization")
            error.error(exception)
        })
    }

    fun saveCurrentUser(user: User) {
        LocalStorageService.getInstance().saveCurrentUser(user)
    }

    fun sendReset(email: String, success: Service.Success<Message>, error: Service.Error) {

        val body = mutableMapOf<String, Any>()
        body["email"] = email

        val net = Net(
            Net.URL.PASSWORD_RESET,
            Net.NetMethod.POST, body, null, null, null
        )

        net.perform({ response ->
            try {
                val message = Gson().fromJson<Message>(response, Message::class.java)
                success.success(message)
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().log(response)
                e.printStackTrace()
                success.success(Message("", "success "))

            }


        }, { exception ->
            error.error(exception)
        })

    }

    fun initUser(success: Service.Success<Login>, error: Service.Error) {

        val net = Net(
            Net.URL.USER_INIT,
            Net.NetMethod.GET,
            null,
            null,
            null,
            null
        )

        net.perform({ response ->

            try {
                val loginData = Gson().fromJson<Login>(response, Login::class.java)
                initLogin = loginData
                initUser = loginData.user
//                initLogin.subscribeForNotificationTopics()
                initUser?.let {
                    saveCurrentUser(it)

                    setCrashlyticsUserData(it)
                }
                FirebaseCrashlytics.getInstance().log("initUser Called")

                success.success(loginData)
            } catch (e: Exception) {
                e.printStackTrace()
                error.error(
                    NetException(
                        "Something went wrong",
                        "JSON_ERROR",
                        ErrorCodes.JSON_ERROR
                    )
                )
            }

        }, { exception ->
            error.error(exception)

        })
    }


    fun initTopics(success: Service.Success<ArrayList<NotificationTopic>>, error: Service.Error) {

        val net = Net(
            Net.URL.TOPICS,
            Net.NetMethod.GET,
            null,
            null,
            null,
            null
        )

        net.perform({ response ->

            try {
                val topicList: Type = object : TypeToken<ArrayList<NotificationTopic?>?>() {}.type

                val notificationTopics: ArrayList<NotificationTopic> =
                    Gson().fromJson(response, topicList)

                subscribeForNotificationTopics(notificationTopics)

                FirebaseCrashlytics.getInstance().log("init topics Called")

                success.success(notificationTopics)
            } catch (e: Exception) {
                e.printStackTrace()
                error.error(
                    NetException(
                        "Something went wrong",
                        "JSON_ERROR",
                        ErrorCodes.JSON_ERROR
                    )
                )
            }

        }, { exception ->
            error.error(exception)

        })
    }

    fun setCrashlyticsUserData(user: User) {
        FirebaseCrashlytics.getInstance().setUserId(user.user_id)
        FirebaseCrashlytics.getInstance().setCustomKey("email", user.email)
        FirebaseCrashlytics.getInstance().setCustomKey("name", user.name)
    }

    fun updateUser(
        image: File,
        success: Service.Success<User>,
        error: Service.Error, )
    {
        val token = LocalStorageService.getInstance().token
        val deviceId = DeviceInformation.getInstance().getDeviceId()
//        val deviceId = "12345"
        AndroidNetworking.upload(Net.URL.UPDATE_USER)
            .addMultipartFile("image", image)
            .setPriority(Priority.HIGH)
            .addHeaders("Authorization", token)
            .addHeaders("t-device-id", deviceId)
            .build()
            .setUploadProgressListener { bytesUploaded, totalBytes ->
                // do anything with progress
                Log.e(TAG, "image upload progress  $bytesUploaded | $totalBytes")
            }
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.d(TAG, "Success image upload")
                    val updateUser = Gson().fromJson<User>(response.toString(), User::class.java)
                    initUser?.image = updateUser.image
                    success.success(updateUser)
                }

                override fun onError(error: ANError) {
                    error.errorBody
                    error.printStackTrace()
                    Log.d(TAG, "Error image upload")
                }
            })
    }

    fun fetchDeviceList(
        success: Service.Success<MutableList<Device>>,
        error: Service.Error,
        token: String? = null
    ) {

        var headers: Map<String, String>? = null

        if (token != null) {
            headers = HashMap()
            headers.put("Authorization", token)
        }

        val net = Net(
            Net.URL.DEVICES,
            Net.NetMethod.GET,
            null,
            null,
            null,
            headers
        )

        net.perform({ response ->
            try {
                val deviceList: MutableList<Device> =
                    Gson().fromJson(response, Array<Device>::class.java).toMutableList()

                devices = deviceList
                success.success(deviceList)
            } catch (e: Exception) {
                e.printStackTrace()
                error.error(
                    NetException(
                        "Something went wrong",
                        "JSON_ERROR",
                        ErrorCodes.JSON_ERROR
                    )
                )
            }
        }, { exception ->
            error.error(exception)
        })

    }

    fun removeDevice(
        deviceId: String,
        success: Service.Success<MutableList<Device>>,
        error: Service.Error,
        token: String? = null
    ) {
        var headers: Map<String, String>? = null

        if (token != null) {
            headers = HashMap()
            headers.put("Authorization", token)
        }

        val pathParams = mutableMapOf<String, Any>()
        pathParams["id"] = deviceId

        val net = Net(
            Net.URL.REMOVE_DEVICES,
            Net.NetMethod.DELETE, null, null, pathParams, headers
        )
        net.perform({ response ->
            try {
                val deviceList: MutableList<Device> =
                    Gson().fromJson(response, Array<Device>::class.java).toMutableList()

                devices = deviceList
                success.success(deviceList)
            } catch (e: Exception) {
                e.printStackTrace()
                error.error(
                    NetException(
                        "Something went wrong",
                        "JSON_ERROR",
                        ErrorCodes.JSON_ERROR
                    )
                )
            }

        }, { exception ->
            error.error(exception)
        })

    }

    fun singOut(context: Context) {
        LoginManager.getInstance().logOut()
        LocalStorageService.getInstance().saveToken(null)
        LoginActivity.show(context = context)
    }

    fun downloadFonts(success: Service.Success<MutableList<Font>>, error: Service.Error) {
        val net = Net(
            Net.URL.DOWNLOAD_FONTS,
            Net.NetMethod.GET,
            null,
            null,
            null,
            null
        )

        net.perform({ response ->

            try {
                val fontList =
                    Gson().fromJson(response, Array<Font>::class.java).toMutableList()
                success.success(fontList)
            } catch (e: Exception) {
                e.printStackTrace()
            }


        }, { exception ->
            exception.printStackTrace()

        })
    }

    fun isOfflineModeAvailable(): Boolean {

        return LocalStorageService.getInstance().getCurrentUser() != null
    }

    private fun getFromLocalStorage(): User {

        val user: String? = LocalStorageService.getInstance().getCurrentUser()
        user?.let {
            try {
                userObj = Gson().fromJson(user, User::class.java)
                userObj?.let {
                    initUser = userObj
                }
                return userObj
            } catch (e: Exception) {
                Log.e(TAG, "Local User not found.")
                e.printStackTrace()
            }

        }
        return userObj
    }


    fun clear() {
        instance = UserService()
    }

    fun isUserLogged(): Boolean {
        if (!::initLogin.isInitialized) return false
        return initUser != null
    }

    fun subscribeForNotificationTopics(topics: ArrayList<NotificationTopic>) {

        topics?.forEach {
            Log.d("Login", "topic : ${it.topic}_android  subscribe : ${it.subscribe}")
            NotificationManager.instance.unSubscribeFrom(it.topic)
            if (it.subscribe) {
                NotificationManager.instance.subscribeTo(it.topic + "_android")
            } else {
                NotificationManager.instance.unSubscribeFrom(it.topic + "_android")
            }

        }

    }

}



















































