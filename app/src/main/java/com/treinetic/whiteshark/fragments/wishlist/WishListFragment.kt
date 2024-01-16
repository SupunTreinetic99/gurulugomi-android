package com.treinetic.whiteshark.fragments.wishlist


import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.treinetic.whiteshark.FragmentNavigation
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.activity.MainActivity
import com.treinetic.whiteshark.adapters.BookAdapter
import com.treinetic.whiteshark.constance.AdapterType
import com.treinetic.whiteshark.databinding.FragmentUserProfileBinding
import com.treinetic.whiteshark.databinding.FragmentWishListBinding
import com.treinetic.whiteshark.fragments.BaseFragment
import com.treinetic.whiteshark.fragments.FragmentRefreshable
import com.treinetic.whiteshark.models.Book
import com.treinetic.whiteshark.services.WishListService
import com.treinetic.whiteshark.util.extentions.clear


class WishListFragment : BaseFragment(), View.OnClickListener, FragmentRefreshable {

    private var _binding : FragmentWishListBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View
    private lateinit var wListView: RecyclerView
    private lateinit var currentBook: Book
    private lateinit var model: WishListViewModel
    private lateinit var actionMenu: Menu
    private var selectedBooks = 0

    private val logTag: String = "WishListFragment"
    private lateinit var bookAdapter: BookAdapter

    companion object {
        fun getInstance(): WishListFragment {
            return WishListFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        model = ViewModelProviders.of(this).get(WishListViewModel::class.java)
        observeData()
        _binding = FragmentWishListBinding.inflate(inflater, container, false)
        mainView = binding.root
        setHasOptionsMenu(true)
        binding.btnAddCart.setOnClickListener(this)
        binding.btnShopMore.setOnClickListener(this)
        binding.myWishList.showLoading()
        setupToolBar()
        fetchWishList()
        return mainView
    }


    private fun fetchWishList() {
        model.fetchWishList()
        model.getResponse().observe(viewLifecycleOwner, Observer { t ->
            t?.let {
                showSuccessSnackBar(mainView, resources.getString(R.string.remove_success_wishlist))
                model.clearDeleteResponse()
                setCartItemSelectionCount(model.updatedList)
            }
        })
    }


    override fun onClick(view: View?) {
        when (view) {
            binding.btnAddCart -> {
                disableClick(binding.btnAddCart)
                if (selectedBooks > 0) {
                    addToCartBtnClick()
                } else {
                    enableClick(binding.btnAddCart)
                    showMessageSnackBar(mainView, resources.getString(R.string.no_book_selected_in_wish_list))
                }
            }
            binding.btnShopMore -> FragmentNavigation().startHomeFragment(
                requireFragmentManager(),
                R.id.fragment_view
            )
        }
    }

    private fun addToCartBtnClick() {

        model.addToCart(model.getSelectedBooksForAddToCard(),
            success = {
                enableClick(binding.btnAddCart)
                Handler(Looper.getMainLooper()).post {
                    FragmentNavigation.getInstance()
                        .startCart(requireFragmentManager(), R.id.fragment_view)
                }

            },
            error = {
                enableClick(binding.btnAddCart)
                Handler(Looper.getMainLooper()).post {
                    showMessageSnackBar(mainView, "Something went wrong when updating the cart")
                }

            })

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initWishList(list: List<Book>) {

        if (list.isEmpty()) {
            binding.myWishList.showNoDataView()
            hideDeleteIcon()
            return
        } else {
            binding.myWishList.hideLoading()
        }

        if (model.isFetchedWishlist) {
            bookAdapter.bookList = list
            bookAdapter.notifyDataSetChanged()
            return
        }
        bookAdapter =
            BookAdapter(AdapterType.WISHLIST, list)
        bookAdapter.onClick = { _, book, _ ->
            run {
                book.isClick = !book.isClick
                setCartItemSelectionCount(list as MutableList<Book>)
                bookAdapter.notifyDataSetChanged()
            }
        }

        bookAdapter.onPopup = { _, book, v ->
            run {
                showPopup(v)
                currentBook = book
            }
        }

        bookAdapter.loadMore = { _, _ ->
            getNextPage()
        }

        wListView = binding.myWishList.recyclerView.apply {
            layoutManager = LinearLayoutManager(
                context,
                RecyclerView.VERTICAL,
                false
            )
            adapter = bookAdapter
        }
        showList()

    }

    private fun setCartItemSelectionCount(bookList: MutableList<Book>) {
//        Log.e(logTag, "selected book list count = $count ")
        val count = WishListService.getInstance().selectionCount(bookList)
        selectedBooks = count
        val getConditionOfDeleteButtonVisible = bookList.size
        if (getConditionOfDeleteButtonVisible > 0) {
            showDeleteIcon()
        } else {
            hideDeleteIcon()
        }
    }

    private fun observeData() {
        model.getWishList().observe(viewLifecycleOwner, Observer<List<Book>> { list ->
            list?.let {
                binding.myWishList.hideLoading()
                initWishList(list)
            }
        })

        model.addToCartSuccess.observe(viewLifecycleOwner, Observer { })
        model.addToCartError.observe(viewLifecycleOwner, Observer {
            it.let {
                showMessageSnackBar(mainView, getString(R.string.error_msg))
                model.addToCartError.clear()
            }

        })
        model.deleteWishListError.observe(viewLifecycleOwner, Observer {

        })
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun getNextPage() {

        model.getNextPage(success = {
            bookAdapter.notifyDataSetChanged()
        }, error = {
            it.printStackTrace()
            activity?.runOnUiThread {

            }
        })

    }

    private fun showDeleteIcon() {
        if (!::actionMenu.isInitialized) return
        setOptionMenuVisibility(
            actionMenu,
            false,
            false,
            false,
            false,
            true
        )
    }

    private fun hideDeleteIcon() {
        if (!::actionMenu.isInitialized) return
        setOptionMenuVisibility(
            actionMenu,
            false,
            false,
            false,
            false,
            false
        )
    }


    private fun showList() {
        val animator: ViewPropertyAnimator = binding.myWishList.animate()
        animator.duration = 350
        animator.alpha(1f).start()
    }


    private fun setupToolBar() {

        val toolBar = (activity as MainActivity).toolBar
        toolBar.title = resources.getString(R.string.wish_list)

        (activity as AppCompatActivity).setSupportActionBar(toolBar)
        val actionbar: ActionBar? = (activity as AppCompatActivity).supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }

    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        actionMenu = menu
        super.onPrepareOptionsMenu(menu)
        setOptionMenuVisibility(
            menu, false,
            false,
            false,
            false,
            false
        )
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.action_delete -> {
                deleteFromWishList()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun deleteFromWishList() {
        if (model.getSelectedBooks().isEmpty()) {
            return
        }
        model.deleteFromWishList(model.getSelectedBooks(), error = {
            it.printStackTrace()
            it.let {
                showMessageSnackBar(mainView, "Error in Wish list update")
            }

        })

    }

    private fun showPopup(v: View) {
        val popup = PopupMenu(requireContext(), v)
        popup.setOnMenuItemClickListener(this::onMenuItemClick)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.popup, popup.menu)
        popup.show()
    }

    private fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.profile -> {
                currentBook.let {
                    FragmentNavigation.getInstance()
                        .startBookProfile(requireFragmentManager(), R.id.fragment_view, it)
                }

                true
            }
            else -> false
        }
    }


    override fun onDestroy() {
        WishListService.getInstance().clearAllSelected()
        super.onDestroy()
    }

    private fun disableClick(view: View) {
        view.isClickable = false
    }

    private fun enableClick(view: View) {
        view.isClickable = true
    }

    override fun onRefresh() {
        Log.d(logTag, "inside search onRefresh")
        setupToolBar()
    }

}
