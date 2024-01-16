package com.treinetic.whiteshark.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.databinding.MyCardBinding
import com.treinetic.whiteshark.glide.GlideApp
import com.treinetic.whiteshark.models.Book

class HomeBookAdapter(private val bookList: List<Book>) :
    RecyclerView.Adapter<HomeBookAdapter.ReviewSectionHolder>() {

    var onClick: (
        (position: Int, book: Book) -> Unit)? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ReviewSectionHolder {

        return ReviewSectionHolder(
            MyCardBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            ),
            this
        )
    }

    override fun getItemCount() = bookList.size

    override fun onBindViewHolder(viewHolder: ReviewSectionHolder, position: Int) {

        val holder = viewHolder as ReviewSectionHolder
        holder.setData()

    }

    class ReviewSectionHolder(var binding: MyCardBinding, val adapter: HomeBookAdapter) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {

        init {
            binding.bookCardView.setOnClickListener(this)
        }

        var bookImage: ImageView = binding.bookImage
        override fun onClick(view: View?) {
            when (view) {
                binding.bookCardView -> adapter.onClick?.let {
                    it(adapterPosition, adapter.bookList[adapterPosition])
                }

            }
        }

        fun setData() {
            val book = adapter.bookList[adapterPosition]
            GlideApp.with(binding.root)
                .load(book.getBookImage())
                .placeholder(R.drawable.book_placeholder)
                .into(bookImage)

            binding.offerView.book = book
        }


    }

}