package com.treinetic.whiteshark.adapters

import android.opengl.Visibility
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.databinding.PromotionCardBinding
import com.treinetic.whiteshark.models.Promotion

class PromotionAdapter(val promotions: List<Promotion>) :
    RecyclerView.Adapter<PromotionAdapter.PromotionViewHolder>() {

    var onClick: (
        (position: Int, promotion: Promotion) -> Unit)? = null
    var onClickPlay: ((url: String?) -> Unit)? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): PromotionViewHolder {
        return PromotionViewHolder(
            PromotionCardBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            ),
            this
        )
    }

    override fun getItemCount() = promotions.size

    override fun onBindViewHolder(viewHolder: PromotionViewHolder, position: Int) {
        val promotion = promotions[position]
        viewHolder.promotion = promotion
        Glide.with(viewHolder.root.context)
            .load(promotion.image.getExtraLargeImage())
            .placeholder(R.drawable.book_placeholder)
            .centerCrop()
            .into(viewHolder.image)

        if (promotion.videoLink == null) {
            viewHolder.playButton.visibility = View.GONE
        }
    }

    class PromotionViewHolder(val binding: PromotionCardBinding, val adapter: PromotionAdapter) :
        RecyclerView.ViewHolder(binding.root) {

        var image = binding.image
        var overalay = binding.overLay
        var playButton = binding.playButton
        var container = binding.btnContainer
        lateinit var promotion: Promotion
        var root = binding.root


        init {
            playButton.bringToFront()
            container.bringToFront()
            overalay.bringToFront()
            playButton.setOnClickListener {
                Log.i("PromotionAdapter", "click work")
                adapter.onClickPlay?.let {
                    it(promotion.videoLink)
                }
            }
            image.setOnClickListener { v ->
                adapter.onClick?.let {
                    it(
                        adapterPosition,
                        adapter.promotions[adapterPosition]
                    )
                }
            }


        }
    }
}