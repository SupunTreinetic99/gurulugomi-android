package com.treinetic.whiteshark.fragments


import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.LayerDrawable
import android.net.NetworkInfo
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.facebook.login.LoginManager
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.treinetic.whiteshark.BuildConfig
import com.treinetic.whiteshark.FragmentNavigation
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.activity.ConnectionActivity
import com.treinetic.whiteshark.dialog.MaterialDialogs
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.network.Net
import com.treinetic.whiteshark.services.*
import com.treinetic.whiteshark.util.Connections
import com.treinetic.whiteshark.util.CountDrawable
import com.treinetic.whiteshark.util.SnackBars


open class BaseFragment : Fragment() {

    private val snackBar = SnackBars()
    var networkListerner: ((connectivity: Connectivity?) -> Unit)? = null
    var isConnected = true
    val baseFragmentTag = "BaseFragment"
    var errorDialog: MaterialDialog? = null


    override fun onStart() {
        super.onStart()
        listenToNetwork()
    }


    fun setOptionMenuVisibility(
        menu: Menu,
        isFavouriteVisible: Boolean,
        isSearchVisible: Boolean,
        isCategoryVisible: Boolean,
        isCartVisible: Boolean,
        isDeleteVisible: Boolean,
        isRefreshVisible: Boolean = false,
        isSocialShareVisible: Boolean = false
    ) {

        menu.findItem(R.id.action_socialShare)?.isVisible = isSocialShareVisible
        menu.findItem(R.id.action_favorite)?.isVisible = isFavouriteVisible
        menu.findItem(R.id.action_search)?.isVisible = isSearchVisible
        menu.findItem(R.id.action_category)?.isVisible = isCategoryVisible
        menu.findItem(R.id.action_cart)?.isVisible = isCartVisible
        menu.findItem(R.id.action_delete)?.isVisible = isDeleteVisible
        menu.findItem(R.id.action_refresh)?.isVisible = isRefreshVisible

    }

    fun showSuccessSnackBar(view: View, msg: String) {
        snackBar.getSuccessSnack(view, msg).show()

    }

    fun showErrorSnackBar(view: View, msg: String) {
        snackBar.getErrorSnack(view, msg).show()
    }

    fun showMessageSnackBar(view: View, msg: String) {
        snackBar.getMessageSnack(view, msg).show()
    }

    fun isErrorHandled(netException: NetException, ignore: List<Int> = listOf()): Boolean {

        if (isIgnored(netException.code, ignore)) {
            return false
        }

        var title = "Error"
        var message = "Something went wrong"
        var hasError = false


        when (netException.code) {
            401 -> {
                title = "Session Expired"
                message = "Session Expired. Please Login again"
                if (netException.message_id == "DEVICE_LIMIT_EXCEED") {
                    title = "Device Limit Exceed"
                    message = "Maximum device limit is exceeded."
                }
                if (netException.message_id == "DEVICE_ID_NOT_FOUND") {
                    Log.e(baseFragmentTag, netException.message?:"No message")
                    logout()
                }
                hasError = true
            }
            503 -> {
                title = "Under Maintenance"
                message = "Server under maintenance. Try again later"
                hasError = true
            }
            500 -> {
                title = "Error"
                message = "Something went Wrong.Please Try again"
                hasError = true
            }

            else -> {
            }
        }

        if (hasError) {

            errorDialog?.let {
                if (it.isShowing) {
                    return hasError
                }
            }
            context?.let {
                errorDialog = MaterialDialogs().getConfirmDialog(it, title, message)
                errorDialog?.cancelable(false)
                errorDialog?.show()
                return hasError
            }
        }
        return false

    }

    fun isIgnored(value: Int, ignore: List<Int>): Boolean {
        return ignore.contains(value)
    }


    fun logout() {
        LoginManager.getInstance().logOut()
        LocalStorageService.getInstance().saveToken(null)
        BookService.getInstance().clear()
        UserService.getInstance().clear()
        HomeService.getInstance().clear()
        WishListService.getInstance().clear()
        OrderService.getInstance().clear()
        CartService.getInstance().clear()
        LocalStorageService.getInstance().removeCurrentUser()
        LocalStorageService.getInstance().saveToken(null)
        Net.setTOKEN(null)
        signOutGoogle()

        FragmentNavigation.getInstance().startHomeFragment(
            requireFragmentManager(), R.id.fragment_view
        )

        Log.d(baseFragmentTag, "logout success")
    }

    private fun signOutGoogle() {
        try {
            val gso: GoogleSignInOptions =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()

            val googleSignInClient: GoogleSignInClient =
                GoogleSignIn.getClient(requireContext(), gso)
            googleSignInClient.signOut()

        } catch (e: Exception) {
            Log.e(baseFragmentTag, e.message?:"No message")
        }

    }

    private fun listenToNetwork() {
        /* ReactiveNetwork.observeNetworkConnectivity(MyApp.getAppContext())
             .subscribeOn(Schedulers.io())
             .observeOn(AndroidSchedulers.mainThread())
             .subscribe { connectivity: Connectivity? ->
                 connectivity?.let {
                     Log.e("BaseActivity", " state : " + it.state())
                     isConnected = isConnected(connectivity)
                 }

                 networkListerner?.let {
                     it(connectivity)
                 }
             }*/

        Connections.getInstance().connectivity.observe(this, Observer { connectivity ->
            connectivity?.let {
                Log.e("BaseActivity", " state : " + it.state())
                isConnected = isConnected(connectivity)
            }

            networkListerner?.let {
                it(connectivity)
            }

        })
    }


    fun isConnected(connectivity: Connectivity): Boolean {
        return connectivity.state() == NetworkInfo.State.CONNECTED
    }


    fun gotoConnectionActivity(context: Context) {
        val intent = Intent(context, ConnectionActivity::class.java)
        startActivity(intent)
    }


    fun fadeIn(view: View) {
        view.alpha = 0f
        view.visibility = View.VISIBLE
        val animater = view.animate()
        animater.alpha(1f)
        animater.duration = 300
        animater.start()
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


    fun setCount(context: Context, count: String, menu: Menu) {
        val menuItem = menu.findItem(R.id.action_cart)
        val icon = menuItem.icon as LayerDrawable

        val badge: CountDrawable

        // Reuse drawable if possible
        val reuse = icon.findDrawableByLayerId(R.id.ic_group_count)
        if (reuse != null && reuse is CountDrawable) {
            badge = reuse
        } else {
            badge = CountDrawable(context)
        }

        badge.setCount(count)
        icon.mutate()
        icon.setDrawableByLayerId(R.id.ic_group_count, badge)
    }

    fun showToolbarBackButton() {
        (requireActivity() as AppCompatActivity).supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
        }
    }

    fun hideToolbarBackButton() {
        (requireActivity() as AppCompatActivity).supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(false)
            it.setHomeButtonEnabled(false)
        }

    }

    fun isUserLogged(): Boolean {
        return UserService.getInstance().isUserLogged()
    }

    fun setCartItemCount(
        menu: Menu?,
        count: String = CartService.getInstance().getCartSize().toString()
    ) {

        menu?.let {
            setCount(
                context = requireContext(),
                count = count,
                menu = it
            )
            requireActivity().invalidateOptionsMenu()
        }
    }

    fun isDebugMode() = BuildConfig.BUILD_TYPE == "debug"
    fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

}


