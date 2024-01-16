package com.treinetic.whiteshark.fragments.promotions


import android.content.Intent
import android.net.Uri
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
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.activity.ImagePreviewActivity
import com.treinetic.whiteshark.activity.MainActivity
import com.treinetic.whiteshark.adapters.PromotionAdapter
import com.treinetic.whiteshark.databinding.FragmentProfileCaregoryBinding
import com.treinetic.whiteshark.databinding.FragmentPromotionBinding
import com.treinetic.whiteshark.fragments.BaseFragment
import com.treinetic.whiteshark.models.Promotion


class PromotionFragment : BaseFragment() {

    private val logTag: String = "PromotionFragment"
    private var _binding : FragmentPromotionBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View
    private lateinit var model: PromotionsViewModel

    companion object {
        fun newInstance(): PromotionFragment {
            return PromotionFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentPromotionBinding.inflate(inflater, container, false)
        mainView = binding.root
        model = ViewModelProvider(requireActivity())[PromotionsViewModel::class.java]
        getPromotions()
        showException()
        model.fetchPromotions()
        setHasOptionsMenu(true)
        setupToolBar()
        return mainView
    }


    private fun getPromotions() {
        model.getPromotions().observe(viewLifecycleOwner, Observer { promotions ->
            initPromotionList(promotions.data)
        })
    }

    private fun initPromotionList(promotionList: MutableList<Promotion>) {

        if (promotionList.isEmpty()) {
            binding.promotions.showNoDataView()
            return
        } else {
            binding.promotions.hideLoading()
        }

        val promotionAdapter = PromotionAdapter(promotionList)
        promotionAdapter.onClick = { position, promotion ->
            var intent=Intent(requireActivity(),ImagePreviewActivity::class.java)
            intent.putExtra("url",promotion.image.getExtraLargeImage())
            requireActivity().startActivity(intent)
        }

        promotionAdapter.onClickPlay = { url ->
            url?.let {

                try {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(it)
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.i(logTag, "Error occur in promotion when open video play url")
                }

            }
        }
        binding.promotions.recyclerView.apply {

            layoutManager = LinearLayoutManager(
                context,
                RecyclerView.VERTICAL,
                false
            )
            adapter = promotionAdapter
        }
    }

    private fun setupToolBar() {

        val toolBar = (activity as MainActivity).toolBar
        toolBar.title = "Promotions"

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
            menu, false,
            false,
            false,
            false,
            false
        )

    }

    private fun showException() {
        model.getNetException().observe(viewLifecycleOwner, Observer { t ->
            t?.let {
                if (isErrorHandled(it)) {
                } else {
                    t.message?.let {
                        showErrorSnackBar(mainView, it)
                    }

                }
            }
        })

    }


}
