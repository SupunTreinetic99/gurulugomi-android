package com.treinetic.whiteshark.fragments.searchresult


import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.treinetic.whiteshark.FragmentNavigation
import com.treinetic.whiteshark.activity.MainActivity
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.adapters.SearchAdapter
import com.treinetic.whiteshark.constance.Screens
import com.treinetic.whiteshark.constance.SearchTypes
import com.treinetic.whiteshark.databinding.FragmentSearchBinding
import com.treinetic.whiteshark.databinding.FragmentSearchResultBinding
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.fragments.BaseFragment
import com.treinetic.whiteshark.fragments.FragmentRefreshable
import com.treinetic.whiteshark.models.Authors
import com.treinetic.whiteshark.models.Books

import com.treinetic.whiteshark.services.SearchService


class SearchResultFragment : BaseFragment(), FragmentRefreshable {

    private val logTag = "SearchResultFrag"
    private var _binding : FragmentSearchResultBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View

    private lateinit var searchBookList: RecyclerView
    private lateinit var authorList: RecyclerView
    private lateinit var publisherList: RecyclerView
    private lateinit var type: String

    //    private var books: Books by lazy { SearchService.getInstance().getSearchSection(type) as Books }
    private lateinit var books: Books
    private val authors: Authors by lazy {
        SearchService.getInstance().getSearchSection(type) as Authors
    }
    private var searchAdapter: SearchAdapter? = null
    private lateinit var model: SearchResultViewModel
    private var authorId: String? = null


    companion object {
        @SuppressLint("StaticFieldLeak")
        val instance = SearchResultFragment()
        fun newInstance(type: String): SearchResultFragment {
            var f = SearchResultFragment()
            var args = Bundle()
            args.putString("type", type)
            f.arguments = args
            return f
        }

        fun newInstance(type: String, authorId: String, publisherId: String): SearchResultFragment {
            var f = SearchResultFragment()
            var args = Bundle()
            args.putString("type", type)
            args.putString("authorId", authorId)
            args.putString("publisherId", publisherId)
            f.arguments = args
            return f
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchResultBinding.inflate(inflater, container, false)
        mainView = binding.root
        model = ViewModelProviders.of(this).get(SearchResultViewModel::class.java)
        handleArgs()
        setHasOptionsMenu(true)

        val currentType = arguments?.getString("type")
//        initRecycler(currentType)
        searchAdapter = null
        observeAuthors()
        observePublishers()
        observeBooks()
        return mainView
    }

    fun handleArgs() {
        arguments?.let {
            model.type = it.getString("type", "")
            Log.d(logTag, "type : " + model.type)
            model.authorId = it.getString("authorId", null)
            model.publisherId = it.getString("publisherId", null)
//            model.loadSearchData()
//            initRecycler(model.type)
            binding.resultList.showLoading("Loading")
            model.getFirstPage(model.type!!)
        }

    }

    override fun onResume() {
        super.onResume()
        setupToolBar()
    }

    private fun initRecycler(type: String?) {
        Log.d(logTag, "Search type $type")
        when (type) {
            SearchTypes.SEARCH_AUTHORS -> {
                initSearchAuthor()
            }
            SearchTypes.SEARCH_PUBLISHERS -> {
                initSearchPublisher()
            }
            SearchTypes.SEARCH_BOOKS -> {
                initSearchBookList()
            }
            SearchTypes.SEARCH_PROMOSTION -> {
                initSearchBookList()
            }
            SearchTypes.SEARCH_AUTHOR_BOOKS -> {
                fetchAutherBooks()
                observeException()
            }
            SearchTypes.SEARCH_PUBLISHER_BOOKS -> {
                fetchPublisherBooks()
                observeException()
            }
        }
    }

    private fun setupToolBar() {


        var toolBar = (activity as MainActivity).toolBar

        (activity as AppCompatActivity).setSupportActionBar(toolBar)

        val actionbar: ActionBar? = (activity as AppCompatActivity).supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }
        toolBar.title = resources.getString(R.string.search_result)
        actionbar?.show()

    }

    private fun initSearchBookList() {
        model.getBooks().value ?: return
        Log.d(logTag, "initSearchBookList continiued")
        searchAdapter = SearchAdapter(
            model.getBooks().value!!.data,
            SearchTypes.SEARCH_BOOKS
        )
        searchAdapter?.onClickBook = { position, book, type ->

            FragmentNavigation()
                .startBookProfile(requireFragmentManager(), R.id.fragment_view, book)
        }

        searchAdapter?.fetchNextPage = { position, author, type ->
            model.getNextPage(model.getBooks().value?.next_page_url)
        }


        searchBookList = binding.resultList.recyclerView.apply {
            layoutManager = LinearLayoutManager(
                context,
                RecyclerView.VERTICAL,
                false
            )
            adapter = searchAdapter

        }
    }

    private fun initSearchAuthor() {

        model.authors.value?.data.let {
            if (it.isNullOrEmpty()) {
                binding.resultList.showNoDataView()
            }
        }

        model.authors.value ?: return
        searchAdapter = SearchAdapter(
            model.authors.value!!.data,
            SearchTypes.SEARCH_AUTHORS
        )
        searchAdapter?.onClickAuthor = { position, author, type ->
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

//            FragmentNavigation.getInstance()
//                .startAuthorBook(
//                    requireFragmentManager(), R.id.fragment_view,
//                    SearchTypes.SEARCH_AUTHOR_BOOKS, author.user_id, author.user.name
//                )

        }
        searchAdapter?.fetchNextPage = { position, item, type ->
            model.getAuthorNextPage(model.authors.value?.next_page_url)
        }

        authorList = binding.resultList.recyclerView.apply {
            layoutManager = LinearLayoutManager(
                context,
                RecyclerView.VERTICAL,
                false
            )
            adapter = searchAdapter
        }

    }

    private fun initSearchPublisher() {

        model.publishers.value?.data.let {
            if (it.isNullOrEmpty()) {
                binding.resultList.showNoDataView()
            }
        }

        model.publishers.value ?: return
        searchAdapter = SearchAdapter(
            model.publishers.value!!.data,
            SearchTypes.SEARCH_PUBLISHERS
        )
        searchAdapter?.onClickPublisher = { position, publisher, type ->

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


//            FragmentNavigation.getInstance()
//                .startPublisherBook(
//                    requireFragmentManager(), R.id.fragment_view,
//                    SearchTypes.SEARCH_PUBLISHER_BOOKS, publisher.id!!, publisher.name!!
//                )

        }
        searchAdapter?.fetchNextPage = { position, item, type ->
            model.getPublisherNextPage(model.publishers.value?.next_page_url)
        }

        publisherList = binding.resultList.recyclerView.apply {
            layoutManager = LinearLayoutManager(
                context,
                RecyclerView.VERTICAL,
                false
            )
            adapter = searchAdapter
        }

    }

    private fun fetchAutherBooks() {
        model.authorId?.let {
            model.getAuthorBooks(it)
        }
    }

    private fun fetchPublisherBooks() {
        model.publisherId?.let {
            model.getPublisherBooks(it)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun observeBooks() {
        model.getBooks().observe(viewLifecycleOwner, Observer<Books> { books ->
            Log.d(logTag, " Calling book observer")
            books?.let {
                binding.resultList.hideLoading()
                searchAdapter?.let {
                    it.notifyDataSetChanged()
                    Log.d(logTag, " Calling book observer notify adapter")
                    return@Observer
                }
                initRecycler(model.type)
            }

        })
    }

    @SuppressLint("NotifyDataSetChanged")
    fun observeAuthors() {
        model.authors.observe(viewLifecycleOwner, Observer { authors ->
            authors?.let {
                binding.resultList.hideLoading()
                searchAdapter?.let {
                    it.notifyDataSetChanged()
                    Log.d(logTag, " Calling book observer notify adapter")
                    return@Observer
                }
                initRecycler(model.type)
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    fun observePublishers() {
        model.publishers.observe(viewLifecycleOwner, Observer { publishers ->
            publishers?.let {
                binding.resultList.hideLoading()
                searchAdapter?.let {
                    it.notifyDataSetChanged()
                    Log.d(logTag, " Calling publisher observer notify adapter")
                    return@Observer
                }
                initRecycler(model.type)
            }
        })
    }


    private fun observeException() {
        model.getException().observe(viewLifecycleOwner, Observer<NetException> {

            it?.let {
                binding.resultList.hideLoading()
                if (isErrorHandled(it)) {
                    return@Observer
                }

                var msg = "Something went wrong"
                it.message?.let {
                    msg = it
                }
                showMessageSnackBar(binding.resultList, msg)
                model.clearError()
            }
        })
    }


    private fun initAutherBooks(books: Books) {
        searchAdapter = SearchAdapter(
            authors.data,
            SearchTypes.SEARCH_AUTHOR_BOOKS
        )
        searchAdapter?.onClickBook = { position, book, type ->

        }


        searchBookList = binding.resultList.recyclerView.apply {
            layoutManager = LinearLayoutManager(
                context,
                RecyclerView.VERTICAL,
                false
            )
            adapter = searchAdapter

        }


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
        Log.d(logTag, "inside search res onRefresh")
        setupToolBar()
    }


}
