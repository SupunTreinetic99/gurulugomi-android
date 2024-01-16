package com.treinetic.whiteshark.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.databinding.WishlistCardBinding
import com.treinetic.whiteshark.models.Book

class WishListAdapter(val wishList: List<Book>) : RecyclerView.Adapter<WishListAdapter.WishListViewHolder>() {


    var onClick: ((position: Int, book: Book) -> Unit)? = null


    override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): WishListViewHolder {

        return WishListViewHolder(
            WishlistCardBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            ),
            this
        )
    }

    override fun getItemCount() = wishList.size


    override fun onBindViewHolder(viewHolder: WishListViewHolder, position: Int) {
        val wList = wishList[position]

        val wViewHolder = viewHolder as WishListViewHolder

//
//        Glide.with(viewHolder.itemView)
//            .load(wList.book_images[0].image.medium)
//            .into(wViewHolder.wBookImage)

        wViewHolder.wBookPrice.text = wList.price.toString() + "LKR"
//        wViewHolder.wbookName.text = wList.bookName.toString() + "LKR"

    }

    class WishListViewHolder(val binding: WishlistCardBinding, adapter: WishListAdapter) : RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {

        val adapter = adapter

        var wBookImage = binding.wBookImage
        var wbookName = binding.wBookName
        var wBookPrice = binding.wBookPrice

        init {
            binding.wRootView.setOnClickListener(this)
        }


        override fun onClick(view: View?) {
            when (view) {
                binding.wRootView -> adapter.onClick?.let {
                    it(adapterPosition, adapter.wishList[adapterPosition])
                }
            }

        }


    }


}