package com.treinetic.whiteshark.adapters

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.treinetic.whiteshark.glide.GlideApp
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.constance.SearchTypes
import com.treinetic.whiteshark.databinding.CustomPublisherAuthorCardviewBinding
import com.treinetic.whiteshark.databinding.SearchAuthorResultBinding
import com.treinetic.whiteshark.databinding.SearchResultCardBinding
import com.treinetic.whiteshark.databinding.SectionHeaderBinding
import com.treinetic.whiteshark.models.Author
import com.treinetic.whiteshark.models.Book
import com.treinetic.whiteshark.models.Publisher
import com.treinetic.whiteshark.models.SearchResult
import com.treinetic.whiteshark.util.extentions.drawStrikeThrough
import com.treinetic.whiteshark.util.extentions.setTextColorRes
import com.treinetic.whiteshark.util.extentions.toCurrency


class SectionAdapter(
    sectionParameters: SectionParameters,
    val title: String,
    val list: List<Any>,
    var adapter: SectionedRecyclerViewAdapter
) : StatelessSection(sectionParameters) {

    var onClick: ((position: Int, parseList: List<Any>) -> Unit)? = null
    var headerClick: ((type: String) -> Unit)? = null

    override fun getContentItemsTotal() = list.size

    override fun onBindItemViewHolder(viewHolder: RecyclerView.ViewHolder?, position: Int) {
        val value = list[position]
        val holder = viewHolder as ItemViewHolder
        holder.apply {
            when (value) {
                is Book -> {
                    value as Book
                    bookName.text = value.title
                    bookAuthor.text = value.bookAuthors[0].user.name
                    setPrice(value, holder)
                    setOffer(value, holder)
                    loadImage(
                        viewHolder.itemView,
                        viewHolder.bookIMage,
                        value.getBookImage(),
                        R.drawable.book_placeholder
                    )
                }
                is Publisher -> {
                    value as Publisher
                    bookName.text = value.name
                    bookIMage.visibility = View.GONE
                    authorImage.visibility = View.VISIBLE
                    promoContainer.visibility = View.GONE
                    detailsContainer.gravity = Gravity.CENTER_VERTICAL
                    bookAuthor.visibility = View.GONE
                    value.image?.let {
                        loadImage(
                            viewHolder.itemView,
                            viewHolder.authorImage,
                            it.getSmallImage(),
                            R.drawable.placeholder_user
                        )
                    }
                }
                else -> {
                    value as Author
                    bookName.text = value.user.name
                    bookIMage.visibility = View.GONE
                    authorImage.visibility = View.VISIBLE
                    promoContainer.visibility = View.GONE
                    detailsContainer.gravity = Gravity.CENTER_VERTICAL
                    bookAuthor.visibility = View.GONE
                    value.user.image?.let {
                        loadImage(
                            viewHolder.itemView,
                            viewHolder.authorImage,
                            it.getSmallImage(),
                            R.drawable.placeholder_user
                        )
                    }
                }
            }
        }
    }

    private fun setPrice(book: Book, holder: ItemViewHolder) {
        var price = "Free"
        holder.bookPrice.setTextColorRes(R.color.colorPrimary)
        book.priceDetails.let {
            if (!it.isFree) {
                price = it.visiblePrice.toCurrency("LKR")
                holder.bookPrice.setTextColorRes(R.color.colorReviewerName)
            }
        }
//        book?.price?.let {
//            if (it > 0) {
//                price = it.toDouble().toCurrency("LKR")
//            }
//        }
        holder.bookPrice.text = price
    }

    private fun setOffer(book: Book, holder: ItemViewHolder) {
        if (!book.priceDetails.isOffer || book.promotions.isNullOrEmpty()) {
            holder.promotionLayer.visibility = View.GONE
            return
        }
        holder.promotionLayer.visibility = View.VISIBLE
        holder.promotionText.text = book.promotions[0].promotion
        holder.promotionPrice.text =
            book.priceDetails.originalPrice.toCurrency("LKR")
        holder.promotionPrice.drawStrikeThrough()
    }

    private fun loadImage(itemView: View, imageView: ImageView, url: String?, placeHolder: Int) {
        GlideApp.with(itemView)
            .load(url)
            .placeholder(placeHolder)
            .into(imageView)
    }

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder?) {
        val viewHolder = holder as HeaderViewHolder
        viewHolder.tittle.text = title
    }

    override fun getItemViewHolder(view: View): ItemViewHolder {
        return ItemViewHolder(
            SearchResultCardBinding.inflate(
                LayoutInflater.from(view.context),
                null,
                false
            ), this
        )
    }

    override fun getHeaderViewHolder(view: View): RecyclerView.ViewHolder {
        return HeaderViewHolder(
            SectionHeaderBinding.inflate(
                LayoutInflater.from(
                    view.context
                ),
                null,
                false
            ), this
        )
    }

    class HeaderViewHolder(val binding: SectionHeaderBinding, val adapter: SectionAdapter) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {
        var tittle = binding.tittle

        init {
            binding.btnSeeAll.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            when (view) {
                binding.btnSeeAll -> {
                    adapter.headerClick?.let {
                        it(tittle.text.toString())
                    }
                }
            }
        }
    }

    class ItemViewHolder(val binding: SearchResultCardBinding, val adapter: SectionAdapter) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {

        private var cardView = binding.searchBookCard
        var bookName = binding.sBookName
        var bookAuthor = binding.sBookAuthor
        var bookPrice = binding.sBookPrice
        var bookIMage = binding.sBookImage
        var authorImage = binding.profileImage
        var promotionText = binding.promotionText
        var promotionPrice = binding.promotionPrice
        var promotionLayer = binding.promotionLayer
        var promoContainer = binding.promoContainer
        var detailsContainer = binding.detailsContainer

        init {
            cardView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {

            when (view) {
                binding.searchBookCard -> adapter.onClick?.let {
                    val pos = adapter.adapter.getPositionInSection(adapterPosition)
                    it(pos, adapter.list)

                }
            }
        }
    }
}