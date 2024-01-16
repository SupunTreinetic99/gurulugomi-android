package com.treinetic.whiteshark.fragments.payment;

import android.util.Log;
import android.webkit.JavascriptInterface;

import androidx.annotation.Keep;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.treinetic.whiteshark.FragmentNavigation;
import com.treinetic.whiteshark.R;
import com.treinetic.whiteshark.constance.Screens;
import com.treinetic.whiteshark.services.BookService;
import com.treinetic.whiteshark.services.CartService;
import com.treinetic.whiteshark.services.OrderService;

/**
 * Created by Nuwan on 2/20/19.
 */
@Keep
public class TrPayNow {

    private String TAG = "TrPayNow";
    private static FragmentManager fragmentManager;
    private Fragment fragment;


    @JavascriptInterface
    public void uiLoaded(String data) {
        if (data == null) data = "";
        Log.e(TAG, "uiLoaded called : " + data);
    }

    @JavascriptInterface
    public void paymentFailed(String data) {
        if (data == null) data = "";
        Log.e(TAG, "paymentFailed called : " + data);
        fragmentManager.popBackStack(Screens.ORDER_CONFIRM,FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    @JavascriptInterface
    public void paymentSuccess(String data) {
        Log.e(TAG, "paymentSuccess called");

    /*    if (fragment != null) {
            ((PaymentFragment) fragment).showSuccessSnackBar(fragment.getView(),
                    "Congratulations. Payment success");
        }*/

        OrderService.Companion.getInstance().clearOrder();

        updateMylibrary();
    }

    public void setFragmentManager(FragmentManager f) {
        fragmentManager = f;
    }

    public FragmentManager getFragmentManager() {
        return fragmentManager;
    }

    private void continueProcess() {
        FragmentNavigation.Companion.getInstance().startMyLibrary(getFragmentManager(), R.id.fragment_view);
    }

    private void updateMylibrary() {
        BookService.Companion.getInstance().fetchMyLibrary(result -> {
            updateCart();
            Log.d(TAG, "success fetch my library");
        }, exception -> {
            exception.printStackTrace();
            Log.e(TAG, "Error in fetch my library");
            updateCart();
        });
    }

    private void updateCart() {
        CartService.Companion.getInstance().loadCartRequest(result -> {
            Log.d(TAG, "success fetch cart");
            updatePurchaseHistory();
        }, exception -> {
            exception.printStackTrace();
            Log.e(TAG, "Error in fetch cart");
            updatePurchaseHistory();
        }, true);
    }

    private void updatePurchaseHistory() {
        OrderService.Companion.getInstance().getPurchasedHistory(
                result -> {
                    Log.d(TAG, "success update Purchase History");
                    continueProcess();
                },
                exception -> {
                    Log.e(TAG, "Error in update Purchase History");
                    continueProcess();
                }, true);
    }

//    private void updateWishlist() {
//        WishListService.Companion.getInstance().fetchWishList(result -> {
//            Log.d(TAG, "success fetch wishlist");
//            continueProcess();
//        }, exception -> {
//            exception.printStackTrace();
//            Log.e(TAG, "Error in fetch wishlist");
//            continueProcess();
//        });
//
//    }


    public Fragment getFragment() {
        return fragment;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }
}
