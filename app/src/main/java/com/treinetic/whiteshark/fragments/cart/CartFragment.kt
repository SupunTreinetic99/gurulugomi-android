package com.treinetic.whiteshark.fragments.cart


import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.treinetic.whiteshark.FragmentNavigation
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.activity.LoginActivity
import com.treinetic.whiteshark.activity.MainActivity
import com.treinetic.whiteshark.adapters.BookAdapter
import com.treinetic.whiteshark.constance.AdapterType
import com.treinetic.whiteshark.constance.Contants
import com.treinetic.whiteshark.databinding.FragmentBillingDetailsBinding
import com.treinetic.whiteshark.databinding.FragmentCartBinding
import com.treinetic.whiteshark.dialog.BottomDialogs
import com.treinetic.whiteshark.fragments.BaseFragment
import com.treinetic.whiteshark.models.Book
import com.treinetic.whiteshark.services.CartService
import com.treinetic.whiteshark.services.LocalCartService
import com.treinetic.whiteshark.services.OrderService
import com.treinetic.whiteshark.services.UserService
import com.treinetic.whiteshark.util.extentions.toCurrency


class CartFragment : BaseFragment(), View.OnClickListener {


    private var _binding : FragmentCartBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View
    private lateinit var bookAdapter: BookAdapter
    private lateinit var model: CartViewModel
    private var currentItems = mutableListOf<Book>()
    private lateinit var actionMenu: Menu
    lateinit var bottomDialogs: BottomDialogs
    var logTag = "CartFragment"

    companion object {
        fun getInstance(): CartFragment {
            return CartFragment()
        }

        lateinit var actionMenus: Menu

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        setupToolBar()

        actionMenu = actionMenus
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        mainView = binding.root
        model = ViewModelProvider(requireActivity())[CartViewModel::class.java]
        bottomDialogs = BottomDialogs()
        binding.bottomBar.visibility = View.GONE
        binding.btnCheckout.setOnClickListener(this)
        binding.btnShopMore.setOnClickListener(this)
        loadViewModel()
        showException()
        showDeleteIcon()
        return mainView
    }

    override fun onClick(view: View?) {
        val fragmentNavigation = FragmentNavigation.getInstance()
        when (view) {
            binding.btnCheckout -> {
                checkOut()
            }
            binding.btnShopMore -> fragmentNavigation.startHomeFragment(
                requireFragmentManager(), R.id.fragment_view
            )
        }
    }

    private fun checkOut() {

        if (!UserService.getInstance().isUserLogged()) {
            Log.d(logTag, "Not a Logged User.Login Started...")
            val intent = Intent(context, LoginActivity::class.java)
            startActivityForResult(intent, Contants.LOGIN_REQUEST_CODE)
            return
        }
        disableClick(binding.btnCheckout)
        if (CartService.getInstance().selectionCount(currentItems) > 0) {
            OrderService.getInstance().clearOrder()
            FragmentNavigation.getInstance().startOrderConfirm(
                requireFragmentManager(),
                R.id.fragment_view
            )
        } else {
            enableClick(binding.btnCheckout)
            showErrorSnackBar(mainView, resources.getString(R.string.cart_error))
        }
    }


    private fun loadViewModel() {
        model.loadCart()
        model.getCart().observe(viewLifecycleOwner, Observer<MutableList<Book>> { t ->
            enableClick(binding.btnCheckout)
            t?.let {
                binding.cartRecycler.hideLoading()
                initBookList(bookList = it)
                currentItems = t

            }
        })


        model.getResponse().observe(viewLifecycleOwner, Observer { t ->
            t?.let {
                showSuccessSnackBar(mainView, resources.getString(R.string.remove_success))
                model.resetDeleteResponse()
            }

        })

    }


    @SuppressLint("NotifyDataSetChanged")
    private fun initBookList(bookList: MutableList<Book>) {

        if (bookList.isEmpty()) {
            hideView(true, binding.bottomBar)
            binding.cartRecycler.showNoDataView()
            hideDeleteIcon()
            return
        }

        bookList.forEach { it.isClick = true }
        setCartItemSelectionCount(bookList)
        setCartItemsTotal(bookList)

        bookAdapter =
            BookAdapter(AdapterType.CART, bookList)
        bookAdapter.onClick = { position, book, itemview ->
            book.isClick = !book.isClick
            bookAdapter.notifyDataSetChanged()

            setCartItemSelectionCount(bookList)
            setCartItemsTotal(bookList)
        }
        binding.cartRecycler.recyclerView.apply {
            layoutManager = LinearLayoutManager(
                context,
                RecyclerView.VERTICAL,
                false
            )
            adapter = bookAdapter
        }
        showBottomSection()
    }

    private fun showBottomSection() {
        binding.bottomBar.visibility = View.VISIBLE
        val animator = binding.bottomBar.animate().alpha(1f).translationY(0f)
        animator.duration = 350
        animator.start()
    }

    private fun hideView(isHide: Boolean, view: View) {
        if (isHide) {
            view.visibility = View.GONE
        } else {
            view.visibility = View.VISIBLE
        }
    }

    private fun setCartItemSelectionCount(bookList: MutableList<Book>) {
        val count = CartService.getInstance().selectionCount(bookList)

        if (count > 0) {
            showDeleteIcon()
        } else {
            hideDeleteIcon()
        }

        val countText = "Item Selected ( $count )"
        binding.cartItemsCount.text = countText
    }

    private fun showDeleteIcon() {
        setOptionMenuVisibility(
            actionMenu,
            false, false, false, false, true
        )
    }

    private fun hideDeleteIcon() {
        setOptionMenuVisibility(
            actionMenu,
            false, false, false, false, false
        )
    }

    private fun setCartItemsTotal(bookList: MutableList<Book>? = null) {
        if (UserService.getInstance().isUserLogged()) {
            binding.cartTotal.text = CartService.getInstance().getCartValue().toCurrency("LKR")
        } else {
            var total = 0.0

            for (item in bookList!!.iterator()) {
                if (item.isClick) {
                    total += item.priceDetails.visiblePrice
                }
            }
            binding.cartTotal.text = total.toCurrency("LKR")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        CartService.getInstance().deSelectAll()
    }


    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        actionMenu = menu

        setOptionMenuVisibility(
            menu,
            false,
            false,
            false,
            false,
            false
        )
    }

    private fun setupToolBar() {
        val toolBar = (activity as MainActivity).toolBar
        toolBar.title = resources.getString(R.string.cart)
        (activity as AppCompatActivity).setSupportActionBar(toolBar)
        val actionbar: ActionBar? = (activity as AppCompatActivity).supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> {
//                model.deleteCartItem()
                showDeleteConfirm()
                return true
            }
        }
        return false
    }

    private fun showDeleteConfirm() {
        var bookCount = ""
        if (CartService.getInstance().getSelectedItemList().size > 1) bookCount = "s"
        bottomDialogs.getConfirmDialog(
            requireContext(),
            "Confirm",
            "Are you sure you want to delete this book$bookCount from cart?",
            "Ok",
            {
                model.deleteCartItem()
                it.dismiss()
            },
            "Cancel",
            {
                it.dismiss()
            }
        ).show()
    }


//    private fun showDeleteDialog() {
//        context?.let {
//            MaterialDialogs().getConfirmDialog(
//                it,
//                R.string.dialog_tittle,
//                R.string.dialog_sub_title
//            ).show {
//                positiveButton(R.string.btn_accept) { dialog ->
//                    model.deleteCartItem()
//                }
//                negativeButton(R.string.btn_cancel) { dialog ->
//
//                }
//            }
//        }
//    }

    private fun enableClick(view: View) {
        view.isClickable = true
    }

    private fun disableClick(view: View) {
        view.isClickable = false
    }

    private fun showException() {
        model.getNetException().observe(viewLifecycleOwner, Observer { t ->
            enableClick(binding.btnCheckout)
            t?.let {
                if (isErrorHandled(it)) {
                } else {
                    t.message?.let {
                        showErrorSnackBar(mainView, getString(R.string.error_msg))
                    }

                }
            }
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        model.createCartFromLocalCart()
        if (resultCode == Activity.RESULT_OK) {
            binding.cartRecycler.showLoading("Preparing Cart")
            model.loadCart()
        }

    }
}
