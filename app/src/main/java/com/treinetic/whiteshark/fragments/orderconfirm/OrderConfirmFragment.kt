package com.treinetic.whiteshark.fragments.orderconfirm


import android.annotation.SuppressLint
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.treinetic.whiteshark.FragmentNavigation
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.activity.MainActivity
import com.treinetic.whiteshark.adapters.OrderItemAdapter
import com.treinetic.whiteshark.databinding.FragmentLoginBinding
import com.treinetic.whiteshark.databinding.FragmentOrderConfimBinding
import com.treinetic.whiteshark.dialog.BottomLoadingDialog
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.fragments.BaseFragment
import com.treinetic.whiteshark.dialog.BottomSheetBaseDialog
import com.treinetic.whiteshark.models.BillingDetails
import com.treinetic.whiteshark.models.Order
import com.treinetic.whiteshark.models.OrderItem
import com.treinetic.whiteshark.models.PaymentData
import com.treinetic.whiteshark.services.CartService
import com.treinetic.whiteshark.services.OrderService
import com.treinetic.whiteshark.util.extentions.toCurrency


class OrderConfirmFragment : BaseFragment(), View.OnClickListener {

    private val logTag = "OrderConfirm"
    private var _binding : FragmentOrderConfimBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View
    private lateinit var selectedList: RecyclerView
    private lateinit var bookAdapter: OrderItemAdapter
    private lateinit var model: OrderConfirmViewModel
    private lateinit var currentList: MutableList<OrderItem>
    private lateinit var loadingDialog: BottomLoadingDialog
    private var dialog: BottomSheetDialog? = null
    private var isListLoading: Boolean = true

    companion object {
        fun getInstance(): OrderConfirmFragment {
            return OrderConfirmFragment()
        }
    }

    override fun onResume() {
        super.onResume()
//        model.initMethods()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOrderConfimBinding.inflate(inflater, container, false)
        mainView = binding.root
        model = ViewModelProvider(requireActivity())[OrderConfirmViewModel::class.java]
        disableBtn()
        getSelectedList()
        loadingDialog = BottomLoadingDialog()
        model.initMethods()
        setHasOptionsMenu(true)
        setupToolBar()
        binding.btnChange.setOnClickListener(this)
        binding.btnPayNow.setOnClickListener(this)
        binding.addBillingDetails.setOnClickListener(this)
        showToolbarBackButton()
        return mainView
    }

    override fun onClick(view: View?) {
        when (view) {
            binding.btnChange -> FragmentNavigation().startBillingDetails(
                requireFragmentManager(),
                R.id.fragment_view, true
            )
            binding.addBillingDetails -> {
                FragmentNavigation().startBillingDetails(
                    requireFragmentManager(),
                    R.id.fragment_view, true
                )
            }
            binding.btnPayNow -> {
                if (isListLoading) {
                    return
                }
                showPayNowBottomSheet()

//                disableClick(mainView.btnPayNow)
//                showLoading("Processing", "Processing your order")
//                clickPayNow()
            }

        }
    }

    var bottomDialog: BottomSheetBaseDialog? = null
    private fun showPayNowBottomSheet() {
        bottomDialog = BottomSheetBaseDialog()
        bottomDialog?.isCancelable = false
        bottomDialog?.onDismissListener = {
            model.order.postValue(OrderService.getInstance().currentOrder)
            setDiscount()
        }
        fragmentManager?.let { bottomDialog?.show(it, "BottomSheetFragment") }

    }

    private fun clickPayNow() {
        model.getPaymentLink()
        model.getDialogStatus().observe(this, Observer {
            it.let {
                dialog?.dismiss()
                model.getClearDialogStatus()
            }
        })

        model.getException().observe(this, Observer<NetException> {
            it?.let {
                if (!isErrorHandled(it)) {
                    showMessageSnackBar(mainView, getString(R.string.error_msg))
                }
                model.clearException()
            }

        })

        model.getPaymentData().observe(this, Observer<PaymentData> {
            it.let {
                hideLoading()
                enableClick(binding.btnPayNow)
                if (it.isValidUrl() && it.needToPay) {
                    Log.e(logTag, "go to  Payment")
                    FragmentNavigation.getInstance().startPaymentFragment(
                        requireFragmentManager(),
                        R.id.fragment_view
                    )
                    model.getPaymentData().removeObservers(this)
                    return@Observer
                } else if (!it.needToPay) {
                    Log.e(logTag, "go to  my library")
                    FragmentNavigation.getInstance()
                        .startMyLibrary(requireFragmentManager(), R.id.fragment_view)
                    CartService.getInstance().refreshCart()
                }
            }
        })
    }

    fun showLoading(title: String, msg: String) {
        dialog?.let {
            if (it.isShowing) it.dismiss()
        }

        dialog = loadingDialog.getLoadingDialog(requireContext(), title, msg)
        dialog?.show()

    }

    fun hideLoading() {
        dialog?.let {
            it.dismiss()
        }
    }


    private fun getSelectedList() {
        model.getOrder().observe(viewLifecycleOwner, Observer<Order> { t ->
            t?.let {
                initSelectedBookList(itemList = t.orderItems)
                currentList = t.orderItems
                enableBtn()
                model.order.value = null
            }
        })
        model.getBilling().observe(viewLifecycleOwner, Observer<BillingDetails> { t ->
            if (t.isFilled()) {
                setBillingDetails(t)
                showBillingDetails()
            } else {
                showAddBillingDetails()
                if (model.isShowAddBiling) {
                    FragmentNavigation.getInstance()
                        .startBillingDetails(requireFragmentManager(), R.id.fragment_view, true)
                    model.isShowAddBiling = false
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun setBillingDetails(billing: BillingDetails) {
//        mainView.address2.visibility = View.GONE
//        mainView.zipCode.visibility = View.GONE
        val name = billing.firstName + " " + billing.lastName
        binding.billingName.text = name + " , " + billing.email
//        mainView.address1.text = billing.address1 + ","

//        billing.address2?.let {
//            mainView.address2.text = it + ","
//            mainView.address2.visibility = View.VISIBLE
//        }

//        mainView.city.text = billing.addressCity + ","
//        mainView.country.text = billing.country + ","
//        mainView.email.text = billing.email
//        billing.zipCode?.let {
//            mainView.zipCode.text = it + "."
//            mainView.zipCode.visibility = View.VISIBLE
//        }

//        mainView.contact.text = UserService.getInstance().getUser()?.getFullContactNumber()
    }


    private fun initSelectedBookList(itemList: MutableList<OrderItem>) {
        selectedList = binding.selectedBookList
        bookAdapter = OrderItemAdapter(itemList)
        selectedList.apply {
            layoutManager = LinearLayoutManager(
                context, RecyclerView.VERTICAL, false
            )
            adapter = bookAdapter
        }
        val strCount = "Items (" + itemList.size.toString() + ")"
        binding.bookCount.text = strCount
        getTotalAmount()
    }

    private fun getTotalAmount() {
        model.getOrder().value?.let {
            //        val fullAmount = CartService.getInstance().getCartValue()
            val fullAmount = it.totalPaid
            binding.fullAmount.text = fullAmount.toCurrency(resources.getString(R.string.currency))
        }

        setDiscount()

    }


    private fun setDiscount() {
        model.getOrder().value?.let {
            if (!it.hasPromotion()) {
                binding.discountPrice.visibility = View.GONE
                binding.promotionName.visibility = View.GONE
                return@let
            }
            binding.discountPrice.visibility = View.VISIBLE
            val totalAmount = it.totalAmount
            binding.discountPrice.text =
                totalAmount.toCurrency(resources.getString(R.string.currency))
//            mainView.discountPrice.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG)
            binding.discountPrice.paintFlags =
                binding.discountPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            if (it.hasPromotions()) binding.promotionName.text = it.promotions[0].promotion
        }
    }

    private fun setupToolBar() {
        val toolBar = (activity as MainActivity).toolBar
        toolBar.title = resources.getString(R.string.order_confirmation)
        (activity as AppCompatActivity).setSupportActionBar(toolBar)
        val actionbar: ActionBar? = (activity as AppCompatActivity).supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        setOptionMenuVisibility(
            menu, false, false, false, false,
            false
        )
    }

    private fun showAddBillingDetails() {
        binding.billingInfoContainer.visibility = View.INVISIBLE
        binding.addBillingDataContainer.visibility = View.VISIBLE
        binding.btnChange.visibility = View.GONE
        binding.btnPayNow.visibility = View.GONE
    }

    private fun showBillingDetails() {
        binding.billingInfoContainer.visibility = View.VISIBLE
        binding.addBillingDataContainer.visibility = View.GONE
        binding.btnChange.visibility = View.VISIBLE
        binding.btnPayNow.visibility = View.VISIBLE
    }

    private fun enableClick(view: View) {
//        view.isClickable = true
    }

    private fun disableClick(view: View) {
//        view.isClickable = false
    }

    private fun disableBtn() {
        isListLoading = true
        binding.btnPayNow.isClickable = false
        binding.btnPayNow.setBackgroundColor(
            ContextCompat.getColor(
                mainView.context,
                R.color.grey_color
            )
        )
    }

    private fun enableBtn() {
        isListLoading = false
        binding.btnPayNow.isClickable = true
        binding.btnPayNow.setBackgroundColor(
            ContextCompat.getColor(
                mainView.context,
                R.color.colorPrimary
            )
        )
    }

}
