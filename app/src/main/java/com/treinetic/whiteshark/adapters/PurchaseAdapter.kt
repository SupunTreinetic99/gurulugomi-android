package com.treinetic.whiteshark.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.databinding.CustomBookCardviewBinding
import com.treinetic.whiteshark.databinding.CustomPublisherAuthorCardviewBinding
import com.treinetic.whiteshark.databinding.PurchaseFooterBinding
import com.treinetic.whiteshark.databinding.PurchaseHederBinding
import com.treinetic.whiteshark.models.Order
import com.treinetic.whiteshark.models.OrderItem
import com.treinetic.whiteshark.models.Promotion
import com.treinetic.whiteshark.util.extentions.drawStrikeThrough
import com.treinetic.whiteshark.util.extentions.toCurrency
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection

class PurchaseAdapter(sectionParameters: SectionParameters, private val order: Order) :
    StatelessSection(sectionParameters) {

    private val orderItemList = order.orderItems

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {

        val viewHolder = holder as PurchaseViewHolder
        val orderItem = orderItemList[position]

        viewHolder.apply {
            iconMore.visibility = View.GONE
            orderItem.book.priceDetails.let {
                setPrices(viewHolder, orderItem)
            }
            bookName.text = orderItem.book.title
            bookAuthor.text = orderItem.book.bookAuthors[0].user.name
        }
        orderItem.book.getBookImage().let {
            Glide.with(viewHolder.itemView)
                .load(orderItem.book.getBookImage())
                .placeholder(R.drawable.book_placeholder)
                .into(viewHolder.bookImage)
        }

    }

    private fun setPrices(
        viewHolder: PurchaseViewHolder,
        orderItem: OrderItem
    ) {
        viewHolder.apply {
            discountView.visibility = View.GONE
            val currency = orderItem.book.priceDetails.currency
            orderItem.amount.toCurrency(currency)
            totalPayable.text = orderItem.amount.toCurrency(currency)
//                discount.paintFlags = viewHolder.discount.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            discountSecion.visibility = View.GONE
        }
    }

    private fun getBookPromotion(orderItem: OrderItem): Promotion? {
        if (orderItem.promotions.isEmpty()) return null
        if (orderItem.book.promotions.isEmpty()) return null
        return orderItem.book.promotions.find {
            it.id == orderItem.promotions.first().id
        }
    }


    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder {
        return PurchaseViewHolder(
            CustomBookCardviewBinding.inflate(
                LayoutInflater.from(view.context),
                null,
                false
            )
            , this)
    }

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder?) {

        val headerHolder = holder as HeaderViewHolder
        headerHolder.apply {
            orderItemList.map {
                orderNo.text = it.orderId
            }
            orderDate.text = order.dateTime
        }
    }

    override fun getHeaderViewHolder(view:View): HeaderViewHolder {
        return HeaderViewHolder(
            PurchaseHederBinding.inflate(
                LayoutInflater.from(view.context),
                null,
                false
            )
            , this)
    }

    override fun getFooterViewHolder(view: View): RecyclerView.ViewHolder {
        return FooterViewHolder(
            PurchaseFooterBinding.inflate(
                LayoutInflater.from(view.context),
                null,
                false
            ),
        )
    }

    override fun onBindFooterViewHolder(holder: RecyclerView.ViewHolder?) {
        val footerViewHolder = holder as FooterViewHolder
        footerViewHolder.apply {
            orderItems.text = orderItemList.size.toString()
            totalAmount.text = order.totalPaid.toCurrency(order.getOrderItemCurrency())
        }
        setOrderPromotions(footerViewHolder)
//        setOrderPromoCodes(footerViewHolder)
//        setOrderLoyalty(footerViewHolder)
//        setOrderEVoucher(footerViewHolder)
    }

    private fun setOrderPromotions(holder: FooterViewHolder) {
        if (order.totalPaid <= order.totalAmount && order.totalAmount > 0.00) {
            val totalDiscount = order.totalAmount - order.totalPaid
            holder.promotionLayer.visibility = View.VISIBLE
            holder.promotionName.text = "Total Discounts"
            holder.discountAmount.text = totalDiscount.toCurrency(order.getOrderItemCurrency())
        } else {
            holder.promotionLayer.visibility = View.GONE
        }
        /*    if (order.hasPromotions()) {
                holder?.apply {
                    promotionLayer.visibility = View.VISIBLE
                    promotionName.text = order.promotions[0].promotion
                    discountAmount.text = order.totalAmount.toCurrency(order.getOrderItemCurrency())
                    discountAmount.drawStrikeThrough()
                }
            } else {
                holder.apply {
                    promotionLayer.visibility = View.GONE
                }
            }*/
    }

    @SuppressLint("SetTextI18n")
    fun setOrderPromoCodes(holder: FooterViewHolder) {
        if (order.hasPromoCode()) {
            holder.apply {
                promoCodeLayer.visibility = View.VISIBLE
                promoCodName.text = "Promo Code (Code: ${order.promotionCode?.promotionCode})"
                val amount = -1 * (order.promotionCode?.promotionCodeAmount!!)
                promoCodeAmount.text =
                    amount.toCurrency(order.getOrderItemCurrency())
            }
        } else {
            holder.apply {
                promoCodeLayer.visibility = View.GONE
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun setOrderLoyalty(holder: FooterViewHolder) {
        if (order.hasLoyalty()) {
            holder.apply {
                loyaltyLayer.visibility = View.VISIBLE
                loyaltyName.text =
                    "Loyalty Points (Points: ${order.loyaltyPoints?.loyaltyPointsAmount})"
                val amount =
                    -1 * (order.loyaltyPoints?.loyaltyPointsAmount!! * order.loyaltyPoints?.value_of_a_point!!)
                loyaltyAmount.text = amount.toCurrency(order.getOrderItemCurrency())
            }
        } else {
            holder.apply {
                loyaltyLayer.visibility = View.GONE
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun setOrderEVoucher(holder: FooterViewHolder) {
        if (order.hasEVoucher()) {
            holder.apply {
                eVoucherLayer.visibility = View.VISIBLE
                eVoucherName.text = "eVoucher (Voucher Code: ${order.eVoucher?.e_voucher})"
                val amount = -1 * (order.eVoucher?.eVoucherAmount!!)
                eVoucherAmount.text =
                    amount.toCurrency(order.getOrderItemCurrency())
            }
        } else {
            holder.apply {
                eVoucherLayer.visibility = View.GONE
            }
        }
    }

    override fun getContentItemsTotal() = orderItemList.size


    class HeaderViewHolder(val binding: PurchaseHederBinding, val adapter: PurchaseAdapter) :
        RecyclerView.ViewHolder(binding.root) {
        var orderNo = binding.orderNo
        var orderDate = binding.orderDate
    }

    class PurchaseViewHolder(val binding: CustomBookCardviewBinding, var adapter: PurchaseAdapter) :
        RecyclerView.ViewHolder(binding.root) {

        var promotion = binding.promotion
        var discountSecion = binding.promotionValue
        var discount = binding.discount
        var totalPayable = binding.totalPayable
        var bookImage = binding.pbookImage
        var bookName = binding.bookName
        var bookAuthor = binding.bookAuthor
        var iconMore = binding.iconMore
        var discountView = binding.discountView

    }

    class FooterViewHolder(val binding: PurchaseFooterBinding) : RecyclerView.ViewHolder(binding.root) {
        var orderItems = binding.orderItems
        var totalAmount = binding.totalAmount
        var promotionLayer = binding.promotionLayer
        var promotionName = binding.promotionName

        var promoCodeLayer = binding.promoCodeLayer
        var promoCodName = binding.promoCodeName
        var promoCodeAmount = binding.promoCodeAmount

        var loyaltyLayer = binding.loyaltyLayer
        var loyaltyName = binding.loyaltyName
        var loyaltyAmount = binding.loyaltyAmount

        var eVoucherLayer = binding.eVoucherLayer
        var eVoucherName = binding.eVoucherName
        var eVoucherAmount = binding.eVoucherAmount

        var discountAmount = binding.discountAmount
    }

}