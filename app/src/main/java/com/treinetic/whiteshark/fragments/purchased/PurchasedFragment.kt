package com.treinetic.whiteshark.fragments.purchased


import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.activity.MainActivity
import com.treinetic.whiteshark.adapters.PurchaseAdapter
import com.treinetic.whiteshark.databinding.FragmentPurchaseHistoryBinding
import com.treinetic.whiteshark.fragments.BaseFragment
import com.treinetic.whiteshark.models.Order
import whiteshark.treinetic.com.myapplication.fragments.purchased.PurchasedViewModel

class PurchasedFragment : BaseFragment() {

    private var _binding : FragmentPurchaseHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View
    private lateinit var model: PurchasedViewModel

    companion object {
        fun newInstance(): PurchasedFragment {
            return PurchasedFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentPurchaseHistoryBinding.inflate(inflater, container, false)
        mainView = binding.root
        model = ViewModelProvider(requireActivity())[PurchasedViewModel::class.java]
        binding.purchasedRecycler.showLoading()
        setHasOptionsMenu(true)
        setupToolBar()
        showException()
        model.loadPurchased()
        setPurchased()
        return mainView
    }

    private fun setPurchased() {
        model.getPurchased().observe(viewLifecycleOwner, Observer { t ->
            binding.purchasedRecycler.hideLoading()
            binding.purchasedRecycler.refreshLayout.isRefreshing = false
            showPurchasedHistory(orderList = t)
        })
    }

    private fun showException() {
        model.getNetException().observe(viewLifecycleOwner, Observer { netException ->
            netException?.let {
                binding.purchasedRecycler.refreshLayout.isRefreshing = false
                if (isErrorHandled(it)) {
                } else {
                    netException.message?.let {
                        showMessageSnackBar(mainView, getString(R.string.error_msg))
                    }
                }
                model.clearException()
            }
        })
    }

    private fun sectionParameters(): SectionParameters {
        val sectionParameters = SectionParameters.builder()
        sectionParameters.itemResourceId(R.layout.custom_book_cardview)
            .headerResourceId(R.layout.purchase_heder)
            .footerResourceId(R.layout.purchase_footer)
        return sectionParameters.build()
    }

    private fun showPurchasedHistory(orderList: MutableList<Order>) {
        val sectionAdapter = SectionedRecyclerViewAdapter().apply {

            if (orderList.isEmpty()) {
                binding.purchasedRecycler.showNoDataView()
                return
            } else {
                binding.purchasedRecycler.hideLoading()
            }
            for (order in orderList) {
                val adapter = PurchaseAdapter(sectionParameters(), order)
                addSection(adapter)
            }

            binding.purchasedRecycler.isEnableSwipeRefresh = true
            binding.purchasedRecycler.onRefreshCallback = {
                model.loadPurchased()

            }
        }
        binding.purchasedRecycler.recyclerView.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = sectionAdapter
        }

        binding.purchasedRecycler.refreshLayout.isEnabled = true
    }

    private fun setupToolBar() {
        val toolBar = (activity as MainActivity).toolBar
        toolBar.title = resources.getString(R.string.purchase_history)
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
            menu, false, false, false, false,
            false
        )
    }
}
