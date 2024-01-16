package com.treinetic.whiteshark.fragments.publishers


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
import com.treinetic.whiteshark.adapters.PublishersAdapter
import com.treinetic.whiteshark.constance.Screens
import com.treinetic.whiteshark.constance.SearchTypes
import com.treinetic.whiteshark.databinding.FragmentPublishersBinding
import com.treinetic.whiteshark.fragments.BaseFragment
import com.treinetic.whiteshark.fragments.publisherbooks.PublisherBookViewModel
import com.treinetic.whiteshark.models.Publisher

class PublishersFragment : BaseFragment() {

    private lateinit var actionMenu: Menu
    private var _binding : FragmentPublishersBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View
    private lateinit var model: PublishersViewModel

    private val TAG: String = "PublishersFragment"
    private lateinit var publisherAdapter: PublishersAdapter
    private lateinit var searchPublisherList: RecyclerView

    companion object {
        fun newInstance(): PublishersFragment {
            return PublishersFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPublishersBinding.inflate(inflater, container, false)
        mainView = binding.root
        binding.publishers.showLoading()
        model = ViewModelProvider(requireActivity())[PublishersViewModel::class.java]
        model.fetchPublisherList(false)
        model.isFetch = false
        showException()
        setHasOptionsMenu(true)
        loadPublishers()
        return mainView
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

    private fun loadPublishers() {
        model.publisherList.observe(viewLifecycleOwner) { publisher ->
            binding.publishers.refreshLayout.isRefreshing = false
            binding.publishers.hideLoading()
            initPublisherList(publisher)
        }
    }

    private fun showException() {
        model.netException.observe(viewLifecycleOwner, Observer { t ->

            t?.let {
                binding.publishers.refreshLayout.isRefreshing = false
                if (isErrorHandled(it)) {
                } else {
                    t.message?.let {
                        showErrorSnackBar(mainView, getString(R.string.error_msg))
                    }
                }
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initPublisherList(publisherList: MutableList<Publisher>) {
        if (publisherList.isEmpty()) {
            binding.publishers.showNoDataView()
            return
        } else {
            binding.publishers.hideLoading()
        }
        if (model.isFetch) {
            publisherAdapter.notifyDataSetChanged()
            return
        }


        binding.publishers.isEnableSwipeRefresh = true
        binding.publishers.onRefreshCallback = {

            model.isFetch = false
            model.fetchPublisherList(true)
        }

        model.isFetch = true
        publisherAdapter = PublishersAdapter(publisherList)
        publisherAdapter.onClick = { position, publisher ->
            Log.d(TAG, "img is publisher ${publisher.image?.medium}")

            val img = publisher.image?.getLargeImage() ?: ""

            FragmentNavigation.getInstance()
                .startProfileCategoryBook(
                    requireParentFragment().requireFragmentManager(), R.id.fragment_view,
                    Screens.PUBLISHER, publisher.id!!, publisher.name!!, img
                )

        }
        publisherAdapter.loadMore = { publisher ->

            model.currentPageUrl?.let {
                model.loadMore(it)
            }
        }
        searchPublisherList = binding.publishers.recyclerView.apply {
            layoutManager = LinearLayoutManager(
                context,
                RecyclerView.VERTICAL,
                false
            )
            adapter = publisherAdapter
        }

    }
}
