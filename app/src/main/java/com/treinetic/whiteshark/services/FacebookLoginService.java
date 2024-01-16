package com.treinetic.whiteshark.services;

import android.content.Intent;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.jaredrummler.android.device.DeviceName;
import com.treinetic.whiteshark.constance.DeviceInformation;
import com.treinetic.whiteshark.exceptions.NetException;
import com.treinetic.whiteshark.models.Login;

import java.util.Arrays;


public class FacebookLoginService {

    private final String TAG = "FacebookLoginService";
    private static FacebookLoginService instance;
    private String facebookToken;
    private CallbackManager callbackManager;
    private static final int PERMISSIONS_DENIED = -506;
    private static final String FACEBOOK = "facebook";


    public static FacebookLoginService getInstance() {
        if (instance == null) {
            instance = new FacebookLoginService();
        }
        return instance;
    }

    public void doLogin(AppCompatActivity activity, final Service.Success<Login> success, final Service.Error error) {

        if (callbackManager == null) {
            callbackManager = CallbackManager.Factory.create();
        }

        LoginManager.getInstance().logOut();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "Facebook login succcess");

                if (loginResult.getAccessToken().getDeclinedPermissions().size() > 0) {
                    Log.e(TAG, " onSuccess .Not all permission allowed " +
                            loginResult.getAccessToken().getDeclinedPermissions().size());
                    error.error(NetException.init("Permission denied", PERMISSIONS_DENIED));

                    return;
                }

                String token = loginResult.getAccessToken().getToken();

                //                    fetchLogin(FACEBOOK, token, success, error);
                String deviceId = new DeviceInformation().getDeviceId();
                String deviceName = DeviceName.getDeviceName();
                UserService.Companion.getInstance().socialLogin(FACEBOOK, token, deviceId, deviceName, success, error);
            }

            @Override
            public void onCancel() {
                Log.e(TAG, "onCancel called");
                error.error(new NetException("Facebook Login cancel ", "FB_LOGIN_CANCEL", 999));
            }

            @Override
            public void onError(FacebookException error) {
                Log.e(TAG, "onError called");
                error.printStackTrace();

            }
        });

        LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList("public_profile", "email"));

    }

    public void OnActivityResult(int requestCode, int resultCode, Intent data) {

        if (callbackManager != null) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.e(TAG, "OnActivityResult callbackManager null");
        }

    }

    public String getToken() {
        return facebookToken;
    }

}
