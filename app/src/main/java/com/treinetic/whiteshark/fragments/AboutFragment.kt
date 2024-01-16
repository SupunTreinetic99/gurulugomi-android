package com.treinetic.whiteshark.fragments


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.treinetic.whiteshark.BuildConfig
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.activity.MainActivity
import android.content.Intent
import android.net.Uri
import com.treinetic.whiteshark.databinding.ActivityTermsAndPrivacyBinding
import com.treinetic.whiteshark.databinding.FragmentAboutBinding
import com.treinetic.whiteshark.glide.GlideApp


class AboutFragment : BaseFragment() {

    private var _binding : FragmentAboutBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View

    companion object {
        fun newInstance(): AboutFragment {
            return AboutFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainView = inflater.inflate(R.layout.fragment_about, container, false)
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        mainView = binding.root
        binding.treineticWeb.setOnClickListener { openWeb() }
        binding.trLogo.setOnClickListener { openWeb() }
        binding.phone1.setOnClickListener { openCall(binding.phone1.text.toString()) }
        binding.phone2.setOnClickListener { openCall(binding.phone2.text.toString()) }
        binding.socialIcon.setOnClickListener { openFacebookPage() }
        setHasOptionsMenu(true)

        setupToolBar()
        setData()
        return mainView
    }

    private fun openFacebookPage() {
        val url = "https://www.facebook.com/mdgunasena.lk/"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)

    }

    @SuppressLint("SetTextI18n")
    private fun setData() {
        val versionName = BuildConfig.VERSION_NAME
        binding.version.text = "Version $versionName"

        GlideApp.with(binding.image)
            .load(R.drawable.about_page_bkg)
            .placeholder(R.color.black)
            .into(binding.image)
    }

    private fun setupToolBar() {

        val toolBar = (activity as MainActivity).toolBar
        toolBar?.title = "About"

        (activity as AppCompatActivity).setSupportActionBar(toolBar)

        val actionbar: ActionBar? = (activity as AppCompatActivity).supportActionBar

        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }

    }

    private fun openWeb() {
        val url = "https://treinetic.com/"
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
    }

    private fun openCall(phone: String) {
//        val phone = resources.getString(R.string.become_author_contact)
        val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))
        startActivity(intent)
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
