package com.treinetic.whiteshark.adapters

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.databinding.CategoryListCardBinding
import com.treinetic.whiteshark.models.Category

class CategoryAdapter(val category: List<Category>) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    var onClick: ((position: Int, category: Category) -> Unit)? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): CategoryViewHolder {
        return CategoryViewHolder(
            CategoryListCardBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            ),
            this
        )
    }

    override fun getItemCount() = category.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: CategoryViewHolder, position: Int) {

        val cat = category[position]
        val categoryViewHolder = viewHolder as CategoryViewHolder

        val count: String = " ( " + cat.booksCount.toString() + " )"
        categoryViewHolder.categoryName.text = cat.category + count

    }


    class CategoryViewHolder(val binding: CategoryListCardBinding, val adapter: CategoryAdapter) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        val categoryName = binding.categoryName
        val categoryItem = binding.categoryItem

        init {
            categoryItem.setOnClickListener(this)
        }


        override fun onClick(view: View?) {
            when (view) {
                binding.categoryItem -> adapter.onClick?.let {
                    it(adapterPosition, adapter.category.get(adapterPosition))
                }

            }

        }


    }


}