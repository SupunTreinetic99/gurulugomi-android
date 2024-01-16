package com.treinetic.whiteshark.fragments.authors


import android.annotation.SuppressLint
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
import com.treinetic.whiteshark.adapters.AuthorsAdapter
import com.treinetic.whiteshark.constance.Screens
import com.treinetic.whiteshark.constance.SearchTypes
import com.treinetic.whiteshark.databinding.FragmentAuthorsBinding
import com.treinetic.whiteshark.fragments.BaseFragment
import com.treinetic.whiteshark.models.Author

class AuthorsFragment : BaseFragment(), View.OnClickListener {


    private lateinit var actionMenu: Menu
    private lateinit var model: AuthorsViewModel
    private val TAG: String = "AuthorsFragment"
    private lateinit var authorAdapter: AuthorsAdapter
    private var _binding : FragmentAuthorsBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View
    private lateinit var authorList: RecyclerView
    companion object {
        fun newInstance(): AuthorsFragment {
            return AuthorsFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAuthorsBinding.inflate(inflater, container, false)
        mainView = binding.root
        binding.authors.showLoading()
        model = ViewModelProvider(requireActivity())[AuthorsViewModel::class.java]
        model.fetchAuthorList(false)
        model.isFetch = false
        showException()
        setHasOptionsMenu(true)
        loadAuthors()
        return mainView
    }

    private fun loadAuthors() {
        model.authorList.observe(viewLifecycleOwner, Observer<MutableList<Author>> { authors ->
            binding.authors.refreshLayout.isRefreshing = false
            binding.authors.hideLoading()
            Log.d(TAG, "loadAuthors called")
            initAuthorList(authors)
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initAuthorList(authorList: MutableList<Author>) {
        if (authorList.isEmpty()) {
            binding.authors.showNoDataView()
            return
        } else {
            binding.authors.hideLoading()
        }
        if (model.isFetch) {
            Log.d(TAG, "model.isFetch true")
            authorAdapter.notifyDataSetChanged()
            return
        }

        binding.authors.isEnableSwipeRefresh = true
        binding.authors.onRefreshCallback = {

            model.isFetch = false
            model.fetchAuthorList(true)
        }

        model.isFetch = true
        authorAdapter = AuthorsAdapter(authorList)
        authorAdapter.onClick = { _, author ->

            Log.d(TAG, "img is author ${author.user.image?.medium}")
            val img = author.user.image?.getLargeImage() ?: ""
            FragmentNavigation.getInstance()
                .startProfileCategoryBook(
                    requireParentFragment().requireFragmentManager(), R.id.fragment_view,
                    Screens.AUTHORS, author.user_id, author.user.name, img
                )

        }
        authorAdapter.loadMore = {

            model.currentPageUrl?.let {
                Log.d(TAG, "loadMore called")
                model.loadMore(it)
            }

        }
        binding.authors.recyclerView.apply {
            layoutManager = LinearLayoutManager(
                context,
                RecyclerView.VERTICAL,
                false
            )
            adapter = authorAdapter
        }
    }

    private fun showException() {
        model.netException.observe(viewLifecycleOwner, Observer { t ->
            t?.let {
                binding.authors.refreshLayout.isRefreshing = false
                if (isErrorHandled(it)) {
                } else {
                    t.message?.let {
                        showErrorSnackBar(mainView, getString(R.string.error_msg))
                    }
                }
            }
        })
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


    override fun onClick(v: View?) {

    }
}
