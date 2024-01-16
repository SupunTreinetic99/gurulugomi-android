package com.treinetic.whiteshark.fragments.home


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.fragments.FragmentRefreshable
import com.treinetic.whiteshark.activity.BaseActivity
import com.treinetic.whiteshark.activity.ConnectionActivity
import com.treinetic.whiteshark.activity.LoginActivity
import com.treinetic.whiteshark.activity.MainActivity
import com.treinetic.whiteshark.adapters.NestedRecyclerAdapter
import com.treinetic.whiteshark.constance.Contants
import com.treinetic.whiteshark.customview.BottomLoginSheet
import com.treinetic.whiteshark.databinding.FragmentEventBinding
import com.treinetic.whiteshark.databinding.FragmentHomeBinding
import com.treinetic.whiteshark.dialog.MaterialDialogs
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.fragments.BaseFragment
import com.treinetic.whiteshark.models.BookCategory
import com.treinetic.whiteshark.models.appupdate.Update
import com.treinetic.whiteshark.services.CartService
import com.treinetic.whiteshark.services.UpdateService
import com.treinetic.whiteshark.services.UserService
import com.treinetic.whiteshark.util.Connections


class HomeFragment : BaseFragment(), FragmentRefreshable {

    private val logTag = "HomeFragment"
    private var _binding : FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View
    private lateinit var homeRecycler: RecyclerView
    private lateinit var nestedRecyclerAdapter: NestedRecyclerAdapter
    private lateinit var model: HomeViewModel
    private lateinit var linearLayoutManager: LinearLayoutManager
    private var materialDialogs: MaterialDialogs = MaterialDialogs()
    private var firstVisibleInListview: Int = 0
    private var isDialogAlredyShow = false
    private var bottomSheetLoginDialog: BottomLoginSheet? = null
    var optionMenu: Menu? = null

    companion object {
        var isShowLogoutMsg = false
        fun newInstance(): HomeFragment {
            val instance = HomeFragment()
            return instance
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isDialogAlredyShow = false
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        mainView = binding.root
        setHasOptionsMenu(true)
        checkUpdateMsg()
        model = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
        model.setIsLoad(false)
        binding.customListView.showLoading()
        checkInternetConnection()
        listenToErrors()
        model.fetchHomeData()
           setupToolBar()
        showLogoutSnackBar()
//        viewBottomLoginSheet(true)
        if (!isUserLogged()) {
            model.cartItemCount.value = 0
            model.getLocalCartItemCount()
            observeLocalCartItemCount()
        }
        return mainView
    }

    private fun showLogoutSnackBar() {
        Log.e(logTag,"showLogoutSnackBar $isShowLogoutMsg")
        if (isShowLogoutMsg) {
            showMessageSnackBar(mainView, "You have successfully logged out")
            isShowLogoutMsg = false
        }
    }

    private fun checkUpdateMsg() {

        if (UpdateService.isDisplayHomeMsg) {
            return
        }

        UpdateService.instance.appUpdate?.update?.let { update ->
            try {
                val isHomeMsg: Boolean =
                    update.extras?.get(Update.IS_HOME_MESSAGE_DISPLAY).toString().toBoolean()

                if (isHomeMsg) {

                    UpdateService.isDisplayHomeMsg = true
                    context?.let { cContext ->
                        materialDialogs.getDialog(
                            context = cContext,
                            title = update.extras?.get(Update.HOME_MESSAGE_TITTLE).toString(),
                            message = update.extras?.get(Update.HOME_MESSAGE_BODY).toString(),
                            positiveText = "OK",
                            positiveClick = {
                                it.dismiss()
                            },
                            cancelable = true,
                            cancelTouchOutSide = true

                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(
                    logTag,
                    "$e\nError occurred when show dialog message in HomeFragment"
                )
                e.printStackTrace()
            }
        }
    }


    private fun checkInternetConnection() {
        if (Connections.getInstance().isNetworkConnected()) {
            getHomeData()
        } else {
            ConnectionActivity.show(requireContext())
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getHomeData() {
        model.getHomeData().observe(viewLifecycleOwner) { t: BookCategory? ->
            binding.customListView.refreshLayout.isRefreshing = false
            (activity as BaseActivity).hideTopLoading()
            if (!model.getIsLoad()) {
                initNestedRecycler(books = t!!)
                binding.customListView.hideLoading()
            } else {
                (activity as BaseActivity).hideTopLoading()
                binding.customListView.hideLoadingWithoutAnimation()
                nestedRecyclerAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun initNestedRecycler(books: BookCategory) {
        model.setIsLoad(true)
        binding.customListView.hideLoading()
        nestedRecyclerAdapter = NestedRecyclerAdapter(books, requireContext())
        nestedRecyclerAdapter.onClick = { position, book, list ->

        }

        nestedRecyclerAdapter.loadMore = { position ->
            (activity as BaseActivity).showTopLoading()
            model.loadMoreData()

        }
        binding.customListView.isEnableSwipeRefresh = true
        binding.customListView.onRefreshCallback = {
            //            it?.isRefreshing = false
            model.setIsLoad(false)
            model.fetchHomeData(true)
        }
        homeRecycler = binding.customListView.recyclerView.apply {
            layoutManager = LinearLayoutManager(
                context,
                RecyclerView.VERTICAL,
                false
            )

            linearLayoutManager = layoutManager as LinearLayoutManager
            firstVisibleInListview = linearLayoutManager.findFirstVisibleItemPosition()
            adapter = nestedRecyclerAdapter

        }

        homeRecycler.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(
                recyclerView: RecyclerView,
                dx: Int,
                dy: Int
            ) {
                if (dy > 0) {
                    viewBottomLoginDialog(true)
                } else if (dy < 0) {
                    viewBottomLoginDialog(false)
                }
            }

        })

    }

    private fun viewBottomLoginDialog(isShow: Boolean) {
        try {
            if (UserService.getInstance().isUserLogged()) {
                return
            }

            if (bottomSheetLoginDialog == null) {
                bottomSheetLoginDialog = binding.bottomSheetDialogCV
                binding.bottomSheetDialogCV.onBtnClick = {
                    val intent = Intent(context, LoginActivity::class.java)
                    startActivityForResult(intent, Contants.LOGIN_REQUEST_CODE)
                }
            }
            if (isShow) {
                if (isDialogAlredyShow) {
                    return
                }
                bottomSheetLoginDialog?.show()
                isDialogAlredyShow = true
            } else {
                if (bottomSheetLoginDialog != null) {
                    bottomSheetLoginDialog?.dismiss()
                    bottomSheetLoginDialog = null
                }
                isDialogAlredyShow = false
            }
        } catch (err: Exception) {
            Log.e(logTag, err.printStackTrace().toString())
        }
    }


    private fun listenToErrors() {

        model.pageNetException.observe(viewLifecycleOwner, Observer<NetException> {
            it?.let {
                binding.customListView.refreshLayout.isRefreshing = false
                (activity as BaseActivity).hideTopLoading()
            }

        })

        model.netException.observe(viewLifecycleOwner, Observer<NetException> {

            it?.let {
                activity?.runOnUiThread {
                    binding.customListView.refreshLayout.isRefreshing = false
                    if (!isErrorHandled(it)) {
                        showErrorSnackBar(binding.customListView, "Something went wrong")
                    }
                }
            }
        })
    }

    private fun setupToolBar() {
        Log.d(logTag, "*****setupToolBar home frag")
        val toolBar = (activity as MainActivity).toolBar
        toolBar.title = requireContext().resources.getString(R.string.app_name)
        val actionbar: ActionBar? = (activity as AppCompatActivity).supportActionBar
        actionbar?.apply {
            Log.d(logTag, "*****setupToolBar home frag action")
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu)
        }

    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        setOptionMenuVisibility(
            menu,
            false,
            true,
            true,
            true,
            false

        )

        if (UserService.getInstance().isUserLogged()) {
            setCount(requireContext(), CartService.getInstance().getCartSize().toString(), menu)
        } else {
            setCount(requireContext(), model.cartItemCount.value.toString(), menu)
            /*  LocalCartService.instance.getAllCartItems(success = {

                  this.activity?.runOnUiThread {
                      setCount(context!!, it.size.toString(), menu)
                  }

              })*/
        }

    }

    private fun observeLocalCartItemCount() {
        model.cartItemCount.observe(viewLifecycleOwner) {
            if (isVisible && !isRemoving) {
                requireActivity().invalidateOptionsMenu()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupToolBar()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Contants.LOGIN_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null && data.getStringExtra("data") == Contants.SUCCESS_CALLBACK) {

                    Log.d(logTag, "login done ")
                    //refresh
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Log.d(logTag, "Cancelled....")
            }
        }
    }

    override fun onRefresh() {
        Log.d(logTag, "inside home onRefresh")
        //setupToolBar()
    }



}
