package com.treinetic.whiteshark.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.databinding.CustomPublisherAuthorCardviewBinding
import com.treinetic.whiteshark.models.Publisher

class PublishersAdapter(val publisherList: MutableList<Publisher>) :

    RecyclerView.Adapter<PublishersAdapter.PublisherViewHolder>() {

    var onClick: ((position: Int, publisher: Publisher) -> Unit)? = null
    var loadMore: ((publisher: Publisher) -> Unit)? = null


    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): PublisherViewHolder {
        return PublisherViewHolder(
            CustomPublisherAuthorCardviewBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            ),
            this
        )
    }

    override fun getItemCount() = publisherList.size

    override fun onBindViewHolder(viewHolder: PublisherViewHolder, position: Int) {
        val event = publisherList[position]
        viewHolder.setPublisher(event)

        if (position == publisherList.size - 2) {
            loadMore?.let {
                it(event)
            }
        }
    }

    class PublisherViewHolder(val binding: CustomPublisherAuthorCardviewBinding, val adapter: PublishersAdapter) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {
        init {
            binding.btnCardItem.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            when (view) {
                binding.btnCardItem -> adapter.onClick?.let {
                    it(adapterPosition, adapter.publisherList[adapterPosition])
                }
            }
        }

        fun setPublisher(publisher: Publisher) {
            binding.cardName.text = publisher.name
            publisher.image?.let {
                Glide.with(binding.root.context).load(it.medium)
                    .placeholder(R.drawable.placeholder_user)
                    .into(binding.cardImg)
            }
        }


    }

}

