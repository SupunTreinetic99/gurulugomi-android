package com.treinetic.whiteshark

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.treinetic.whiteshark.constance.Screens
import com.treinetic.whiteshark.fragments.*
import com.treinetic.whiteshark.fragments.addreview.AddReviewFragment
import com.treinetic.whiteshark.fragments.allreviews.AllReviewsFragment
import com.treinetic.whiteshark.fragments.authorbooks.AuthorBooksFragment
import com.treinetic.whiteshark.fragments.authors.AuthorsFragment
import com.treinetic.whiteshark.fragments.profilescategory.ProfileCategoryFragment
import com.treinetic.whiteshark.fragments.billingdetails.BillingDetailsFragment
import com.treinetic.whiteshark.fragments.bookcategory.BookCategoryFragment
import com.treinetic.whiteshark.fragments.bookprofile.BookProfileFragment
import com.treinetic.whiteshark.fragments.cart.CartFragment
import com.treinetic.whiteshark.fragments.orderprocessingflow.addpromo.AddPromoFragment
import com.treinetic.whiteshark.fragments.orderprocessingflow.changenumber.ChangeNumberFragment
import com.treinetic.whiteshark.fragments.orderprocessingflow.otpconfirmation.OtpConfirmFragment
import com.treinetic.whiteshark.fragments.orderprocessingflow.paynow.PayNowFragment
import com.treinetic.whiteshark.fragments.orderprocessingflow.requestotp.RequestOtpFragment
import com.treinetic.whiteshark.fragments.orderprocessingflow.redeemloyaltypoints.RedeemLoyaltyPointsFragment
import com.treinetic.whiteshark.fragments.categorybase.CategoryBaseFragment
import com.treinetic.whiteshark.fragments.devices.DeviceListFragment
import com.treinetic.whiteshark.fragments.event.EventFragment
import com.treinetic.whiteshark.fragments.home.HomeFragment
import com.treinetic.whiteshark.fragments.library.MyLibraryFragment
import com.treinetic.whiteshark.fragments.login.LoginFragment
import com.treinetic.whiteshark.fragments.orderconfirm.OrderConfirmFragment
import com.treinetic.whiteshark.fragments.orderprocessingflow.evoucher.EVoucherFragment
import com.treinetic.whiteshark.fragments.payment.PaymentFragment
import com.treinetic.whiteshark.fragments.promotions.PromotionFragment
import com.treinetic.whiteshark.fragments.publisherbooks.PublisherBookFragment
import com.treinetic.whiteshark.fragments.publishers.PublishersFragment
import com.treinetic.whiteshark.fragments.register.RegisterFragment
import com.treinetic.whiteshark.fragments.removedevices.RemoveDevicesFragment
import com.treinetic.whiteshark.fragments.search.SearchFragment
import com.treinetic.whiteshark.fragments.searchresult.SearchResultFragment
import com.treinetic.whiteshark.fragments.userprofile.UserProfileFragment
import com.treinetic.whiteshark.fragments.wishlist.WishListFragment
import com.treinetic.whiteshark.models.Book
import com.treinetic.whiteshark.fragments.purchased.PurchasedFragment


class FragmentNavigation {

    companion object {
        private val instance = FragmentNavigation()
        fun getInstance(): FragmentNavigation =
            instance
    }

    private fun setTransitions(transaction: FragmentTransaction) {
        transaction.setCustomAnimations(
            R.anim.fade_in,
            R.anim.fade_out,
            R.anim.fade_in,
            R.anim.fade_out
        )
    }


    private fun setLeftSlideTransition(transaction: FragmentTransaction) {
        transaction.setCustomAnimations(
            R.anim.enter_from_left,
            R.anim.exit_from_left,
            R.anim.fade_in,
            R.anim.fade_out
        )
    }

    fun startHomeFragment(
        supportFragmentManager: FragmentManager,
        container: Int,
        isShowMsg: Boolean = false
    ) {

            val fragment = HomeFragment.newInstance()
            HomeFragment.isShowLogoutMsg = isShowMsg
            val transaction = supportFragmentManager.beginTransaction()
            setTransitions(transaction)
            transaction
                .replace(container, fragment, Screens.HOME)
                .addToBackStack(Screens.HOME)
                .commit()
    }


    fun startBookProfile(supportFragmentManager: FragmentManager, container: Int, book: Book) {
        BookProfileFragment.initbook = book
        val fragment = BookProfileFragment.newInstance(book)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(container, fragment, Screens.BOOK_PROFILE)
        transaction.addToBackStack(Screens.BOOK_PROFILE)
        transaction.commit()

    }

    fun startBookProfile(
        supportFragmentManager: FragmentManager,
        container: Int,
        book: Book,
        view: ImageView,
        tag: String = "sharedImage"
    ) {
        val fragment = BookProfileFragment.newInstance(book)

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            fragment.sharedElementEnterTransition = ImageTransition()
//            fragment.enterTransition = Fade()
//            fragment.exitTransition = Fade()
//            fragment.sharedElementReturnTransition = ImageTransition()
//            ViewCompat.setTransitionName(view, tag)
//        }
        BookProfileFragment.initbook = book

        val transaction = supportFragmentManager.beginTransaction()
        ViewCompat.getTransitionName(view)?.let {
            transaction.addSharedElement(view, it)
        }
        transaction.setReorderingAllowed(true)
        transaction.apply {
            addSharedElement(view, tag)
        }
        transaction.replace(container, fragment)
        transaction.addToBackStack("sharedImage")
        transaction.commit()

    }

    fun startUserProfile(supportFragmentManager: FragmentManager, container: Int) {
        val fragment = UserProfileFragment.newInstance()
        val transaction = supportFragmentManager.beginTransaction()
//        setLeftSlideTransition(transaction)
        transaction.replace(container, fragment, Screens.USER_PROFILE)
        transaction.addToBackStack(Screens.USER_PROFILE)
        transaction.commit()

    }

    fun startPublisher(supportFragmentManager: FragmentManager, container: Int) {
        val fragment = PublishersFragment.newInstance()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(container, fragment, Screens.PUBLISHER)
        transaction.addToBackStack(Screens.PUBLISHER)
        transaction.commit()
    }

    fun startAuthors(supportFragmentManager: FragmentManager, container: Int) {
        val fragment = AuthorsFragment.newInstance()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(container, fragment, Screens.AUTHORS)
        transaction.addToBackStack(Screens.AUTHORS)
        transaction.commit()
    }

    fun startPurchasedHistory(supportFragmentManager: FragmentManager, container: Int) {
        val fragment = PurchasedFragment.newInstance()
        val transaction = supportFragmentManager.beginTransaction()
        setTransitions(transaction)
        transaction.replace(container, fragment, Screens.PURCHASE_HISTORY)
        transaction.addToBackStack(Screens.PURCHASE_HISTORY)
        transaction.commit()

    }

    fun startBillingHistory(supportFragmentManager: FragmentManager, container: Int) {
        val fragment = BillingHistoryFragment.newInstance()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(container, fragment, Screens.BILLING_HISTORY)
        transaction.addToBackStack(Screens.BILLING_HISTORY)
        transaction.commit()

    }

    fun startCart(supportFragmentManager: FragmentManager, container: Int) {
        val fragment = CartFragment.getInstance()
        val transaction = supportFragmentManager.beginTransaction()
        setLeftSlideTransition(transaction)
        transaction.replace(container, fragment, Screens.CART)
        transaction.addToBackStack(Screens.CART)
        transaction.commit()

    }

    fun startBaseCategoryFragment(supportFragmentManager: FragmentManager, container: Int) {
        val fragment = CategoryBaseFragment.getInstance()
        val transaction = supportFragmentManager.beginTransaction()
        setTransitions(transaction)
        transaction.replace(container, fragment, Screens.CATEGORY)
        transaction.addToBackStack(Screens.CATEGORY)
        transaction.commit()

    }

    fun startLoginFragment(supportFragmentManager: FragmentManager, container: Int) {
        val fragment = LoginFragment.newInstance()
        val transaction = supportFragmentManager.beginTransaction()
        setTransitions(transaction)
        transaction.replace(container, fragment, Screens.LOGIN)
        transaction.addToBackStack(Screens.LOGIN)
        transaction.commit()

    }

    fun startRegisterFragment(supportFragmentManager: FragmentManager, container: Int) {
        val fragment = RegisterFragment.newInstance()
        val transaction = supportFragmentManager.beginTransaction()
        setTransitions(transaction)
        transaction.replace(container, fragment, Screens.REGISTER)
        transaction.addToBackStack(Screens.REGISTER)
        transaction.commit()

    }

    fun startPasswordFragment(supportFragmentManager: FragmentManager, container: Int) {
        val fragment = PasswordResetFragment.newInstance()
        val transaction = supportFragmentManager.beginTransaction()
        setTransitions(transaction)
        transaction.replace(container, fragment, Screens.PASSWORD_RESET)
        transaction.addToBackStack(Screens.PASSWORD_RESET)
        transaction.commit()

    }

    fun startCategoryBookFragment(
        supportFragmentManager: FragmentManager,
        container: Int,
        categoryId: String,
        type: String? = null,
        ref: String? = null
    ) {
        val fragment = BookCategoryFragment.newInstance(categoryId)
        val transaction = supportFragmentManager.beginTransaction()
//        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
//        setTransitions(transaction)
        transaction.add(container, fragment, Screens.BOOK_CATEGORY)
        transaction.addToBackStack(Screens.BOOK_CATEGORY)

        transaction.commit()

    }

    fun startWishlistFragment(supportFragmentManager: FragmentManager, container: Int) {
        Handler(Looper.getMainLooper()).post {
            val fragment = WishListFragment.getInstance()
            val transaction = supportFragmentManager.beginTransaction()
            setTransitions(transaction)
            transaction.replace(container, fragment, Screens.WISH_LIST)
            transaction.addToBackStack(Screens.WISH_LIST)
            transaction.commit()
        }


    }

    fun startMyLibrary(supportFragmentManager: FragmentManager, container: Int) {
        val fragment = MyLibraryFragment.newInstance()
        val transaction = supportFragmentManager.beginTransaction()
        setTransitions(transaction)
        transaction.replace(container, fragment, Screens.MY_LIBRARY)
        transaction.addToBackStack(Screens.MY_LIBRARY)
        transaction.commit()

    }

    fun startAllReview(supportFragmentManager: FragmentManager, container: Int, bookId: String) {
        val fragment = AllReviewsFragment.getInstance(bookId)
        val transaction = supportFragmentManager.beginTransaction()
        setTransitions(transaction)
        transaction.replace(container, fragment, Screens.ALL_REVIEW)
        transaction.addToBackStack(Screens.ALL_REVIEW)
        transaction.commit()

    }

    fun startAddReview(supportFragmentManager: FragmentManager, container: Int, book: Book) {
        val fragment = AddReviewFragment.newInstance(book)
        val transaction = supportFragmentManager.beginTransaction()
        setTransitions(transaction)
        transaction.replace(container, fragment, Screens.ADD_REVIEW)
        transaction.addToBackStack(Screens.ADD_REVIEW)
        transaction.commit()
    }


    fun startEventList(supportFragmentManager: FragmentManager, container: Int) {
        val fragment = EventFragment.newInstance()
        val transaction = supportFragmentManager.beginTransaction()
        setTransitions(transaction)
        transaction.replace(container, fragment, Screens.EVENTS)
        transaction.addToBackStack(Screens.EVENTS)
        transaction.commit()

    }

    fun startEventDetails(
        supportFragmentManager: FragmentManager,
        container: Int,
        eventId: String?,
        fromNotification: Boolean = false
    ) {
        val fragment = EventDetailsFragment.newInstance(eventId, fromNotification)
        val transaction = supportFragmentManager.beginTransaction()
        setTransitions(transaction)
        transaction.replace(container, fragment, Screens.EVENT_DETAILS)
        transaction.addToBackStack(Screens.EVENT_DETAILS)
        transaction.commit()

    }

    fun startSearch(supportFragmentManager: FragmentManager, container: Int) {
        val fragment = SearchFragment.newInstance()
        val transaction = supportFragmentManager.beginTransaction()
        setTransitions(transaction)
        transaction.replace(container, fragment, Screens.SEARCH)
        transaction.addToBackStack(Screens.SEARCH)
        transaction.commit()

    }


    fun startSearchResult(supportFragmentManager: FragmentManager, container: Int, type: String) {
        val fragment = SearchResultFragment.newInstance(type)
        val transaction = supportFragmentManager.beginTransaction()
        setTransitions(transaction)
        transaction.replace(container, fragment, Screens.SEARCH_RESULT)
        transaction.addToBackStack(Screens.SEARCH_RESULT)
        transaction.commit()

    }

    fun startBillingDetails(
        supportFragmentManager: FragmentManager,
        container: Int,
        goBack: Boolean = false
    ) {
        val fragment = BillingDetailsFragment.newInstance()
        fragment.arguments = Bundle().apply {
            putBoolean("goBack", goBack)
        }
        val transaction = supportFragmentManager.beginTransaction()
        setTransitions(transaction)
        transaction.replace(container, fragment, Screens.BILLING_DETAILS)
        transaction.addToBackStack(Screens.BILLING_DETAILS)
        transaction.commit()

    }

    fun startOrderConfirm(supportFragmentManager: FragmentManager, container: Int) {
        val fragment = OrderConfirmFragment.getInstance()
        val transaction = supportFragmentManager.beginTransaction()
        setTransitions(transaction)
        transaction.replace(container, fragment, Screens.ORDER_CONFIRM)
        transaction.addToBackStack(Screens.ORDER_CONFIRM)
        transaction.commit()

    }

    fun startBookPublish(supportFragmentManager: FragmentManager, container: Int) {
        val fragment = BookPublishFragment.newInstance()
        val transaction = supportFragmentManager.beginTransaction()
        setTransitions(transaction)
        transaction.replace(container, fragment, Screens.BOOK_PUBLISH)
        transaction.addToBackStack(Screens.BOOK_PUBLISH)
        transaction.commit()

    }

    fun startAboutFragment(supportFragmentManager: FragmentManager, container: Int) {
        val fragment = AboutFragment.newInstance()
        val transaction = supportFragmentManager.beginTransaction()
        setTransitions(transaction)
        transaction.replace(container, fragment, Screens.ABOUT)
        transaction.addToBackStack(Screens.ABOUT)
        transaction.commit()

    }

    fun startPromotionFragment(supportFragmentManager: FragmentManager, container: Int) {
        val fragment = PromotionFragment.newInstance()
        val transaction = supportFragmentManager.beginTransaction()
        setTransitions(transaction)
        transaction.replace(container, fragment, Screens.PROMOTION)
        transaction.addToBackStack(Screens.PROMOTION)
        transaction.commit()

    }


    fun startPaymentFragment(supportFragmentManager: FragmentManager, container: Int) {
        val fragment = PaymentFragment.newInstance()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(container, fragment, Screens.PAYMENT)
        setTransitions(transaction)
        transaction.addToBackStack(Screens.PAYMENT)
        transaction.commit()
    }

    fun startAuthorBook(
        supportFragmentManager: FragmentManager,
        container: Int,
        type: String,
        authorId: String,
        authorName: String = "",
        fromView: String = Screens.SEARCH_RESULT
    ) {
        val fragment = AuthorBooksFragment.newInstance(type, authorId, authorName)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(container, fragment, Screens.AUTHOR_BOOKS)
        setTransitions(transaction)
        transaction.addToBackStack(Screens.AUTHOR_BOOKS)
        transaction.commit()
    }

    fun startProfileCategoryBook(
        supportFragmentManager: FragmentManager,
        container: Int,
        type: String,
        id: String,
        name: String = "",
        img: String = ""
    ) {
        val fragment = ProfileCategoryFragment.newInstance(type, id, name, img)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(container, fragment, Screens.PROFILE_CATEGOREY)
        setTransitions(transaction)
        transaction.addToBackStack(Screens.PROFILE_CATEGOREY)
        transaction.commit()
    }


    fun startPublisherBook(
        supportFragmentManager: FragmentManager,
        container: Int,
        type: String,
        publisherId: String,
        publisherName: String = "",
        fromView: String = Screens.SEARCH_RESULT
    ) {
        val fragment = PublisherBookFragment.newInstance(type, publisherId, publisherName)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(container, fragment, Screens.PUBLISHER_BOOK)
        setTransitions(transaction)
        transaction.addToBackStack(Screens.PUBLISHER_BOOK)
        transaction.commit()
    }

    fun startDeviceList(supportFragmentManager: FragmentManager, container: Int) {
        val fragment = DeviceListFragment.getInstance()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(container, fragment, Screens.DEVICES)
        setTransitions(transaction)
        transaction.addToBackStack(Screens.DEVICES)
        transaction.commit()
    }

    fun startRemoveDeviceList(
        supportFragmentManager: FragmentManager,
        container: Int,
        token: String
    ) {
        val fragment = RemoveDevicesFragment.newInstance(token)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(container, fragment, Screens.REMOVE_DEVICES)
        setTransitions(transaction)
        transaction.addToBackStack(Screens.REMOVE_DEVICES)
        transaction.commit()
    }


    fun startPayNowFragment(supportFragmentManager: FragmentManager, container: Int) {
        val fragment =
            PayNowFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(container, fragment, Screens.PAY_NOW_SHEET)
        transaction.addToBackStack(Screens.PAY_NOW_SHEET)
        transaction.commit()
    }

    fun startPromoFragment(supportFragmentManager: FragmentManager, container: Int) {
        val fragment =
            AddPromoFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(container, fragment, Screens.ADD_PROMO_SHEET)
        transaction.addToBackStack(Screens.ADD_PROMO_SHEET)
        transaction.commit()
    }

    fun startRequestOtpFragment(supportFragmentManager: FragmentManager, container: Int) {
        val fragment =
            RequestOtpFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(container, fragment, Screens.REQUEST_OTP)
        transaction.addToBackStack(Screens.REQUEST_OTP)
        transaction.commit()
    }

    fun startChangeNumberFragment(supportFragmentManager: FragmentManager, container: Int) {
        val fragment =
            ChangeNumberFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(container, fragment, Screens.CHANGE_NUM_SHEET)
        transaction.addToBackStack(Screens.CHANGE_NUM_SHEET)
        transaction.commit()
    }


    fun starteVoucherFragment(supportFragmentManager: FragmentManager, container: Int) {
        val fragment = EVoucherFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(container, fragment, Screens.REQUEST_OTP)
        transaction.addToBackStack(Screens.REQUEST_OTP)
        transaction.commit()
    }

    fun startRedeemLoyaltyPointsFragment(supportFragmentManager: FragmentManager, container: Int) {

        val fragment = RedeemLoyaltyPointsFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(container, fragment, Screens.REDEEM_LOYALTY_POINTS)
        transaction.addToBackStack(Screens.REDEEM_LOYALTY_POINTS)
        transaction.commit()
    }

    fun startOtpConfirmFragment(supportFragmentManager: FragmentManager, container: Int) {
        val fragment =
            OtpConfirmFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(container, fragment, Screens.OTP_CONFIRM_SHEET)
        transaction.addToBackStack(Screens.OTP_CONFIRM_SHEET)
        transaction.commit()
    }
}



















