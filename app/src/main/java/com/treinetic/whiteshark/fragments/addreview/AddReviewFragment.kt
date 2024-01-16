package com.treinetic.whiteshark.fragments.addreview


import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.treinetic.whiteshark.R

import com.treinetic.whiteshark.activity.MainActivity
import com.treinetic.whiteshark.databinding.FragmentAddReviewBinding
import com.treinetic.whiteshark.databinding.FragmentPurchaseHistoryBinding
import com.treinetic.whiteshark.fragments.BaseFragment
import com.treinetic.whiteshark.fragments.bookprofile.BookProfileFragment
import com.treinetic.whiteshark.models.Book
import com.treinetic.whiteshark.models.Rating
import com.treinetic.whiteshark.services.BookService
import com.treinetic.whiteshark.services.Service
import com.treinetic.whiteshark.services.UserService


class AddReviewFragment : BaseFragment(), View.OnClickListener {

    private var _binding : FragmentAddReviewBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View
    private lateinit var bookId: String
    private lateinit var book: Book
    private lateinit var model: AddReviewViewModel

    companion object {
        fun newInstance(book: Book): AddReviewFragment {
            val instance = AddReviewFragment()
            instance.book = book
            return instance
        }

        var isAdd = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        model = ViewModelProvider(requireActivity())[AddReviewViewModel::class.java]
        observeData()
        setupData()
        _binding = FragmentAddReviewBinding.inflate(inflater, container, false)
        mainView = binding.root
        binding.ratingBar.rating = 0f
        binding.review.setText("")
        binding.submit.setOnClickListener(this)
        binding.edit.setOnClickListener(this)
        binding.edit.visibility = View.GONE
        getUserImage()
        setUserName()
        hideView(true, binding.reviewText)
        bookId = book.id
        getUserReview(bookId)
        setHasOptionsMenu(true)
        setupToolBar()
        hideLoading()
        return mainView
    }

    private fun setupData() {
        if (::book.isInitialized) {
            model.currentBook = book
        } else if (model.currentBook != null && !::book.isInitialized) {
            book = model.currentBook!!
        }


    }

    override fun onClick(view: View?) {
        when (view) {
            binding.submit -> {
                submitReview()
            }
            binding.edit -> {
                hideView(false, binding.review)
                binding.review.setText(binding.reviewText.text)
                hideView(true, binding.reviewText)
                hideView(false, binding.submit)
                hideView(true, binding.edit)
                binding.ratingBar.setIsIndicator(false)

            }
        }
    }

    private fun submitReview() {
        showLoading()
        saveUserReview(bookId, getRateValue(), getReview())
    }

    fun showLoading() {
        fadeIn(binding.LoadingLayer)
    }

    fun hideLoading() {
        if (binding.LoadingLayer.visibility == View.GONE) return
        fadeOut(view = binding.LoadingLayer, onAnimationEnd = {
            binding.LoadingLayer.visibility = View.GONE
        })
    }


    private fun getRateValue(): Double {
        return binding.ratingBar.rating.toDouble()
    }

    private fun getReview(): String {
        return binding.review.text.toString()
    }

    private fun setupToolBar() {
        val toolBar = (activity as MainActivity).toolBar
        toolBar.title = resources.getString(R.string.review)

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
            menu,
            false,
            false,
            false,
            false,
            false
        )

    }

    private fun saveReview(id: String, rating: Double, review: String) {
        BookService.getInstance()
            .saveReview(id, rating, review, { result ->

                book.ratingOfUser = result
                if (isAdd) {
                    showSuccessSnackBar(mainView, resources.getString(R.string.review_success))
                } else {
                    showSuccessSnackBar(
                        mainView,
                        resources.getString(R.string.review_edit_success)
                    )
                }

                hideView(true, binding.review)
                hideView(false, binding.reviewText)
                binding.reviewText.text = result.review
                hideView(true, binding.submit)
                hideView(false, binding.edit)
                binding.ratingBar.setIsIndicator(true)
                hideKeyboard()

                updateBook(result)
                if (!isAdd) {
                    activity?.onBackPressed()
                }


            },
                { exception ->
                    if (isErrorHandled(exception)) {
                        exception.message?.let {
                            showErrorSnackBar(mainView, it)
                        }
                    }
                })
    }


    private fun saveUserReview(id: String, rating: Double, review: String) {

        /*      val res = model.isValidReview(rating, review)
              if (!res.first) {
                  showErrorSnackBar(mainView.parentView, res.second)
                  return
              }*/
        val review = binding.review.text.toString()
        val rating = binding.ratingBar.rating.toDouble()
        if (review.trim().isBlank()) {
            showErrorSnackBar(mainView, "Please provide a valid review")
            hideLoading()
            return
        }

        if (rating <= 0) {
            showErrorSnackBar(mainView, "Please add a valid rating")
            hideLoading()
            return
        }

        model.saveReview(id, rating, review)

    }

    private fun observeData() {
        model.getRating().observe(viewLifecycleOwner, Observer { rating ->

            rating?.let {
                hideLoading()
                if (isAdd) {
                    showSuccessSnackBar(mainView, resources.getString(R.string.review_success))
                } else {
                    showSuccessSnackBar(mainView, resources.getString(R.string.review_edit_success))
                }

                rating?.let {
                    book.ratingOfUser = rating
                    hideView(true, binding.review)
                    hideView(false, binding.reviewText)
                    binding.reviewText.text = rating.review
                    hideView(true, binding.submit)
                    hideView(false, binding.edit)
                    binding.ratingBar.setIsIndicator(true)
                    hideKeyboard()
                    updateBook(rating)

                    if (!isAdd) {
                        activity?.onBackPressed()
                    }
                    model.getRating().value = null
                }
            }


        })

        model.getNetException().observe(viewLifecycleOwner, Observer { exception ->

            exception?.let {
                hideLoading()
                if (isErrorHandled(exception)) {
                    return@Observer
                }

                var msg = "Something went wrong"
//                exception?.message?.let {
//                    msg = it
//                }
                showMessageSnackBar(binding.parentView, msg)
                model.getNetException().value = null
            }


        })
    }

    private fun getUserImage() {
        val path = UserService.getInstance().getUser()?.image?.small
        Glide.with(binding.profileImage).load(path)
            .placeholder(R.drawable.placeholder_user)
            .into(binding.profileImage)
    }

    private fun setUserName() {
        UserService.getInstance().getUser()?.name.let {
            binding.userName.text = it
        }

    }

    private fun hideView(isHide: Boolean, view: View) {
        if (isHide) {
            view.visibility = View.GONE
        } else {
            view.visibility = View.VISIBLE
        }
    }

    private fun getUserReview(id: String) {
        val userRating = BookService.getInstance().getUserRating(id)
        userRating?.let {
            binding.ratingBar.rating = userRating.rating
            binding.review.setText(userRating.review)
        }
    }

    private fun hideKeyboard() {
        val imm = context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(mainView.windowToken, 0)
    }

    private fun updateBook(rate: Rating) {
        fragmentManager?.findFragmentByTag("book_profile")?.let {
            val fragment = it as BookProfileFragment
            fragment.getCurrentBook().ratingOfUser = rate
        }
        book.setIsMyReview()
        book.ratingOfUser = rate
        book.ratings.let {
            book.ratings = BookService.getInstance().addMyReviewToList(rate, it)
        }

        BookProfileFragment.initbook = book

    }


}
