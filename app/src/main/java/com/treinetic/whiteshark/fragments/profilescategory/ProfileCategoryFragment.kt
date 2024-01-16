package com.treinetic.whiteshark.fragments.profilescategory


import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.treinetic.whiteshark.FragmentNavigation
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.activity.MainActivity
import com.treinetic.whiteshark.adapters.CategoryBookAdapter
import com.treinetic.whiteshark.customview.CustomNestedScrollView
import com.treinetic.whiteshark.databinding.FragmentProfileCaregoryBinding
import com.treinetic.whiteshark.databinding.PaymentFragmentBinding
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.fragments.BaseFragment
import com.treinetic.whiteshark.fragments.FragmentRefreshable
import com.treinetic.whiteshark.models.Books
import com.treinetic.whiteshark.models.Category
import com.treinetic.whiteshark.util.PixelConvert


class ProfileCategoryFragment : BaseFragment(), FragmentRefreshable {


    private var _binding : FragmentProfileCaregoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View
    private val logTag = "ProfileCategoryFragment"
    private lateinit var searchBookList: RecyclerView
    private lateinit var model: ProfileCategoryViewModel
    private var id: String? = null
    private var type: String = ""
    private lateinit var bookAdapter: CategoryBookAdapter
    private var name: String = "Books"
    private var img: String = ""

    companion object {
        @SuppressLint("StaticFieldLeak")
        val instance = ProfileCategoryFragment()
        fun newInstance(
            type: String,
            id: String,
            name: String = "",
            img: String = ""
        ): ProfileCategoryFragment {
            var args = Bundle()
            args.putString("type", type)
            args.putString("id", id)
            args.putString("name", name)
            args.putString("img", img)
            instance.arguments = args
            return instance
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        handleArgs()
        _binding = FragmentProfileCaregoryBinding.inflate(inflater, container, false)
        mainView = binding.root
        model = ViewModelProvider(requireActivity())[ProfileCategoryViewModel::class.java]
        model.setType(type)
        binding.profileBookList.isNestedScrollingEnabled = false
        model.isfetched = false
        setHasOptionsMenu(true)
        setupToolBar()
        fetchBooks()
        setDetails()
        return mainView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecycleViewLayoutHeight()
    }

    private fun setupScrollListener() {

        if (binding.nestedScrollView.scrollChangedListener != null) {
            Log.d(logTag, "Listener set!")
            return
        }

            binding.nestedScrollView.scrollChangedListener =
            { scrollView: CustomNestedScrollView, x: Int, y: Int, oldx: Int, oldy: Int ->
                if (!scrollView.canScrollVertically(1)) {
                    // bottom of scroll view
                    binding.profileBookList.isNestedScrollingEnabled = true
                }
                if (!scrollView.canScrollVertically(-1)) {
                    // top of scroll view
                    binding.profileBookList.isNestedScrollingEnabled = false
                }
            }
        binding.nestedScrollView.fullScroll(View.FOCUS_UP)
        binding.nestedScrollView.scrollTo(0, 0)
    }

//    override fun onPause() {
//        super.onPause()
//        mainview.nestedScrollView.scrollChangedListener = null
//        Log.d(logTag, "scrollChangedListener is null")
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.nestedScrollView.scrollChangedListener = null
        Log.d(logTag, "scrollChangedListener is null")
    }

    private fun setRecycleViewLayoutHeight() {
        activity?.displayMetrics()?.run {
            val height = heightPixels
            val toolBar = (activity as MainActivity)?.toolBar
            binding.recycleViewLayout.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                height - toolBar?.layoutParams?.height!!
            )
        }
    }

    private fun Activity.displayMetrics(): DisplayMetrics {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics
    }

    fun setDetails() {
        Log.d(logTag, "img is ${img}")

        Glide.with(this)
            .load(img)
            .fitCenter()
            .placeholder(R.drawable.placeholder_user)
            .into(binding.profileProImg)

        binding.profileName.text = name
    }

    fun handleArgs() {
        arguments?.let {
            type = it.getString("type", "")
            Log.d(logTag, "type : " + type)
            id = it.getString("id", null)
            name = it.getString("name", "Books")
            img = it.getString("img", "")
        }


    }

    private fun setupToolBar() {


        val toolBar = (activity as MainActivity).toolBar

        (activity as AppCompatActivity).setSupportActionBar(toolBar)

        val actionbar: ActionBar? = (activity as AppCompatActivity).supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }
        var title = type
        id?.let {
            title = name
        }
        toolBar.title = title
        actionbar?.show()


    }

    private fun fetchBooks() {
        id?.let {
            binding.loadingView.visibility = View.VISIBLE
            model.getProfilesBooks(it)
        }
    }

    fun observeBooks() {

        model.getBooks().observe(this, Observer<Books> { books ->
            binding.loadingView.visibility = View.GONE
            initBookList(books)
            setupScrollListener()
        })

        model.getException().observe(this, Observer<NetException> { e ->

            e?.let {
                it.printStackTrace()
                if (!isErrorHandled(it)) {
                    showErrorSnackBar(binding.profileBookList, "Something went wrong")
                }
            }
        })

    }

    @SuppressLint("NotifyDataSetChanged")
    fun initBookList(books: Books) {


        if (books.data.isEmpty()) {
            binding.noDataView.visibility = View.VISIBLE
            return
        } else {
            binding.noDataView.visibility = View.GONE
            binding.loadingView.visibility = View.GONE

        }

        if (model.isfetched) {
            bookAdapter.bookList = books.data
            bookAdapter.notifyDataSetChanged()
            return
        }

        model.isfetched = true

        val category = Category(null, null, null, books)

        bookAdapter = CategoryBookAdapter(
            CategoryBookAdapter.CategoryTypes.IMAGE_ONLY,
            category,
            false
        )
        bookAdapter.onClick = { index, book, view ->
            FragmentNavigation.getInstance()
                .startBookProfile(requireFragmentManager(), R.id.fragment_view, book)

        }
        bookAdapter.endPosition = { nextPageUrl, categoryId ->
            model.getNextPage()
        }

        binding.profileBookList.apply {
            layoutManager = GridLayoutManager(
                context,
                PixelConvert(requireActivity()).bookInRow(resources.getDimension(R.dimen.book_width))
            )
            adapter = bookAdapter
        }


    }

    private fun observeException() {
        model.getException().observe(this, Observer<NetException> {

            it?.let {
                if (isErrorHandled(it)) {
                    return@Observer
                }

                var msg = "Something went wrong"
                it.message?.let {
                    msg = it
                }
                showErrorSnackBar(binding.profileBookList, msg)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        observeBooks()
        observeException()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        setOptionMenuVisibility(
            menu,
            false,
            false,
            false,
            false,
            false
        )

    }

    override fun onRefresh() {
        Log.d(logTag, "inside pro cata onRefresh")
        setupToolBar()
    }

}
