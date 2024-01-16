package com.treinetic.whiteshark.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.databinding.AllReviewCardBinding
import com.treinetic.whiteshark.models.Rating

class ReviewAdapter(private val reviewList: ArrayList<Rating>) :
    RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    var loadMore: ((position: Int) -> Unit)? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ReviewViewHolder {
        return ReviewViewHolder(
            AllReviewCardBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            ),
            this
        )
    }

    override fun getItemCount() = reviewList.size

    override fun onBindViewHolder(viewHolder: ReviewViewHolder, position: Int) {
        val rating = reviewList[position]
        Glide.with(viewHolder.itemView)
            .load(rating.userCustomer?.user?.image?.small)
            .placeholder(R.drawable.placeholder_user)
            .into(viewHolder.userImage)

        viewHolder.apply {
            comment.text = rating.review
            rateValue.text = rating.rating.toString()
            rate.rating = rating.rating
            userRateName.text = rating.userCustomer?.user?.name
            rateDate.text = rating.createdAt
        }

        if (position == reviewList.size - 3) {
            loadMore?.let {
                it(position)
            }
        }
    }

    class ReviewViewHolder(val binding: AllReviewCardBinding, adapter: ReviewAdapter) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {
        var userRateName = binding.rUserName
        var userImage = binding.rUserImage
        var comment = binding.rComment
        var rateValue = binding.rRatingValue
        var rate = binding.ratingBar
        var rateDate = binding.rDate

        override fun onClick(view: View?) {
            when (view) {

            }
        }
    }
}