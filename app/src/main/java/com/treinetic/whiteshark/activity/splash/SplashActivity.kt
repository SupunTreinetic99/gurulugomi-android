package com.treinetic.whiteshark.activity.splash

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.androidnetworking.common.ANConstants
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.internal.common.CommonUtils
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import com.treinetic.whiteshark.BuildConfig
import com.treinetic.whiteshark.MyApp
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.activity.MainActivity
import com.treinetic.whiteshark.constance.AppModes
import com.treinetic.whiteshark.constance.DeviceInformation
import com.treinetic.whiteshark.databinding.ActivitySplashBinding
import com.treinetic.whiteshark.dialog.MaterialDialogs
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.appupdate.Update
import com.treinetic.whiteshark.network.Net
import com.treinetic.whiteshark.notification.NotificationManager
import com.treinetic.whiteshark.services.*
import com.treinetic.whiteshark.util.Connections
import com.treinetic.whiteshark.util.InitDataFetcher
import com.treinetic.whiteshark.util.ProcessEpub
import java.util.*
import kotlin.system.exitProcess


@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    val TAG = "SplashActivity"
    private var finishRequests = false
    var materialDialogs: MaterialDialogs = MaterialDialogs()
    private var _binding : ActivitySplashBinding? = null
    private val binding get() = _binding!!
    private var initDataFetcher = InitDataFetcher().apply {
        onFinish = ::onFinishAllRequests
        onErrror = ::onError
    }

    override fun onStart() {
        super.onStart()
        startNewAnimation()
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        _binding = ActivitySplashBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        progressSplash()

//        requestRuntimePermission()
        // sendNotificationTest()


//       printHashKey()

//        startAnimation()


        //test
//        FirebaseMessaging.getInstance().subscribeToTopic("dev_2020-07-31"+"_android")
//            .addOnSuccessListener(object : OnSuccessListener<Void?> {
//                override fun onSuccess(aVoid: Void?) {
//                    Toast.makeText(applicationContext, "Success", Toast.LENGTH_LONG).show()
//                }
//            })
    }


    private fun progressSplash() {
        checkPermissions()
        NotificationManager.instance.clear()
        NotificationManager.instance.getNotificationData(intent)
        FirebaseCrashlytics.getInstance().log("splash data")
    }

    private fun isRootedDevice(): Boolean {
        if (BuildConfig.BUILD_TYPE != "debug") {
            val isRooted = CommonUtils.isRooted()
            if (isRooted) showRootMessage()
            return isRooted
        }
        return false
    }

    private fun showRootMessage() {
        runOnUiThread {
            materialDialogs.getDialog(
                context = this,
                title = "APP NOT SUPPORTED",
                message = "Gurulugomi App does not support for rooted devices.",
                positiveText = "Ok",
                positiveClick = {
                    it.dismiss()
                    System.exit(0)
                },
                cancelTouchOutSide = false, cancelable = false
            ).show()
        }

    }

    private fun clearData() {
        var p = ProcessEpub()
        p.deleteAllFilesInDir(p.readLocationPath)
    }

    private fun getFonts() {
        UserService.getInstance().downloadFonts({ result ->
            result.forEach { font ->
                BookService.getInstance().downloadFont(font)
            }
        }, { exception ->
            Log.e(TAG, exception.message?:"Font missing")
        })
    }

//    private fun startAnimation() {


//        var layoutWidth: Int = getScreenHeight()

//        val marginParams = ViewGroup.MarginLayoutParams(mainLogo.getLayoutParams())
//        marginParams.setMargins(0, 0, 0, layoutWidth + 10)
//        val layoutParams: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(marginParams)
//        mainLogo.layoutParams = layoutParams


//        layoutWidth = layoutWidth / 2 + 250


//        mainLogo.animate()
//            .alpha(1f)
//            .translationY(0f)
////            .translationYBy((-layoutWidth).toFloat())
//            .setDuration(600)
//            .setInterpolator(AccelerateDecelerateInterpolator())
//            .setStartDelay(300).withEndAction(Runnable {
//                mainLogo.setImageResource(R.drawable.logo_splash_2)
//
//                Handler().postDelayed(object : Runnable {
//                    override fun run() {
//                        mainLogo.setImageResource(R.drawable.logo_splash_3)
//                    }
//                }, 1500)
//            }).start()


//    }

    private fun startNewAnimation() {
//        splashAnimView.animate().alpha(1f).translationY(0f).setDuration(800)
//            .setInterpolator(AccelerateDecelerateInterpolator())
//            .withEndAction {
//                splashImg2.animate().alpha(1f).setDuration(400)
//                    .withEndAction {
//                        splashImg1.animate().scaleYBy(0.2f).scaleXBy(0.2f).alpha(0f)
//                            .setDuration(300).start()
//                        splashImg2.animate().alpha(0f).setDuration(200).start()
//                        splashImg3.animate().alpha(1f).setDuration(200).start()
//                    }.start()
//            }.start()

        binding.splashImg.animate().alpha(1f).translationY(0f).setDuration(500)
            .setInterpolator(DecelerateInterpolator()).start()
    }

    fun getScreenHeight(): Int {
        return Resources.getSystem().displayMetrics.heightPixels
    }

    fun requestRuntimePermission() {

        val permissions = mutableListOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CAMERA
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d(TAG, "Permission granted TIRAMISU ")
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

//        val permissions = arrayOf(
////            Manifest.permission.READ_PHONE_STATE,
//            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE
//        )

        Permissions.check(this, permissions.toTypedArray(), null, null, object : PermissionHandler() {
            override fun onGranted() {
                continueProcess()
            }

            override fun onDenied(context: Context?, deniedPermissions: ArrayList<String>?) {
                deniedPermissions?.forEach {
                    Log.i(TAG, it)
                    showExitDialog()
                }
            }

        })
    }

    fun continueProcess() {
//        DeviceInformation.getInstance().createSampleFile()
        LocalBookService.getInstance().validateDownloadedBooks {
            if (isRootedDevice()) return@validateDownloadedBooks
            clearData()
            DeviceInformation.getInstance().loadUUIDFromFileOrDB {
                runOnUiThread {
                    checkConnection()
                }
            }

        }
    }

    private fun showExitDialog() {
        materialDialogs.getDialog(
            context = this,
            title = "Permission Denied",
            message = "Requested permissions are required to start the mobile app",
            positiveText = "Grant Permissions",
            negativeText = "Cancel",
            positiveClick = {
                it.dismiss()
                checkPermissions()
            },
            negativeClick = {
                exitApp()
            },
            cancelTouchOutSide = false,
            cancelable = false
        ).show()
    }


    private fun exitApp() {
        moveTaskToBack(true)
        android.os.Process.killProcess(android.os.Process.myPid())
        exitProcess(1)
    }

    private fun checkConnection() {
        if (Connections.getInstance().isNetworkConnected()) {
            Log.d(TAG, "ONLINE_MODE")
            getFonts()
            UserService.APP_MODE = AppModes.ONLINE_MODE
            FirebaseCrashlytics.getInstance().log("$TAG -> AppMode: ${UserService.APP_MODE}")
            checkUpdates()
            if (LocalStorageService.getInstance().token != null) {

            } else {
//                Handler().postDelayed({
//                    startLoginActivity()
//                }, 3000)
            }
        } else {
            offlineMode()
        }
    }

    private fun offlineMode() {
        Log.d(TAG, "OFFLINE_MODE")
        UserService.APP_MODE = AppModes.OFFLINE_MODE
        if (UserService.getInstance().isOfflineModeAvailable()) {
            FirebaseCrashlytics.getInstance().log("$TAG -> AppMode: ${UserService.APP_MODE}")
            Handler().postDelayed({
                startMainActivity()
            }, 1700)
            return
        }
        startLoginActivity()
    }

    fun openAppLink(dialog: MaterialDialog) {
        var link = "https://play.google.com/store/apps/details?id=com.treinetic.whiteshark"
        UpdateService.instance.appUpdate?.update?.extras?.let {
            link = it[Update.STORE_LINK].toString()
        }
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(link)
        )
        startActivity(intent)
    }

    private fun startLoginActivity() {

        if (!Connections.getInstance().isNetworkConnected()) {
            showPoorNetDialog()
            return
        }
        showSomeThingWrongDialog()

//        val intent = Intent(this, LoginActivity::class.java)
//        startActivity(intent)
//        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
//        finish()
    }

    private fun showSomeThingWrongDialog() {
        materialDialogs.getDialog(
            context = this,
            title = "Something went wrong",
            message = "It looks like something went wrong. Try to restart the app!",
            positiveText = "Restart",
            negativeText = "Close",
            positiveClick = {
                it.dismiss()
                restartApp()
            },
            negativeClick = {
                exitApp()
            },
            cancelTouchOutSide = false,
            cancelable = false
        ).show()
    }

    private fun showPoorNetDialog() {
        materialDialogs.getDialog(
            context = this,
            title = "Poor Connection",
            message = "Your connection may be slow to load the app. Please restart the app!",
            positiveText = "Restart",
            negativeText = "Close",
            positiveClick = {
                it.dismiss()
                restartApp()
            },
            negativeClick = {
                exitApp()
            },
            cancelTouchOutSide = false,
            cancelable = false
        ).show()
    }

    private fun restartApp() {
        val intent =
            Intent(this, SplashActivity::class.java)
        this.startActivity(intent)
        finishAffinity()
    }

    private fun startMainActivity() {

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    fun getInitData() {
        UserService.getInstance().initUser(Service.Success { result ->
//            getHomeData()
            initDataFetcher.beginFetch()
            initTopics()

        }, Service.Error { exception ->
            Log.d(TAG, "Login failed")
            Log.e(TAG, exception.message?:"User missing")
            if (isErrorHandled(exception)) return@Error
            LocalStorageService.getInstance().saveToken(null)
            Net.setTOKEN(null)
            initDataFetcher.beginFetch()
            initTopics()

            /*    if (true)return@Error
                if (exception.isLoginRequired()) {
                    Log.e(TAG,"Login required. begin fetch")

                    initDataFetcher.beginFetch()
                    return@Error
                }

                Log.d(TAG,"Not a login required")
                if (isErrorHandled(exception)) return@Error



                startLoginActivity()*/
        })
    }

    private fun initTopics() {
//        if (UserService.getInstance().isUserLogged()) {
//            Log.d(TAG, "User logged. topics normal flow..")
//            return
//        }
        UserService.getInstance().initTopics({ result ->
            Log.d(TAG, "topics success")

        }, { exception ->
            Log.d(TAG, "Topics failed")
            Log.e(TAG, exception.message?:"No message")
        })
    }

    private fun onFinishAllRequests() {
        finishRequests = true
        startMainActivity()
    }

    private fun onError(exception: NetException) {
        if (isErrorHandled(exception)) return
        startLoginActivity()
        Log.e(TAG, exception.message?:"No message")
    }

    private fun getCategories() {
        CategoryService.getInstance().fetchCategoryList(Service.Success { result ->
            fetchMyLibrary()
        }, Service.Error { exception ->
            if (isErrorHandled(exception)) return@Error
            startLoginActivity()
            Log.e(TAG, exception.message?:"No message")
        })
    }

    private fun getHomeData() {
        HomeService.getInstance().fetchHomePageData(Service.Success { result ->
            getCategories()
        }, Service.Error { exception ->
            if (isErrorHandled(exception)) return@Error
            startLoginActivity()
            Log.e(TAG, exception.message?:"No message")
        })
    }

    private fun fetchMyLibrary() {
        BookService.getInstance().fetchMyLibrary(Service.Success { result ->
            result?.data?.let {
                BookService.getInstance().cacheMyLibraryBookCovers(it.toTypedArray())
            }

            fetchCart()
        }, Service.Error { exception ->
            if (exception.code == 403 && exception.message_id == "LOGIN_REQUIRED") {
                initDataFetcher.myLibraryError = false
                initDataFetcher.myLibrarySuccess = true
                return@Error
            }
            if (isErrorHandled(exception)) return@Error
            startLoginActivity()
            Log.e(TAG, exception.message?:"No message")
        })
    }

    private fun fetchCart() {
        CartService.getInstance().loadCartRequest(Service.Success { result ->
            fetchWishlist()
        }, Service.Error { exception ->
            if (exception.code == 403 && exception.message_id == "LOGIN_REQUIRED") {
                initDataFetcher.cartError = false
                initDataFetcher.cartSuccess = true
                return@Error
            }
            if (isErrorHandled(exception)) return@Error
            startLoginActivity()
        })
    }

    private fun fetchWishlist() {
        WishListService.getInstance().fetchWishList(Service.Success { result ->
            finishRequests = true
            startMainActivity()
        }, Service.Error { exception ->
            if (exception.code == 403 && exception.message_id == "LOGIN_REQUIRED") {
                initDataFetcher.wishListError = false
                initDataFetcher.wishListSuccess = true
                return@Error
            }
            if (isErrorHandled(exception)) return@Error
            startLoginActivity()
            Log.e(TAG, exception.message?:"No message")
        })
    }

    override fun onBackPressed() {
        return
    }

    override fun onDestroy() {
        super.onDestroy()
        materialDialogs?.let {
            it.clear()
        }
    }


    fun isErrorHandled(exception: NetException): Boolean {

        if (exception.message == ANConstants.CONNECTION_ERROR && exception.code == 0) {
            offlineMode()
            return true
        }

        if (exception.code == 503) {
            maintenanceDialog(
                msg = exception.message ?: "Server under maintenance. Try again later"
            )
            FirebaseCrashlytics.getInstance().log("splash, Server under maintenance 503")
            return true
        }

        /*if (exception.code != 401 && !exception.isLoginRequired()) {
            Log.d(TAG,"isErrorHandled not 401 and not login required ")
            errorDialog()
            FirebaseCrashlytics.getInstance()
                .log("splash, Something went wrong ${exception.code} , ${exception.message}")
            return true
        }*/

        return false

    }


    fun maintenanceDialog(
        msg: String = "Server under maintenance. Try again later",
        title: String = "Under Maintenance"
    ) {
        materialDialogs.getDialog(
            context = this,
            title = title,
            message = msg,
            positiveText = "Ok",
            positiveClick = {
                it.dismiss()
                finish()
            },
            cancelTouchOutSide = false,
            cancelable = false
        ).show()
    }

    fun errorDialog() {
        materialDialogs.getDialog(
            context = this,
            title = "Error",
            message = "Something went wrong. Please try again",
            positiveText = "Try Again",
            positiveClick = {
                it.dismiss()
                checkConnection()
            },
            cancelTouchOutSide = false,
            cancelable = false
        ).show()
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "com.treinetic.gurulugomi.dev"
            val description = "des"
            val importance = android.app.NotificationManager.IMPORTANCE_MAX
            @SuppressLint("WrongConstant") val channel =
                NotificationChannel("com.treinetic.gurulugomi.dev", name, importance)
            channel.description = description
            val notificationManager =
                getSystemService(
                    android.app.NotificationManager::class.java
                )
            notificationManager.createNotificationChannel(channel)
            println("channel created")
        }
    }

}
