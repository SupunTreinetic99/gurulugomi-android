package com.treinetic.whiteshark.fragments.search


import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mancj.materialsearchbar.MaterialSearchBar
import com.treinetic.whiteshark.FragmentNavigation
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.activity.MainActivity
import com.treinetic.whiteshark.adapters.SectionAdapter
import com.treinetic.whiteshark.constance.Screens
import com.treinetic.whiteshark.constance.SearchTypes
import com.treinetic.whiteshark.databinding.FragmentSearchBinding
import com.treinetic.whiteshark.fragments.BaseFragment
import com.treinetic.whiteshark.fragments.FragmentRefreshable
import com.treinetic.whiteshark.models.Author
import com.treinetic.whiteshark.models.Book
import com.treinetic.whiteshark.models.Publisher
import com.treinetic.whiteshark.models.SearchResult
import com.treinetic.whiteshark.services.SearchService
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import java.util.*


class SearchFragment : BaseFragment(), TextWatcher,
    MaterialSearchBar.OnSearchActionListener, FragmentRefreshable {

    private val logTag: String = "SearchFragment"

    //    private lateinit var searchBookList: RecyclerView
    private var _binding : FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View
    private lateinit var sectionRecyclerAdapter: SectionedRecyclerViewAdapter
    private lateinit var search: MaterialSearchBar
    private lateinit var model: SearchViewModel

    //    private var handler: Handler = Handler()
    var clearSearch = false

    companion object {
        @SuppressLint("StaticFieldLeak")
        private val instance = SearchFragment()

        fun newInstance(): SearchFragment {
            return instance
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        model = ViewModelProvider(requireActivity())[SearchViewModel::class.java]
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        mainView = binding.root
        binding.searchRecyclerView.hideLoading()
        binding.searchBar.text = ""
        binding.searchBar.searchEditText.isCursorVisible = true

        Log.d(logTag, "onCreateView called ->")
        clearSearch = false
        onClickSearch()
        setupToolBar()
        return mainView
    }

    override fun onStart() {
        super.onStart()
        Log.d(logTag, "setupData called -> ")
        setupData()
    }

    override fun onDestroy() {
        super.onDestroy()
        SearchService.getInstance().clearSearchData()
        Log.d(logTag, "onDestroy called ->")


    }


    private fun onClickSearch() {
        search = binding.searchBar
        search.text = ""
        search.addTextChangeListener(this)

        search.setOnSearchActionListener(this)

    }

    override fun onButtonClicked(buttonCode: Int) {
        when (buttonCode) {
            MaterialSearchBar.BUTTON_BACK -> {
                requireActivity().onBackPressed()
            }
        }
    }

    override fun onSearchStateChanged(enabled: Boolean) {
        Log.e(logTag, "onSearchStateChanged $enabled")
        if (!enabled) requireActivity().onBackPressed()
    }

    override fun onSearchConfirmed(text: CharSequence?) {
        Log.d(logTag, "onSearchConfirmed $text")
    }

    private var timer: Timer? = null

    override fun afterTextChanged(editable: Editable?) {
        try {
            timer = Timer()
            timer?.schedule(object : TimerTask() {
                override fun run() {
                    performSearch()
                }
            }, 800)
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }


    override fun beforeTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
        timer?.cancel()
        timer = null
    }

    fun performSearch() {
        if (!isAdded) return
        Handler(Looper.getMainLooper()).post {
            if (model.previousSearchTerm != search.text.toString() && search.text.trim()
                    .isNotEmpty()
            ) {
                Log.d(logTag, "Performing search -> ${search.text}")
//                mainView.search_recycler_view.showLoading()
                binding.searchRecyclerView.showLoadingWithoutAnimation()
                model.search(search.text.toString())
            }

            if (search.text.trim().isEmpty()) {
                model.previousSearchTerm = ""
                Log.d(logTag, "NOT Performing search -> ${search.text}")
//                mainView.search_recycler_view.hideLoading()
                SearchService.getInstance().clearSearchData()
                model.resetSearchData()
                binding.searchRecyclerView.showNoDataView()

            }
        }

    }

    private fun hideKeyboard(v: View) {
        val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        inputMethodManager!!.hideSoftInputFromWindow(v.applicationWindowToken, 0)
    }


    private fun setupData() {
        model.searchResult.observe(this, Observer {
            hideKeyboard(mainView)
            Log.d(logTag, "Search observer calling")
            it?.let {
                Log.d(logTag, "addSectionHeader calling")
                addSectionHeader(it)
            }

        })

        model.error.observe(this, Observer {
            hideKeyboard(mainView)
            Log.d(logTag, "error observer calling")
            it?.let {
                binding.searchRecyclerView.showNoDataView()
//                model.error = MutableLiveData()
                model.clearError()

            }

        })
    }


    private fun addSectionHeader(res: SearchResult) {
        if (!res.hasSearResults()) {
            binding.searchRecyclerView.showNoDataView()
            Log.d(logTag, "addSectionHeader no data")
            return
        }
        binding.searchRecyclerView.hideLoadingWithoutAnimation()
        Log.d(logTag, "addSectionHeader preparing data")
        sectionRecyclerAdapter = SectionedRecyclerViewAdapter().apply {
            for (str in SearchTypes.typeArray) {
                when (str) {
                    SearchTypes.SEARCH_BOOKS -> {
                        if (res.hasEpubs()) {
                            addBooksSection(str, this, res.epubs.data)
                        }
                    }
                    SearchTypes.SEARCH_PROMOSTION -> {
                        if (res.hasPromotions()) {
                            addBooksSection(str, this, res.promotions.data)
                        }
                    }
                    SearchTypes.SEARCH_AUTHORS -> {
                        if (res.hasAuthors()) {
                            addAuthorsSection(str, this, res.authors.data)
                        }
                    }
                    SearchTypes.SEARCH_PUBLISHERS -> {
                        if (res.hasPublishers()) {
                            addPublishersSection(str, this, res.publishers.data)
                        }
                    }

                }
            }
        }
            binding.searchRecyclerView.recyclerView.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = sectionRecyclerAdapter
        }


    }

    private fun addAuthorsSection(
        title: String,
        adapter: SectionedRecyclerViewAdapter,
        authorsList: List<Author>
    ) {
        if (authorsList.isNullOrEmpty()) return
        val sectionAdapter =
            SectionAdapter(setSection(title), title, authorsList, adapter)
        sectionAdapter.headerClick = { type ->
            FragmentNavigation.getInstance()
                .startSearchResult(requireFragmentManager(), R.id.fragment_view, type)
        }

        sectionAdapter.onClick = { position, parseList ->

            val author = parseList[position] as Author

            author.user_id.let {

                val img = author.user.image?.getLargeImage() ?: ""

                FragmentNavigation.getInstance()
                    .startProfileCategoryBook(
                        requireFragmentManager(),
                        R.id.fragment_view,
                        Screens.AUTHORS,
                        author.user_id,
                        author.user.name,
                        img
                    )
//                FragmentNavigation.getInstance()
//                    .startAuthorBook(
//                        requireFragmentManager(),
//                        R.id.fragment_view,
//                        SearchTypes.SEARCH_AUTHOR_BOOKS,
//                        it,
//                        author.user.name
//                    )
            }

        }

        adapter.addSection(sectionAdapter)
    }

    private fun addPublishersSection(
        title: String,
        adapter: SectionedRecyclerViewAdapter,
        publisherList: List<Publisher>
    ) {
        if (publisherList.isNullOrEmpty()) return
        val sectionAdapter =
            SectionAdapter(setSection(title), title, publisherList, adapter)
        sectionAdapter.headerClick = { type ->
            FragmentNavigation.getInstance()
                .startSearchResult(requireFragmentManager(), R.id.fragment_view, type)
        }

        sectionAdapter.onClick = { position, parseList ->

            val publisher = parseList[position] as Publisher

            publisher.id.let {

                val img = publisher.image?.getLargeImage() ?: ""

                FragmentNavigation.getInstance()
                    .startProfileCategoryBook(
                        requireFragmentManager(),
                        R.id.fragment_view,
                        Screens.PUBLISHER,
                        publisher.id!!,
                        publisher.name!!,
                        img
                    )


//                FragmentNavigation.getInstance()
//                    .startPublisherBook(
//                        requireFragmentManager(),
//                        R.id.fragment_view,
//                        SearchTypes.SEARCH_AUTHOR_BOOKS,
//                        it!!,
//                        publisher.name!!
//                    )
            }

        }

        adapter.addSection(sectionAdapter)
    }

    private fun addBooksSection(
        title: String,
        adapter: SectionedRecyclerViewAdapter,
        bookList: List<Book>
    ) {
        if (bookList.isNullOrEmpty()) return
        Log.d(logTag, "addBooksSection title : $title")
        val sectionAdapter =
            SectionAdapter(setSection(title), title, bookList, adapter)
        sectionAdapter.headerClick = { type ->
            FragmentNavigation.getInstance()
                .startSearchResult(requireFragmentManager(), R.id.fragment_view, type)
        }

        sectionAdapter.onClick = { position, parseList ->

            FragmentNavigation.getInstance().startBookProfile(
                requireFragmentManager(),
                R.id.fragment_view,
                parseList[position] as Book
            )
        }

        adapter.addSection(sectionAdapter)
    }


//    private fun getAdapterPosition(position: Int): Int {
//        return sectionRecyclerAdapter.getPositionInSection(position)
//    }

    private fun setSection(type: String): SectionParameters {

        val sectionParameters = SectionParameters.builder()

        if (type == SearchTypes.SEARCH_AUTHORS || type == SearchTypes.SEARCH_PUBLISHERS) {
            sectionParameters.itemResourceId(R.layout.search_author_result)
                .headerResourceId(R.layout.section_header)
                .footerResourceId(R.layout.sepration_footer)

            return sectionParameters.build()

        }

        return sectionParameters.itemResourceId(R.layout.search_result_card)
            .headerResourceId(R.layout.section_header)
            .footerResourceId(R.layout.sepration_footer)
            .build()
    }

//    private fun setSectionList(type: String): List<Book> {
//        val bookList: List<Book> = listOf()
////        when (type) {
//////            SEARCH_BOOKS -> list = BookService().getPurchasedBookList()
//////            SEARCH_PROMOSTION -> list = BookService().getPurchasedBookList()
////        }
//        return bookList
//    }

//    private fun setAuthorSection(): List<Author> {
//        return BookService().getAuthorList()
//    }

    private fun setupToolBar() {
        val toolBar = (activity as MainActivity).toolBar
        (activity as AppCompatActivity).setSupportActionBar(toolBar)
        val actionbar: ActionBar? = (activity as AppCompatActivity).supportActionBar
        actionbar?.hide()
    }

    override fun onResume() {
        super.onResume()
        binding.searchBar.post {
            binding.searchBar.performClick()
        }
        Log.d(logTag, "onResume called -> ")
    }

    override fun onPause() {
        super.onPause()
        Log.d(logTag, "onPause called -> ")
        Log.e(logTag, "clear search -> $clearSearch")
        if (clearSearch) search.text = ""
        if (view != null && clearSearch) {
            Log.d(logTag, "remove observer called -> ")
            model.searchResult.removeObservers(this)
        }


    }

    override fun onRefresh() {
        Log.d(logTag, "inside search onRefresh")
        setupToolBar()
    }
}
