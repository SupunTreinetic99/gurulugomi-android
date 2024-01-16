package com.treinetic.whiteshark.fragments.publisherbooks


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
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
import com.treinetic.whiteshark.FragmentNavigation
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.activity.MainActivity
import com.treinetic.whiteshark.adapters.SearchAdapter
import com.treinetic.whiteshark.constance.Screens
import com.treinetic.whiteshark.constance.SearchTypes
import com.treinetic.whiteshark.databinding.FragmentPublishersBinding
import com.treinetic.whiteshark.databinding.PaymentFragmentBinding
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.fragments.BaseFragment
import com.treinetic.whiteshark.models.Books


class PublisherBookFragment : BaseFragment() {

    private val logTag = "SearchResultFrag"
    private var _binding : FragmentPublishersBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View
    private lateinit var searchBookList: RecyclerView
    private lateinit var model: PublisherBookViewModel
    private var publisherId: String? = null
    private lateinit var type: String
    private var searchAdapter: SearchAdapter? = null
    private var publisherName: String = "Publisher Books"

    companion object {
        val instance = PublisherBookFragment()
        fun newInstance(
            type: String,
            publisherId: String,
            name: String = ""
        ): PublisherBookFragment {
            var args = Bundle()
            args.putString("type", type)
            args.putString("publisherId", publisherId)
            args.putString("publisherName", name)
            instance.arguments = args
            return instance
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        handleArgs()
        _binding = FragmentPublishersBinding.inflate(inflater, container, false)
        mainView = binding.root
        model = ViewModelProvider(requireActivity())[PublisherBookViewModel::class.java]
        setHasOptionsMenu(true)
        searchAdapter = null
        setupToolBar()
        fetchPublisherBooks()
        return mainView
    }

    fun handleArgs() {
        arguments?.let {
            type = it.getString("type", "")
            Log.d(logTag, "type : " + type)
            publisherId = it.getString("publisherId", null)
            publisherName = it.getString("publisherName", "Publisher Books")
        }


    }

    private fun setupToolBar() {
        val toolBar = (activity as MainActivity)?.toolBar

        (activity as AppCompatActivity).setSupportActionBar(toolBar)

        val actionbar: ActionBar? = (activity as AppCompatActivity).supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }
        var title = type
        publisherId?.let {
            title = publisherName
        }
        if (toolBar != null) {
            toolBar.title = title
        }
        actionbar?.show()


    }


    private fun fetchPublisherBooks() {
        publisherId?.let {
            binding.publishers.showLoading("Loading")
            model.getPublisherBooks(it)
        }
    }


    fun initBookList() {


        model.getBooks().value?.data?.let {

            if (it.isEmpty()) {
                binding.publishers.showNoDataView()
                return
            }

            searchAdapter = SearchAdapter(
                it,
                SearchTypes.SEARCH_PUBLISHER_BOOKS
            )
            searchAdapter?.onClickBook = { position, book, type ->
                FragmentNavigation.getInstance()
                    .startBookProfile(requireFragmentManager(), R.id.fragment_view, book)

            }

            searchAdapter?.fetchNextPage = { position, publisher, type ->
                model.getNextPage()
            }


            searchBookList = binding.publishers.recyclerView.apply {
                layoutManager = LinearLayoutManager(
                    context,
                    RecyclerView.VERTICAL,
                    false
                )
                adapter = searchAdapter

            }
        }

    }

    fun observeBooks() {
        model.getBooks().observe(this, Observer<Books> {

            model.getBooks().value?.let { books: Books ->
                books.let {
                    binding.publishers.hideLoading()
                }
                searchAdapter?.let {
                    it.list = books.data
                    it.notifyDataSetChanged()
                    return@Observer
                }

                initBookList()
            }

        })
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
                showErrorSnackBar(binding.publishers, msg)
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
}
