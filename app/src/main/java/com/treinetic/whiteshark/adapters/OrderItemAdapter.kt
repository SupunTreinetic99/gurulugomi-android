package com.treinetic.whiteshark.adapters

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.databinding.CustomBookCardviewBinding
import com.treinetic.whiteshark.databinding.HomeSectionListItemBinding
import com.treinetic.whiteshark.models.OrderItem
import com.treinetic.whiteshark.util.extentions.toCurrency

class OrderItemAdapter(private val orderItem: MutableList<OrderItem>) :
    RecyclerView.Adapter<OrderItemAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): OrderViewHolder {
        return OrderViewHolder(
            CustomBookCardviewBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            ),
            this
        )
    }

    override fun getItemCount() = orderItem.size

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {

        val book = orderItem[position].book

        holder.apply {
            book.priceDetails.let {

                //                subTotal.text = it.originalPrice.toCurrency(it.currency)
                discount.text = it.originalPrice.toCurrency(it.currency)
                discount.paintFlags = discount.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                if (it.discount == 0.00) {
                    discount.visibility = View.GONE
                }
                totalPayable.text = it.visiblePrice.toCurrency(it.currency)
            }

            if (book.promotions.isNotEmpty()) {
                promotion.text = book.promotions[0].promotion
            } else {
                holder.promotionValue.visibility = View.GONE
            }

            iconMore.visibility = View.GONE
            book.bookAuthors.let {
                bookAuthor.text = it[0]?.user?.name
            }

            bookName.text = book.title
        }
        holder.purchasedBookCard.setBackgroundColor(Color.parseColor("#ffffff"))
        Glide.with(holder.itemView)
            .load(book.getBookImage())
            .placeholder(R.drawable.book_placeholder)
            .into(holder.bookImage)


    }

    class OrderViewHolder(val binding: CustomBookCardviewBinding, val adapter: OrderItemAdapter) :
        RecyclerView.ViewHolder(binding.root) {

        var promotion = binding.promotion
        var discount = binding.discount
        var totalPayable = binding.totalPayable
        var bookImage = binding.pbookImage
        var bookName = binding.bookName
        var bookAuthor = binding.bookAuthor
        var iconMore = binding.iconMore
        var purchasedBookCard = binding.purchasedBookCard
        var promotionValue = binding.promotionValue

    }
}