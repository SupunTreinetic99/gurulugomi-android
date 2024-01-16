package com.treinetic.whiteshark.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.treinetic.whiteshark.FragmentNavigation
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.constance.AppModes
import com.treinetic.whiteshark.constance.Screens
import com.treinetic.whiteshark.databinding.ActivityMainBinding
import com.treinetic.whiteshark.databinding.ActivityNoConnectionBinding
import com.treinetic.whiteshark.fragments.FragmentRefreshable
import com.treinetic.whiteshark.fragments.bookprofile.BookProfileFragment
import com.treinetic.whiteshark.fragments.cart.CartFragment
import com.treinetic.whiteshark.fragments.search.SearchFragment
import com.treinetic.whiteshark.notification.NotificationManager
import com.treinetic.whiteshark.services.LocalStorageService
import com.treinetic.whiteshark.services.UserService
import kotlin.system.exitProcess


class MainActivity : BaseActivity(){

    private val TAG = "MainActivity"
    lateinit var toolBar: Toolbar
    private lateinit var favoriteFill: MenuItem
    private lateinit var favoriteUnFill: MenuItem
    private var isStatedConnectionActivity = false
    private var isDoubleTaped = false
    var optionMenus: Menu? = null
    var backStackChangedListener: Unit? = null
    private lateinit var binding: ActivityMainBinding

    companion object {
        fun show(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setupBackStackListener()
        setupNetConnectionListener()
        setupToolBar()
        showFragments()
        loadingView = binding.progressContainer
        progressBar = binding.progress
    }

    private fun setupBackStackListener() {
        backStackChangedListener = supportFragmentManager.addOnBackStackChangedListener {

            if (supportFragmentManager.backStackEntryCount >= 1) {
                val topOnStack: String? =
                    supportFragmentManager.getBackStackEntryAt(supportFragmentManager.backStackEntryCount - 1).name

                val topFragment = topOnStack?.let { getFragment(it) }

                topFragment?.let {
                    if (it is FragmentRefreshable) {
                        it.onRefresh()
                    }
                }
            }
        }
    }

    private fun removeTempToken() {
        if (!UserService.getInstance().isUserLogged()) {
            LocalStorageService.getInstance().saveToken(null)
        }
    }

    private fun showFragments() {
        supportFragmentManager?.fragments?.let {
            if (it.count() > 1) {
                return
            }
        }

        if (UserService.APP_MODE == AppModes.ONLINE_MODE) {
            if (showNotificationDataFragment()) {
                return
            }
            startHome()
        } else {
            //
            startMyLibrary()
        }

    }

    private fun showNotificationDataFragment(): Boolean {

        if (!NotificationManager.instance.isUsedNotificationData &&
            NotificationManager.instance.hasNotificationData()
        ) {
            NotificationManager.instance.book?.let {

                FragmentNavigation.getInstance().startBookProfile(
                    supportFragmentManager,
                    R.id.fragment_view,
                    it
                )
                return true
            }

            NotificationManager.instance.event?.let {
                FragmentNavigation.getInstance().startEventDetails(
                    supportFragmentManager,
                    R.id.fragment_view, it.id,
                    true
                )
                return true
            }
            Log.d(TAG, "checking promotion")
            NotificationManager.instance.promotion?.let {
                Log.d(TAG, "startPromotionFragment")
                FragmentNavigation.getInstance().startPromotionFragment(
                    supportFragmentManager,
                    R.id.fragment_view
                )
                return true
            }
        }
        return false
    }


    private fun setupNetConnectionListener() {
        networkListerner = { connectivity ->
            connectivity?.let {
                if (!isConnected(it)) {
                    gotoConnectionActivity(this)
                    isStatedConnectionActivity = true
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
//        removeTempToken()
        if (UserService.APP_MODE.equals(AppModes.OFFLINE_MODE)) {
            return
        }
        if (isStatedConnectionActivity) {
            isStatedConnectionActivity = false
            if (!supportFragmentManager.fragments.isNullOrEmpty()) {
                startHome()
            }
        }
    }
    private fun setupToolBar() {
        Log.d(TAG, "*****setupToolBar main frag")
        toolBar = binding.toolBarHome.root
        toolBar.title = resources.getString(R.string.app_name)
        setSupportActionBar(toolBar)
        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            Log.d(TAG, "*****setupToolBar main frag action")
            if (!UserService.isOfflineMode()) {
                setDisplayHomeAsUpEnabled(true)
                setHomeAsUpIndicator(R.drawable.ic_menu)
            } else {
                setDisplayHomeAsUpEnabled(false)
            }
        }
        actionbar?.show()

    }


    private fun startHome() {
        val fragmentNavigation = FragmentNavigation()
        fragmentNavigation.startHomeFragment(
            supportFragmentManager,
            R.id.fragment_view
        )

    }

    private fun startMyLibrary() {
        FragmentNavigation().startMyLibrary(supportFragmentManager, R.id.fragment_view)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        CartFragment.actionMenus = menu
        BookProfileFragment.optionMenus = menu
        optionMenus = menu
        menuInflater.inflate(R.menu.tool_menu, menu)
        menu.findItem(R.id.action_delete).isVisible = false

        favoriteUnFill = menu.findItem(R.id.action_favorite)
        favoriteUnFill.isVisible = false

        favoriteFill = menu.findItem(R.id.action_favorite_fill)
        favoriteFill.isVisible = false


        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.let {
            BookProfileFragment.optionMenus = it
        }

        return super.onPrepareOptionsMenu(menu)

    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.action_category -> {
                if (!isFinishing())
                    FragmentNavigation().startBaseCategoryFragment(
                        supportFragmentManager,
                        R.id.fragment_view
                    )
                true
            }

            R.id.action_socialShare -> {
                false
            }
            R.id.action_search -> {
                if (!isFinishing())
                    FragmentNavigation()
                        .startSearch(supportFragmentManager, R.id.fragment_view)
                true
            }
            R.id.action_cart -> {
                if (!isFinishing()) {
                    FragmentNavigation()
                        .startCart(supportFragmentManager, R.id.fragment_view)
                }
                true
                /*      if (!UserService.getInstance().isUserLogged()) {
                          FragmentNavigation()
                              .startCart(supportFragmentManager, R.id.fragment_view)
                          true
                      }else{
                          if (!isFinishing()) {
                              FragmentNavigation()
                                  .startCart(supportFragmentManager, R.id.fragment_view)
                          }
                          true
                      }*/

            }
            R.id.action_favorite -> {

//                favoriteUnFill.isVisible = false
//                favoriteFill.isVisible = true
                false
            }
            R.id.action_favorite_fill -> {
//                favoriteUnFill.isVisible = true
//                favoriteFill.isVisible = false
                false
            }
            R.id.action_delete -> {
                false
            }

            R.id.action_refresh -> {
                false
            }
            else -> {


                if (isFragmentVisible(Screens.HOME)) {
                    startProfileFragment()
                } else if (isFragmentVisible(Screens.BOOK_PROFILE)) {
                    backPressFomBookProfile()
                } else {
                    onBackPressed()
                }
                super.onOptionsItemSelected(item)


            }
        }

    private fun backPressFomBookProfile() {
        if (supportFragmentManager.backStackEntryCount <= 1) {
            startHome()
            return
        }
        supportFragmentManager.popBackStack(
            Screens.BOOK_PROFILE,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    private fun startProfileFragment() {
        if (!isFinishing())
            FragmentNavigation()
                .startUserProfile(supportFragmentManager, R.id.fragment_view)

    }

    override fun onBackPressed() {

        Log.d(TAG, "inside on back...")

        if (UserService.isOfflineMode()) {
            return
        }

        val fragment = supportFragmentManager.findFragmentByTag(Screens.BOOK_PROFILE)

        if (fragment?.isVisible == true && supportFragmentManager.backStackEntryCount <= 1) {
            FragmentNavigation.getInstance()
                .startHomeFragment(supportFragmentManager, R.id.fragment_view)
            return
        }

        if (isFragmentVisible(Screens.BOOK_PROFILE)) {
            supportFragmentManager.popBackStack()
            return
        }
        if (isFragmentVisible(Screens.PAYMENT)) {
            supportFragmentManager.popBackStack(
                Screens.ORDER_CONFIRM,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
            return
        }

        //setupToolBar()

        if (isFragmentVisible(Screens.HOME)) {
            Log.i(TAG, "this is home")
            if (isDoubleTaped) {
                moveTaskToBack(true)
                android.os.Process.killProcess(android.os.Process.myPid())
                exitProcess(1)
            }
            isDoubleTaped = true
            Toast.makeText(this, "Please click BACK again to exit from the app.", Toast.LENGTH_LONG)
                .show()
            Handler().postDelayed(Runnable { isDoubleTaped = false }, 2000)
            return
        }


        if (!NotificationManager.instance.isUsedNotificationData) {
            NotificationManager.instance.isUsedNotificationData = true
            FragmentNavigation.getInstance()
                .startHomeFragment(supportFragmentManager, R.id.fragment_view)
            return
        }

        if (isFragmentVisible(Screens.USER_PROFILE)) {
//            setupToolBar()
        }

        if (isFragmentVisible(Screens.MY_LIBRARY)) {
            supportFragmentManager.fragments.clear()
            FragmentNavigation.getInstance()
                .startHomeFragment(supportFragmentManager, R.id.fragment_view)
            return
        }

        if (isFragmentVisible(Screens.SEARCH)) {
            Log.e(TAG, "Calling Back press from search")
            //toolBar?.title = resources.getString(R.string.home)
            val fragment: SearchFragment? = getFragment(Screens.SEARCH) as SearchFragment?
            fragment?.clearSearch = true
            setupToolBar()
        }



        super.onBackPressed()
    }

    private fun isFragmentVisible(tag: String): Boolean {

        val fragment = supportFragmentManager.findFragmentByTag(tag)
//        val currentFragment = supportFragmentManager.fragments.last().tag

        if (supportFragmentManager.backStackEntryCount < 1) {
            return false
        }
        val topOnStack: String? =
            supportFragmentManager.getBackStackEntryAt(supportFragmentManager.backStackEntryCount - 1).name

        if (fragment?.isVisible == true && topOnStack == tag) {
            return true
        }
        return false

    }

    private fun getFragment(tag: String): Fragment? {
        return supportFragmentManager.findFragmentByTag(tag)
    }

    override fun onDestroy() {
        super.onDestroy()
        networkListerner = null
        backStackChangedListener = null
    }

}
