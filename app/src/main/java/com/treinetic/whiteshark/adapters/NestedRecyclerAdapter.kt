package com.treinetic.whiteshark.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.customview.BookSectionView
import com.treinetic.whiteshark.databinding.HomeSectionListItemBinding
import com.treinetic.whiteshark.models.Book
import com.treinetic.whiteshark.models.BookCategory


class NestedRecyclerAdapter(private val bookCategory: BookCategory, val context: Context) :
    RecyclerView.Adapter<NestedRecyclerAdapter.NestedRecyclerViewHolder>() {

    var onClick: ((position: Int, book: Book, list: ArrayList<Book>?) -> Unit)? = null
    var loadMore: ((position: Int) -> Unit)? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): NestedRecyclerViewHolder {
        return NestedRecyclerViewHolder(
            HomeSectionListItemBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            ),
            this
        )
    }


    override fun getItemCount() = bookCategory.data.size

    override fun onBindViewHolder(viewHolder: NestedRecyclerViewHolder, position: Int) {

        val category = bookCategory.data[position]

        viewHolder.categorySection.setData(category)
        viewHolder.categorySection.onItemClick = onClick
        if (category.showBackground) {
            viewHolder.categorySection.setBackground(true)
        } else {
            viewHolder.categorySection.setBackground(false)
        }
        if (position == bookCategory.data.size.minus(4)) {
            loadMore?.let {
                it(position)
            }
        }
    }

    class NestedRecyclerViewHolder(val binding: HomeSectionListItemBinding, val adapter: NestedRecyclerAdapter) :
        RecyclerView.ViewHolder(binding.root) {
        var categorySection: BookSectionView = binding.sectionList
    }
}