package com.treinetic.whiteshark.fragments


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.activity.ImagePreviewActivity
import com.treinetic.whiteshark.activity.MainActivity
import com.treinetic.whiteshark.databinding.FragmentBookPublishBinding
import com.treinetic.whiteshark.databinding.FragmentEventDetailsBinding
import com.treinetic.whiteshark.models.Event
import com.treinetic.whiteshark.notification.NotificationManager
import com.treinetic.whiteshark.services.EventService
import com.treinetic.whiteshark.util.DateFormatUtil

class EventDetailsFragment : BaseFragment() {

    private var _binding : FragmentEventDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View

    companion object {
        fun newInstance(eventId: String?, fromNotification: Boolean): EventDetailsFragment {
            val fragment = EventDetailsFragment()
            val bundle = Bundle()
            bundle.putString("id", eventId)
            bundle.putBoolean("fromNotification", fromNotification)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEventDetailsBinding.inflate(inflater, container, false)
        mainView = binding.root
        setHasOptionsMenu(true)
        setupToolBar()
        arguments?.let {
            val e = if (!it.getBoolean("fromNotification", false)) {
                EventService.getInstance().getEvent(arguments?.getString("id"))
            } else {
                NotificationManager.instance.event
            }
            binding.evenvt = e
            setDate(e)
        }

        return mainView
    }

    private fun setDate(event: Event?) {

        if(!event?.eventDate.isNullOrBlank() && !event?.endDate.isNullOrBlank()){
            event?.eventDate?.let {
                val dateFormat = DateFormatUtil.getInstance().getFormattedDate(it)
                binding.date.text = dateFormat.date.toString()
                binding.year.text = dateFormat.year.toString()
                binding.month.text = dateFormat.month
            }

            event?.endDate?.let {
                val dateFormat = DateFormatUtil.getInstance().getFormattedDate(it)
                binding.endDate.text = dateFormat.date.toString()
                binding.endYear.text = dateFormat.year.toString()
                binding.endMonth.text = dateFormat.month
            }
            binding.container.visibility = View.VISIBLE
        }else{
            binding.container.visibility = View.GONE
        }

        if (event?.contactNumber.isNullOrBlank()) {
            binding.contactIcon.visibility = View.GONE
        }

        if (event?.endDate.isNullOrBlank()) {
            binding.endDateContainer.visibility = View.GONE
        }

        if (event?.contactNumber.isNullOrBlank() && event?.link.isNullOrBlank()) {
            binding.detailsContainer.visibility = View.GONE
            binding.separation.visibility = View.GONE
        }

        event?.images?.get(0)?.image?.let {
            Glide.with(requireContext()).load(it.getExtraLargeImage())
                .placeholder(R.drawable.book_placeholder)
                .into(binding.image)
            binding.image.setOnClickListener { view ->
                zoomImage(it.getExtraLargeImage())
            }
        }
        event?.customData?.let {
            if (it.isNotEmpty()) {
                binding.keyValueView.data = it
                return@let
            }
            binding.keyValueView.visibility = View.GONE
        }

        event?.let { e ->
            if (e.link.isNullOrBlank()) {
                binding.gotoWeb.visibility = View.GONE
                binding.webIcon.visibility = View.GONE
                return@let
            }
            clickGotoWeb(e)
        }

    }

    fun zoomImage(url: String?) {
        if (url.isNullOrBlank()) return
        var intent = Intent(requireActivity(), ImagePreviewActivity::class.java)
        intent.putExtra("url", url)
        requireActivity().startActivity(intent)
    }

    private fun clickGotoWeb(e: Event) {
        binding.gotoWeb.setOnClickListener {
            var webpage: Uri? = Uri.parse(e.link)
            if (!e.link!!.startsWith("http://") && !e.link.startsWith("https://")) {
                webpage = Uri.parse("http://" + e.link)
            }
            val intent = Intent(Intent.ACTION_VIEW, webpage)
            requireActivity().startActivity(Intent.createChooser(intent, "Open web link with"))
        }
    }


    private fun setupToolBar() {
        val toolBar = (activity as MainActivity).toolBar
        toolBar.title = ""
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
}
