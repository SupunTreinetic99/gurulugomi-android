package com.treinetic.whiteshark.fragments.authorbooks

import android.annotation.SuppressLint
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
import com.treinetic.whiteshark.FragmentNavigation
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.activity.MainActivity
import com.treinetic.whiteshark.adapters.SearchAdapter
import com.treinetic.whiteshark.constance.SearchTypes
import com.treinetic.whiteshark.databinding.FragmentAuthorsBinding
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.fragments.BaseFragment
import com.treinetic.whiteshark.models.Books

/**
 * Created by Nuwan on 2/22/1
 */
class AuthorBooksFragment : BaseFragment() {

    private val logTag = "SearchResultFrag"
    private var _binding : FragmentAuthorsBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View
    private lateinit var searchBookList: RecyclerView
    private lateinit var model: AuthorBookViewModel
    private var authorId: String? = null
    private lateinit var type: String
    private var searchAdapter: SearchAdapter? = null
    private var authorName: String = "Author Books"


    companion object {
        @SuppressLint("StaticFieldLeak")
        val instance = AuthorBooksFragment()
        fun newInstance(type: String, authorId: String, name: String = ""): AuthorBooksFragment {
            var args = Bundle()
            args.putString("type", type)
            args.putString("authorId", authorId)
            args.putString("authorName", name)
            instance.arguments = args
            return instance
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        observeBooks()
        observeException()
        handleArgs()
        _binding = FragmentAuthorsBinding.inflate(inflater, container, false)
        mainView = binding.root
        model = ViewModelProvider(requireActivity())[AuthorBookViewModel::class.java]
        setHasOptionsMenu(true)
        searchAdapter = null
        setupToolBar()
        fetchAutherBooks()

        return mainView
    }


    private fun handleArgs() {
        arguments?.let {
            type = it.getString("type", "")
            Log.d(logTag, "type : $type")
            authorId = it.getString("authorId", null)
            authorName = it.getString("authorName", "Author Books")
        }
    }

    private fun initBookList() {
        model.getBooks().value?.data?.let {

            if (it.isEmpty()) {
                binding.authors.showNoDataView()
                return
            }

            searchAdapter = SearchAdapter(
                it,
                SearchTypes.SEARCH_AUTHOR_BOOKS
            )
            searchAdapter?.onClickBook = { position, book, type ->
                FragmentNavigation.getInstance()
                    .startBookProfile(requireFragmentManager(), R.id.fragment_view, book)

            }

            searchAdapter?.fetchNextPage = { position, author, type ->
                model.getNextPage()
            }

            searchBookList = binding.authors.recyclerView.apply {
                layoutManager = LinearLayoutManager(
                    context,
                    RecyclerView.VERTICAL,
                    false
                )
                adapter = searchAdapter
            }
        }
    }


    private fun fetchAutherBooks() {
        authorId?.let {
            binding.authors.showLoading("Loading")
            model.getAuthorBooks(it)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeBooks() {
        model.getBooks().observe(viewLifecycleOwner, Observer<Books> {

            model.getBooks().value?.let { books: Books ->
                books?.let {
                    binding.authors.hideLoading()
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
        model.getException().observe(viewLifecycleOwner, Observer<NetException> {

            it?.let {
                if (isErrorHandled(it)) {
                    return@Observer
                }
                var msg = "Something went wrong"
                it.message?.let {
                    msg = it
                }
                showErrorSnackBar(binding.authors, msg)
            }
        })
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
        authorId?.let {
            title = authorName
        }
        toolBar.title = title
        actionbar?.show()
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