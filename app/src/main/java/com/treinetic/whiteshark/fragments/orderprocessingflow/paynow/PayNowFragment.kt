package com.treinetic.whiteshark.fragments.orderprocessingflow.paynow

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import br.com.simplepass.loadingbutton.customViews.CircularProgressButton
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.treinetic.whiteshark.FragmentNavigation
import com.treinetic.whiteshark.MyApp
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.constance.PaymentProviders
import com.treinetic.whiteshark.databinding.BottomSheetPaynowBinding
import com.treinetic.whiteshark.databinding.FragmentOtpConfirmBottomSheetBinding
import com.treinetic.whiteshark.fragments.BaseFragment
import com.treinetic.whiteshark.models.PaymentData
import com.treinetic.whiteshark.payment.PayHerePayment
import com.treinetic.whiteshark.payment.PaymentImpl
import com.treinetic.whiteshark.services.CartService
import com.treinetic.whiteshark.services.OrderService
import com.treinetic.whiteshark.services.UserService
import com.treinetic.whiteshark.util.extentions.toJson

class PayNowFragment : BaseFragment() {
    private val logTag = "PayNowFragment"
    private var _binding : BottomSheetPaynowBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View
    private lateinit var BUTTON_BACKROUND: Drawable
    lateinit var model: PayNowViewModel
    var payment: PaymentImpl = PayHerePayment()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetPaynowBinding.inflate(inflater, container, false)
//        val dataBinding: BottomSheetPaynowBinding =
//            DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_paynow, container, false)
        binding.lifecycleOwner = this
        mainView = binding.root
        model = ViewModelProvider(requireActivity())[PayNowViewModel::class.java]
        binding.model = model
        ContextCompat.getDrawable(requireContext(), R.drawable.button_circular_shape)?.let {
            BUTTON_BACKROUND = it
        }
        setButtonBkg(binding.btnPayNow, BUTTON_BACKROUND)
        observeData()
        observeClearData()
        return mainView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        viewDiscountOptions()

    }

    private fun viewDiscountOptions() {
        UserService.getInstance().initLogin.services.let {
            if (!UserService.getInstance().initLogin.services.promoCode) {
                binding.promoCodeContainer.visibility = View.GONE
                binding.btnPromoCodeClear.visibility = View.GONE
                binding.promoDivider.visibility = View.GONE
            }
            if (!UserService.getInstance().initLogin.services.loyaltyPoints) {
                binding.loyaltyPointContainer.visibility = View.GONE
                binding.btnLoyaltyClear.visibility = View.GONE
                binding.loyaltyDivider.visibility = View.GONE
            }
            if (!UserService.getInstance().initLogin.services.eVoucher) {
                binding.eVoucherContainer.visibility = View.GONE
                binding.btneVoucherClear.visibility = View.GONE
                binding.eVoucherDivider.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        model.setData()

        hideClearBtns()
        showClearBtn()
        viewDiscountOptions()


        disableIfTotZero()
        initPayment()
    }

    private fun initPayment() {
        (payment as PayHerePayment).success = ::paymentSuccess
        (payment as PayHerePayment).error = ::paymentError
        (payment as PayHerePayment).cancelled = ::paymentCancelled
    }

    private fun disableIfTotZero() {

        OrderService.getInstance().currentOrder?.totalPaid?.let {
            if (it <= 0.0) {
                Log.d(logTag, "total payable is zero");

                OrderService.getInstance().currentOrder?.promotionCode?.promotionCodeAmount.let {
                    if (it == 0.00) {
                        disableClick(binding.promoCodeContainer)
                    }
                }

                OrderService.getInstance().currentOrder?.loyaltyPoints?.loyaltyPointsAmount?.let {
                    if (it == 0.00) {
                        disableClick(binding.loyaltyPointContainer)
                    }
                }

                OrderService.getInstance().currentOrder?.eVoucher?.eVoucherAmount?.let {
                    if (it == 0.00) {
                        disableClick(binding.eVoucherContainer)
                    }
                }
            } else {
                Log.d(logTag, "total payable is not zero");
                enableClick(binding.promoCodeContainer)
                enableClick(binding.loyaltyPointContainer)
                enableClick(binding.eVoucherContainer)
            }
        }
    }

    private fun showClearBtn() {
        OrderService.getInstance().currentOrder?.promotionCode?.let { promotionCode ->
            promotionCode.promotionCode?.let {
                binding.promoCodeChip.visibility = View.VISIBLE
                binding.btnPromoCodeClear.visibility = View.VISIBLE
            }
        }

        OrderService.getInstance().currentOrder?.loyaltyPoints?.loyaltyPointsAmount?.let {
            if (it != 0.00) {
                binding.btnLoyaltyClear.visibility = View.VISIBLE
            }
        }

        OrderService.getInstance().currentOrder?.eVoucher?.eVoucherAmount?.let {
            if (it != 0.00) {
                binding.btneVoucherClear.visibility = View.VISIBLE
            }
        }
    }

    private fun hideClearBtns() {
        binding.promoCodeChip.visibility = View.GONE
        binding.btnPromoCodeClear.visibility = View.INVISIBLE
        binding.btnLoyaltyClear.visibility = View.INVISIBLE
        binding.btneVoucherClear.visibility = View.INVISIBLE
    }

    private fun setButtonBkg(view: View, drawable: Drawable) {
        val button = view as CircularProgressButton
        button.background = drawable
    }

    private fun initUI() {
        binding.loadingView.hide()
        binding.promoCodeContainer.setOnClickListener { gotoPromoCode() }
        binding.loyaltyPointContainer.setOnClickListener { gotoLoyalty() }
        binding.eVoucherContainer.setOnClickListener { gotoeVoucher() }

        binding.btnPayNowBack.setOnClickListener { goBackClick() }
        binding.btnPayNow.setOnClickListener { payNowClick() }

        binding.btnPromoCodeClear.setOnClickListener { clearPromoCode() }
        binding.btnLoyaltyClear.setOnClickListener { clearLoyaltyPoints() }
        binding.btneVoucherClear.setOnClickListener { clearEVoucher() }


    }


    private fun gotoPromoCode() {
        if (OrderService.getInstance().currentOrder!!.hasPromoCode()) {
            showMessageSnackBar(
                binding.parentView,
                "You have already added a promo code.Please clear it to add a new one"
            )
            return
        }
        FragmentNavigation.getInstance()
            .startPromoFragment(requireFragmentManager(), R.id.bottomSheetContainer)
    }

    private fun payNowClick() {
        hideKeyboardFrom(requireContext(), binding.parentView)
        binding.loadingView.show()
        model.getPaymentLink()
        disableClick(binding.btnPayNow)
        disableClick(binding.btnPayNowBack)
        disableClick(binding.loyaltyPointContainer)
        disableClick(binding.promoCodeContainer)
        disableClick(binding.eVoucherContainer)
    }


    private fun gotoeVoucher() {
        if (OrderService.getInstance().currentOrder!!.hasEVoucher()) {
            showMessageSnackBar(
                binding.parentView,
                "You have already added an eVoucher.Please clear it to add a new one"
            )
            return
        }
        FragmentNavigation.getInstance()
            .starteVoucherFragment(requireFragmentManager(), R.id.bottomSheetContainer)
    }

    private fun gotoLoyalty() {

        if (OrderService.getInstance().currentOrder!!.hasLoyalty()) {
            showMessageSnackBar(
                binding.parentView,
                "You have already added loyalty points.Please clear it to update loyalty point amount"
            )
            return
        }

        FragmentNavigation.getInstance()
            .startRequestOtpFragment(requireFragmentManager(), R.id.bottomSheetContainer)
    }

    private fun goBackClick() {
        if (parentFragment is BottomSheetDialogFragment) {
            (parentFragment as BottomSheetDialogFragment).dismiss()
        }
    }


    private fun processPayment(paymentData: PaymentData) {



        // COMMERCIAL BANK PAYMENT
        if (paymentData.provider == PaymentProviders.COMMERCIAL_BANK) {
            FragmentNavigation.getInstance().startPaymentFragment(
                requireActivity().supportFragmentManager,
                R.id.fragment_view
            )
            goBackClick()
            return
        }


        val isValidOrderAmount = paymentData.paymentInfo.isAmountValidForPayHerePayment()

        // PAYHERE PAYMENT
        if (paymentData.provider == PaymentProviders.PAYHERE && isValidOrderAmount) {
            payment.prepare(paymentData)
            payment.doPayment(this)
            return
        }

        if (paymentData.provider == PaymentProviders.PAYHERE && !isValidOrderAmount) {
            submitEventForInvalidPayherePayment(paymentData)
            showMessageSnackBar(mainView, "Minimum allowed payment amount is LKR 50.00")
            setViewInteraction(true)
        }
    }

    private fun submitEventForInvalidPayherePayment(paymentData: PaymentData) {
        MyApp.crashlytics.log("Mount is not in rance")
        MyApp.analytics.logEvent(
            "invalid_payment",
            Bundle().apply {
                putString("data", paymentData.toJson())
                putString("user_id", UserService.getInstance().getUser().user_id)
            })
    }

    private fun observeData() {

        model.paymentData.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.loadingView.hide()
                if (it.needToPay) {
                    Log.e(logTag, "go to  Payment ${it.provider}")
                    FirebaseCrashlytics.getInstance()
                        .log("Payment provider= ${it.provider} order= ${it.paymentInfo.orderId}")

                    processPayment(it)
                    model.paymentData.postValue(null)
                    return@Observer

                } else if (!it.needToPay) {
                    Log.e(logTag, "go to  my library")
                    FragmentNavigation.getInstance()
                        .startMyLibrary(
                            requireActivity().supportFragmentManager,
                            R.id.fragment_view
                        )
                    CartService.getInstance().refreshCart()
                    goBackClick()
                }
                model.paymentData.value = null
            }
        })

        model.paymentException.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.loadingView.hide()

                setViewInteraction(true)

                enableClick(binding.btnPayNow)
                enableClick(binding.btnPayNowBack)
                enableClick(binding.loyaltyPointContainer)
                enableClick(binding.promoCodeContainer)
                enableClick(binding.eVoucherContainer)

                enableClick(binding.btnLoyaltyClear)
                enableClick(binding.btnPromoCodeClear)
                enableClick(binding.btneVoucherClear)
                showErrorSnackBar(mainView, it.message ?: "Payment Failed")
                model.paymentException.postValue(null)
            }
        })

        model.orderSuccess.observe(viewLifecycleOwner, Observer {
            it?.let {
                Log.d(logTag, " orderSuccess observer  called")
                FragmentNavigation.getInstance().startMyLibrary(
                    requireActivity().supportFragmentManager,
                    R.id.fragment_view
                )
                goBackClick()
                model.orderSuccess.value = null
            }
        })
        model.orderError.observe(viewLifecycleOwner, Observer {
            it?.let {
                Log.d(logTag, " orderError observer  called")
                showMessageSnackBar(mainView, "Something went wrong")
                Handler(Looper.getMainLooper()).post {
                    FragmentNavigation.getInstance().startHomeFragment(
                        requireActivity().supportFragmentManager,
                        R.id.fragment_view
                    )
                }
                goBackClick()
                model.orderError.value = null
            }
        })

    }

    private fun enableClick(view: View) {
        view.isClickable = true
    }

    private fun disableClick(view: View) {
        view.isClickable = false
    }

    private fun clearEVoucher() {
        setViewInteraction(false)
        binding.loadingView.show()
        model.clearEvoucher()
    }

    private fun clearLoyaltyPoints() {
        setViewInteraction(false)
        binding.loadingView.show()
        model.clearLoyaltyPoints()
    }

    private fun clearPromoCode() {
        setViewInteraction(false)
        binding.loadingView.show()
        model.clearPromoCode()
    }

    private fun observeClearData() {

        model.promoCodeClear.observe(viewLifecycleOwner, Observer {
            it.let {
                binding.loadingView.hide()
                setViewInteraction(true)
                model.setData()
                hidePromoClearBtn()
                model.promoCodeClear.value = null
            }
        })
        model.loyaltyPointsClear.observe(viewLifecycleOwner, Observer {
            it.let {
                binding.loadingView.hide()
                setViewInteraction(true)
                model.setData()
                hideLoyaltyCLearBtn()
                model.loyaltyPointsClear.value = null
            }
        })
        model.eVoucherClear.observe(viewLifecycleOwner, Observer {
            it.let {
                binding.loadingView.hide()
                setViewInteraction(true)
                model.setData()
                hideeVoucherClearBtn()
                model.eVoucherClear.value = null
            }
        })
    }

    private fun hidePromoClearBtn() {
        binding.promoCodeChip.visibility = View.GONE
        binding.btnPromoCodeClear.visibility = View.INVISIBLE
    }

    private fun hideLoyaltyCLearBtn() {
        binding.btnLoyaltyClear.visibility = View.INVISIBLE
    }

    private fun hideeVoucherClearBtn() {
        binding.btneVoucherClear.visibility = View.INVISIBLE
    }

    private fun setViewInteraction(enable: Boolean) {

        binding.promoCodeContainer.isClickable = enable
        binding.loyaltyPointContainer.isClickable = enable
        binding.eVoucherContainer.isClickable = enable
        binding.promoCodeContainer.isClickable = enable

        binding.btnPromoCodeClear.isClickable = enable
        binding.btnLoyaltyClear.isClickable = enable
        binding.btneVoucherClear.isClickable = enable
        binding.btnPayNow.isClickable = enable
        binding.btnPayNowBack.isClickable = enable

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        (payment as PayHerePayment).onActivityResult(requestCode, resultCode, data)

    }


    fun paymentSuccess(message: String?) {
        Log.d(logTag, "paymentSuccess -> called $message")
        binding.loadingView.show()
        showSuccessSnackBar(mainView, "Payment Success")
//        goBackClick()
        model.updateMylibrary()
    }

    fun paymentError(message: String?) {
        Log.e(logTag, "paymentError-> called $message")
        showMessageSnackBar(mainView, message ?: "Payment Failed")
        binding.loadingView.hide()
        setViewInteraction(true)
    }

    fun paymentCancelled(message: String?) {
        Log.e(logTag, "paymentCancelled-> called $message")
        binding.loadingView.hide()
        setViewInteraction(true)
        showMessageSnackBar(mainView, message ?: "Payment Cancelled")
    }


    override fun onDestroy() {
        super.onDestroy()
        payment.clear()
    }
}