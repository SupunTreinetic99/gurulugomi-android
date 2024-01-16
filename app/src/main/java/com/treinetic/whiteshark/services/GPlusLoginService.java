package com.treinetic.whiteshark.services;

import android.content.Intent;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;
import com.jaredrummler.android.device.DeviceName;
import com.treinetic.whiteshark.R;
import com.treinetic.whiteshark.constance.DeviceInformation;
import com.treinetic.whiteshark.exceptions.NetException;
import com.treinetic.whiteshark.models.Login;


public class GPlusLoginService {

    private final String TAG = "GPlusLoginService";

    private static GPlusLoginService instance;
    public String gPlusToken;
    private static final int PERMISSIONS_DENIED = -506;
    private static final int GOOGLE_LOGIN_REQUEST_CODE = 1001;
    private static final int GOOGLE_LOGIN_CANCELED = -505;
    private static final String GOOGLE = "google";

    private Service.Success<Login> success;
    private Service.Error error;
    private GoogleApiClient mGoogleApiClient;


    public static GPlusLoginService getInstance() {

        if (instance == null) {
            instance = new GPlusLoginService();
        }
        return instance;

    }


    public void doLogin(AppCompatActivity activity, final Service.Success<Login> success, final Service.Error error) {


        this.success = success;
        this.error = error;

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestServerAuthCode(activity.getString(R.string.server_client_id))
                .requestProfile()
                .build();


        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.stopAutoManage(((AppCompatActivity) activity));
                mGoogleApiClient.disconnect();
                Log.e(TAG, "mGoogleApiClient connected and disconnected");
            }
            mGoogleApiClient = null;
        } else {
            Log.e(TAG, "mGoogleApiClient null");
        }

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .enableAutoManage(activity, connectionResult -> {

                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();


        Intent intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        activity.startActivityForResult(intent, GOOGLE_LOGIN_REQUEST_CODE);
    }


    public void OnActivityResult(int requestCode, int resultCode, Intent data) {

        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        handleSignInResult(task);
//
//        GoogleSignInResult googleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
//
//        googleSignInResult.isSuccess();
//        googleSignInResult.getSignInAccount().getServerAuthCode();

    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            assert account != null;
            String token = account.getServerAuthCode();
            if (token != null) {
                String deviceId = new DeviceInformation().getDeviceId();
                String deviceName = DeviceName.getDeviceName();
                UserService.Companion.getInstance().socialLogin(GOOGLE, token, deviceId, deviceName, success, error);
                return;
            }

            Log.e(TAG, "Server auth code not available");
            error.error(new NetException("Something went wrong", "SERVER_AUTHCODE_MISSING", 404));
            // Signed in successfully, show authenticated UI.

        } catch (Exception e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            if (e instanceof ApiException) {
                Log.w(TAG, "signInResult:failed code=" + ((ApiException) e).getStatusCode());
                Log.i(TAG, ((ApiException) e).toString());
            }
            Log.e(TAG, "Failed to handle the google login result");
//            error.error(new NetException("Something went wrong", "GPLUS_ERROR", 500));
            error.error(new NetException("Something went wrong", "GPLUS_CANCEL", GOOGLE_LOGIN_CANCELED));
        }
    }

    public void stop(AppCompatActivity activity) {
        try {
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                mGoogleApiClient.stopAutoManage(activity);
                mGoogleApiClient.disconnect();
                Log.e(TAG, "mGoogleApiClient disconnected");
            }
        } catch (Exception e) {
            Log.e(TAG, "mGoogleApiClient disconnect failed");
            e.printStackTrace();
        }

    }


}
