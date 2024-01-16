package com.treinetic.whiteshark.fragments.event


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.treinetic.whiteshark.FragmentNavigation
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.activity.MainActivity
import com.treinetic.whiteshark.adapters.EventAdpater
import com.treinetic.whiteshark.databinding.FragmentDeviceListBinding
import com.treinetic.whiteshark.databinding.FragmentEventBinding
import com.treinetic.whiteshark.fragments.BaseFragment
import com.treinetic.whiteshark.models.Event


class EventFragment : BaseFragment() {
    private var _binding : FragmentEventBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View
    private lateinit var model: EventViewModel
    private lateinit var eventAdapter: EventAdpater
    private var logTag = "EventFragment"

    companion object {
        fun newInstance(): EventFragment {
            return EventFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentEventBinding.inflate(inflater, container, false)
        mainView = binding.root
        binding.events.showLoading()
        model = ViewModelProvider(requireActivity())[EventViewModel::class.java]
        model.fetchEventList(false)
        model.isFetch = false
        showException()
        setHasOptionsMenu(true)
        setupToolBar()
        loadEvents()
        return mainView
    }

    private fun loadEvents() {
        model.getEventList().observe(viewLifecycleOwner, Observer<MutableList<Event>> { events ->
            binding.events.refreshLayout.isRefreshing = false
            binding.events.hideLoading()
            initEventList(events)
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initEventList(eventList: MutableList<Event>) {
        if (eventList.isEmpty()) {
            binding.events.showNoDataView()
            return
        } else {
            binding.events.hideLoading()
        }
        if (model.isFetch) {
            eventAdapter.notifyDataSetChanged()
            return
        }


        binding.events.isEnableSwipeRefresh = true
        binding.events.onRefreshCallback = {
            //            it?.isRefreshing = false
            model.isFetch = false
            model.fetchEventList(true)
        }

        model.isFetch = true
        eventAdapter = EventAdpater(eventList)
        eventAdapter.onClick = { position, event ->
            FragmentNavigation()
                .startEventDetails(requireFragmentManager(), R.id.fragment_view, event.id)
        }
        eventAdapter.loadMore = { event ->

            model.getPageUrl()?.let {
                model.loadMore(it)
            }

        }
        binding.events.recyclerView.apply {
            layoutManager = LinearLayoutManager(
                context,
                RecyclerView.VERTICAL,
                false
            )
            adapter = eventAdapter
        }

    }

    private fun setupToolBar() {
        val toolBar = (activity as MainActivity).toolBar
        toolBar.title = resources.getString(R.string.events)
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
            menu, false, false, false,
            false, false
        )
    }

    private fun showException() {
        model.getNetException().observe(viewLifecycleOwner, Observer { t ->

            t?.let {
                binding.events.refreshLayout.isRefreshing = false
                if (isErrorHandled(it)) {
                } else {
                    t.message?.let {
                        showErrorSnackBar(mainView, getString(R.string.error_msg))
                    }
                }
            }
        })
    }
}
