package com.treinetic.whiteshark.fragments.bookcategory


import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.recyclerview.widget.GridLayoutManager
import com.treinetic.whiteshark.FragmentNavigation
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.activity.MainActivity
import com.treinetic.whiteshark.adapters.CategoryBookAdapter
import com.treinetic.whiteshark.databinding.FragmentBillingDetailsBinding
import com.treinetic.whiteshark.databinding.FragmentBookCategoryBinding
import com.treinetic.whiteshark.fragments.BaseFragment
import com.treinetic.whiteshark.fragments.FragmentRefreshable
import com.treinetic.whiteshark.models.Category
import com.treinetic.whiteshark.util.PixelConvert


class BookCategoryFragment : BaseFragment(), FragmentRefreshable {

    private var _binding : FragmentBookCategoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View
    private var categoryName: String = ""
    private lateinit var bookAdapter: CategoryBookAdapter
    private val logTag: String = "BookCategory"
    private lateinit var model: BookCategoryViewModel


    companion object {
        const val CATEGORY = "CATEGORY"
        const val AUTHOR = "AUTHOR"
        const val PUBLISHER = "PUBLISHER"

        fun newInstance(categoryId: String): BookCategoryFragment {
            val instance = BookCategoryFragment()
            val args = Bundle()
            args.putString("categoryId", categoryId)
            instance.arguments = args
            return instance
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBookCategoryBinding.inflate(inflater, container, false)
        mainView = binding.root
        model = ViewModelProvider(requireActivity())[BookCategoryViewModel::class.java]
        observeData()
        setHasOptionsMenu(true)
        model.setIsLoad(false)
        arguments?.getString("categoryId")?.let {
            model.loadCategory(it)
            categoryName = model.getCategoryName()
            getCategory(it)
        }
        Log.d(logTag, "Book category fragment called")
        binding.bookList.noDataText = "No Books Found"
        return mainView
    }

    override fun onResume() {
        super.onResume()
        arguments?.getString("categoryId")?.let {
        }
        setupToolBar()
        requireActivity().invalidateOptionsMenu()
    }

    private fun getCategory(categoryId: String) {
        model.fetchCategory(categoryId)

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeData() {
        model.category.observe(viewLifecycleOwner,
            Observer<Category> { t: Category? ->
                Log.d(logTag, "category Observer Called")
                t?.category?.let {
                    categoryName = it
                }
                t?.let { it ->
                    Log.d(logTag, "category Observer Have Data")

                    if (!model.getIsLoad() && t.books != null) {
                        binding.bookList.hideLoading()
                        model.setIsLoad(true)
                        initBookList(it)
                        Log.d(logTag, " Category adapter initialized ...")
                    } else if (model.getIsLoad()) {
                        binding.bookList.hideLoadingWithoutAnimation()
                        binding.bookList.recyclerView.adapter?.let {
                            it.notifyDataSetChanged()
                            Log.d(logTag, " Category adapter notified ...")
                            return@Observer
                        }
                        initBookList(it)
//                        bookAdapter?.notifyDataSetChanged()
                        Log.d(logTag, " Category adapter notified ...")
                    }
                    return@Observer
                }
            }
        )

        model.error.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                model.netException?.let {
                    if (isErrorHandled(it)) {
                        return@Observer
                    }
                    Handler(Looper.getMainLooper()).post {
                        showMessageSnackBar(mainView, "Something went wrong")
                    }
                }

            }
        })
    }

    private fun initBookList(category: Category) {
        category.books?.data?.let {
            if (it.isEmpty()) {
                binding.bookList.showNoDataView()
                return
            } else {
                binding.bookList.hideLoading()

            }
        }

        bookAdapter = CategoryBookAdapter(
            CategoryBookAdapter.CategoryTypes.DETAILS,
            category
        )

        bookAdapter.onClick = { index, book, view ->
            FragmentNavigation()
                .startBookProfile(requireFragmentManager(), R.id.fragment_view, book)

        }
        bookAdapter.endPosition = { nextPageUrl, categoryId ->
            if (nextPageUrl != null) {
//                model.loadNextPageData(nextPageUrl, categoryId)
                model.getNextPage()
            }
        }

        binding.bookList.recyclerView.apply {
            layoutManager = GridLayoutManager(
                context, PixelConvert(activity as Activity)
                    .bookInRow(resources.getDimension(R.dimen.book_width))
            )
            adapter = bookAdapter
        }
    }

    private fun setupToolBar() {
        val toolBar = (activity as MainActivity).toolBar
        toolBar.title = categoryName
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
            menu,
            false, false, false, false,
            false
        )
    }

    override fun onRefresh() {
        Log.d(logTag, "inside cata onRefresh")
        setupToolBar()
    }
}
