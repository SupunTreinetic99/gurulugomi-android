package com.treinetic.whiteshark.fragments.allreviews


import android.annotation.SuppressLint
import android.os.Bundle
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
import com.treinetic.whiteshark.activity.MainActivity
import com.treinetic.whiteshark.adapters.ReviewAdapter
import com.treinetic.whiteshark.databinding.FragmentAllReviewsBinding
import com.treinetic.whiteshark.fragments.BaseFragment
import com.treinetic.whiteshark.models.AllRatings


class AllReviewsFragment : BaseFragment() {

    private var _binding : FragmentAllReviewsBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View
    private lateinit var model: AllReviewsModelView
    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var bookId: String

    companion object {
        fun getInstance(bookId: String): AllReviewsFragment {
            val instance = AllReviewsFragment()
            val args = Bundle()
            args.putString("bookId", bookId)
            instance.arguments = args
            return instance
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAllReviewsBinding.inflate(inflater, container, false)
        mainView = binding.root
        model = ViewModelProvider(requireActivity())[AllReviewsModelView::class.java]
        model.isFetch = false
        requireArguments().getString("bookId")?.let {
            bookId = it
            model.fetchReviews(it)
        }
        binding.reviews.showLoading()
        fetchReviews()
        showException()
        setHasOptionsMenu(true)
        setupToolBar()

        return mainView
    }

    private fun fetchReviews() {
        model.getReviews().observe(viewLifecycleOwner, Observer { reviews ->
            binding.reviews.hideLoading()
            initReviewList(reviews)
        })
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun initReviewList(reviews: AllRatings) {

        if (reviews.data.isEmpty()) {
            binding.reviews.showNoDataView()
            return
        } else {
            binding.reviews.hideLoading()
        }

        if (model.isFetch) {
            reviewAdapter.notifyDataSetChanged()
            return
        }
        model.isFetch = true
        reviewAdapter = ReviewAdapter(reviews.data)
        reviewAdapter.loadMore = { position ->
            reviews.nextPageUrl?.let {
                model.loadMore(it, bookId)
            }
        }


        binding.reviews.recyclerView.apply {
            layoutManager = LinearLayoutManager(
                context, RecyclerView.VERTICAL, false
            )
            adapter = reviewAdapter
        }
    }


    private fun setupToolBar() {
        val toolBar = (activity as MainActivity).toolBar
        toolBar.title = resources.getString(R.string.all_reviews)

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
            menu, false, false,
            false, false, false
        )

    }

    private fun showException() {
        model.getNetException().observe(viewLifecycleOwner, Observer { t ->
            t?.let {
                if (isErrorHandled(it)) {
                } else {
                    t.message?.let {
                        val msg = getString(R.string.error_msg)
                        showErrorSnackBar(mainView, msg)
                    }

                }
            }
        })

    }

}
