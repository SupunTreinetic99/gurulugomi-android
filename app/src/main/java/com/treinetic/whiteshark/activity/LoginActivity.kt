package com.treinetic.whiteshark.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.treinetic.whiteshark.FragmentNavigation
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.constance.Contants
import com.treinetic.whiteshark.constance.Screens
import com.treinetic.whiteshark.fragments.login.LoginFragment

class LoginActivity : AppCompatActivity() {

    val TAG = "LoginActivity"

    companion object {
        fun show(context: Context) {
            val intent = Intent(context, LoginActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        FragmentNavigation.getInstance()
            .startLoginFragment(supportFragmentManager, R.id.login_fragment_view)


    }

    override fun onBackPressed() {

        if (isFragmentVisible(Screens.REMOVE_DEVICES)) {
            supportFragmentManager.fragments.clear()
            FragmentNavigation.getInstance()
                .startLoginFragment(supportFragmentManager, R.id.login_fragment_view)
            return
        }
        if (isFragmentVisible(Screens.REGISTER)) {
            supportFragmentManager.fragments.clear()
            FragmentNavigation.getInstance()
                .startLoginFragment(supportFragmentManager, R.id.login_fragment_view)
            return
        }
        if (isFragmentVisible(Screens.LOGIN)) {
            if (isLoginInProgress()) {
                Log.e(TAG, "Login in progress. Cannot Go back")
                return
            }
            finishLoginActivity()
        }
        super.onBackPressed()
    }

    private fun isLoginInProgress(): Boolean {
        val fragment = supportFragmentManager.findFragmentByTag(Screens.LOGIN)
        if (fragment is LoginFragment) {
            return fragment.googleInProgress || fragment.facebookInprogress || fragment.appleInprogress
        }
        return false
    }

    private fun finishLoginActivity() {
        val returnIntent = Intent()

        returnIntent.putExtra("data", Contants.FAILED_CALLBACK)

        setResult(Activity.RESULT_CANCELED, returnIntent)
        finish()
    }

    private fun isFragmentVisible(tag: String): Boolean {
        val fragment = supportFragmentManager.findFragmentByTag(tag)
        if (fragment?.isVisible == true) {
            return true
        }
        return false

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        LoginFragment.newInstance().onActivityResult(requestCode, resultCode, data)
    }


}
