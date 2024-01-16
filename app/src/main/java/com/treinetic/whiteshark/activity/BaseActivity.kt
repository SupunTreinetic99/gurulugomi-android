package com.treinetic.whiteshark.activity


import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.treinetic.whiteshark.MyApp
import com.treinetic.whiteshark.constance.AppModes
import com.treinetic.whiteshark.services.UserService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by Nuwan on 2/26/19.
 */
open class BaseActivity : AppCompatActivity() {

    var networkListerner: ((connectivity: Connectivity?) -> Unit)? = null
    var isConnected = true

    var loadingView: FrameLayout? = null
    var progressBar: ProgressBar? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listenToNetwork()
    }

    @SuppressLint("MissingPermission", "CheckResult")
    private fun listenToNetwork() {
        ReactiveNetwork.observeNetworkConnectivity(MyApp.getAppContext())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { connectivity: Connectivity? ->
                if (UserService.APP_MODE.equals(AppModes.OFFLINE_MODE)) {
                    return@subscribe
                }
                connectivity?.let {
                    Log.e("BaseActivity", " state : " + it.state())
                    isConnected = isConnected(connectivity)
                }

                networkListerner?.let {
                    it(connectivity)
                }
            }
    }

    fun isConnected(connectivity: Connectivity): Boolean {
        return connectivity.state() == NetworkInfo.State.CONNECTED
    }

    fun gotoConnectionActivity(context: Context) {
        val intent = Intent(context, ConnectionActivity::class.java)
        startActivity(intent)
    }

    fun showTopLoading() {
        runOnUiThread {
            loadingView?.let {
                it.visibility = View.VISIBLE
                progressBar?.animate()?.alpha(1f)?.setDuration(300)?.start()
            }

        }


    }

    fun hideTopLoading() {
        runOnUiThread {
            loadingView?.let {

                progressBar?.animate()?.alpha(0f)?.setDuration(200)?.setListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator) {
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        it.visibility = View.GONE
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        it.visibility = View.GONE
                    }

                    override fun onAnimationStart(animation: Animator) {
                    }
                })?.start()
            }
        }


    }

    fun fadeOut(
        view: View,
        onAnimationStart: ((p0: Animator?) -> Unit)? = null,
        onAnimationEnd: ((p0: Animator?) -> Unit)? = null,
        onAnimationCancel: ((p0: Animator?) -> Unit)? = null
    ) {
        val animater = view.animate()
        animater.alpha(0f)
        animater.duration = 300
        animater.setListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator) {

            }

            override fun onAnimationEnd(p0: Animator) {
                onAnimationEnd?.let { it(p0) }
            }

            override fun onAnimationCancel(p0: Animator) {
                onAnimationCancel?.let { it(p0) }
            }

            override fun onAnimationStart(p0: Animator) {
                onAnimationStart?.let { it(p0) }
            }

        })
        animater.start()
    }


}