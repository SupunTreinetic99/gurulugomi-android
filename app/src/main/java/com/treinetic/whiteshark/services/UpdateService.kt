package com.treinetic.whiteshark.services

import com.google.gson.Gson
import com.treinetic.whiteshark.BuildConfig
import com.treinetic.whiteshark.constance.ErrorCodes
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.appupdate.AppUpdate
import com.treinetic.whiteshark.network.Net

class UpdateService {

    private val TAG = "UpdateService"
    var appUpdate: AppUpdate? = null

    companion object {
        var instance = UpdateService()
        var isDisplayHomeMsg = false
    }

    var gson = Gson()
    fun checkUpdates(
        success: Service.Success<AppUpdate>,
        error: Service.Error
    ) {

        val pathParams = mutableMapOf<String, Any>()
        pathParams["publicKey"] = "4ee05bb23af00f2fcf5d0f9abbe8359c"
        pathParams["version"] = BuildConfig.VERSION_CODE
//        pathParams["version"] = 1
        val body = mutableMapOf<String, Any>()
        body["secret"] = "edae14d5c1313a780d73e8a68e56ece3"
        val net = Net(
            Net.URL.UPDATE_SERVER,
            Net.NetMethod.POST,
            body,
            null,
            pathParams,
            null
        )
        net.perform({ response ->
            try {
                appUpdate = gson.fromJson(response, AppUpdate::class.java)
                appUpdate?.let {
                    success.success(it)
                    return@perform
                }
//                throw Exception()
                error.error(
                    NetException(
                        "Something went wrong",
                        "ERROR",
                        500
                    )
                )

            } catch (e: Exception) {
                error.error(
                    NetException(
                        "Something went wrong",
                        e.message,
                        ErrorCodes.JSON_ERROR
                    )
                )
            }
        },
            { exception ->
                error.error(exception)
            })
    }
}