package com.treinetic.whiteshark.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.PagerAdapter
import com.treinetic.whiteshark.FragmentNavigation
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.databinding.FragmentSearchBinding
import com.treinetic.whiteshark.databinding.ReviewViewBinding
import com.treinetic.whiteshark.dialog.MaterialDialogs
import com.treinetic.whiteshark.fragments.addreview.AddReviewFragment
import com.treinetic.whiteshark.fragments.bookprofile.BookProfileFragment
import com.treinetic.whiteshark.glide.GlideApp
import com.treinetic.whiteshark.models.Book
import com.treinetic.whiteshark.models.Rating


class ViewPagerAdapter(
    val context: Context,
    val book: Book,
    private val fragmentManager: FragmentManager
) :
    PagerAdapter(),
    View.OnClickListener {

    var callback: ((message: String) -> Unit)? = null
    var getPosition: ((position: Int) -> Unit)? = null
    private var _binding : ReviewViewBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainView: View

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun getCount(): Int {
        return book.ratings.count()
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        _binding = ReviewViewBinding.inflate(
            LayoutInflater.from(container.context),
            container,
            false
        )
        mainView = binding.root
//        val mainView = LayoutInflater.from(context).inflate(R.layout.review_view, container, false)
        binding.moreOption.setOnClickListener(this)

        if (!book.ratings[position].isMyReview) {
            binding.moreOption.visibility = View.GONE
        }
        container.addView(mainView)
        setReviewsData(book.ratings[position], mainView)
        return mainView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    private fun setReviewsData(ratingData: Rating, review: View) {
        GlideApp.with(binding.userImage)
            .load(ratingData.userCustomer?.user?.image?.medium)
            .placeholder(R.drawable.placeholder_user)
            .into(binding.userImage)

        binding.rateValue.text = ratingData.rating.toString()
        binding.rateComment.text = ratingData.review
        binding.rateName.text = ratingData.userCustomer?.user?.name
        binding.rateDate.text = ratingData.createdAt
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.moreOption -> {
                showPopup(view)
            }
        }
    }

    private fun showPopup(v: View) {
        val popup = PopupMenu(context, v)
        popup.setOnMenuItemClickListener(this::onMenuItemClick)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.review_popup, popup.menu)
        popup.show()
    }

    private fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.edit -> {
                AddReviewFragment.isAdd = false
                FragmentNavigation.getInstance()
                    .startAddReview(
                        fragmentManager,
                        R.id.fragment_view,
                        BookProfileFragment.initbook
                    )
                true
            }
            R.id.delete -> {
                MaterialDialogs().getConfirmDialog(
                    context,
                    context.resources.getString(R.string.delete_review),
                    context.getString(R.string.delete_review_messgae)
                ).show {
                    positiveButton(R.string.btn_delete) { dialog ->
                        callback?.let {
                            it("delete")
                        }
                    }
                    negativeButton(R.string.btn_cancel) { dialog ->
                    }
                }
                true
            }
            else -> false
        }
    }
}