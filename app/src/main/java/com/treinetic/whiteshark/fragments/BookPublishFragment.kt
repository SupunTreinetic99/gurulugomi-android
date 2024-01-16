package com.treinetic.whiteshark.fragments


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.treinetic.whiteshark.activity.MainActivity

import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.databinding.FragmentBillingHistoryBinding
import com.treinetic.whiteshark.databinding.FragmentBookPublishBinding
import com.treinetic.whiteshark.glide.GlideApp
import com.treinetic.whiteshark.services.UserService

class BookPublishFragment : Fragment() {

    private var _binding : FragmentBookPublishBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View

    companion object {

        fun newInstance(): BookPublishFragment {
            return BookPublishFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBookPublishBinding.inflate(inflater, container, false)
        mainView = binding.root

        setHasOptionsMenu(true)

        setupToolBar()
        setData()
        binding.email.setOnClickListener { openEmail() }
        binding.phone.setOnClickListener { openCall() }
        return mainView
    }

    private fun setupToolBar() {

        var toolBar = (activity as MainActivity)?.toolBar
        toolBar?.title = "Become an Author"

        (activity as AppCompatActivity).setSupportActionBar(toolBar)

        val actionbar: ActionBar? = (activity as AppCompatActivity).supportActionBar

        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }
    }

    fun setData() {
        GlideApp.with(binding.image)
            .load(R.drawable.become_author)
            .into(binding.image)
    }

    fun openEmail() {
        val toEmail = resources.getString(R.string.become_author_email)
        val username = if(!UserService.getInstance().initUser?.name.isNullOrBlank()) UserService.getInstance().initUser?.name.toString() else "[username]"
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$toEmail"))
        intent.putExtra(Intent.EXTRA_SUBJECT, "Become an Author Inquiry - $username")
        startActivity(Intent.createChooser(intent, "Send Email"))
    }

    fun openCall() {
        val phone = resources.getString(R.string.become_author_contact)
        val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))
        startActivity(intent)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_favorite)?.isVisible = false
        menu.findItem(R.id.action_search)?.isVisible = false
        menu.findItem(R.id.action_category)?.isVisible = false
        menu.findItem(R.id.action_cart)?.isVisible = false
        menu.findItem(R.id.action_delete)?.isVisible = false


    }


}
