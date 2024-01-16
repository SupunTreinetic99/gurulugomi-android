package com.treinetic.whiteshark.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.databinding.DeviceListCardBinding
import com.treinetic.whiteshark.databinding.EventCardBinding
import com.treinetic.whiteshark.models.Event

class EventAdpater(val eventList: MutableList<Event>) :
    RecyclerView.Adapter<EventAdpater.EventViewHolder>() {

    var onClick: ((position: Int, event: Event) -> Unit)? = null
    var loadMore: ((event: Event) -> Unit)? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): EventViewHolder {
        return EventViewHolder(
            EventCardBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            ),
            this
        )
    }

    override fun getItemCount() = eventList.size

    override fun onBindViewHolder(viewHolder: EventViewHolder, position: Int) {
        val event = eventList[position]
        viewHolder.setEvent(event)

        if (position == eventList.size - 2) {
            loadMore?.let {
                it(event)
            }
        }

    }

    class EventViewHolder(val  binding: EventCardBinding, val adapter: EventAdpater) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {
        init {
            binding.btnMoreEvent.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            when (view) {
                binding.btnMoreEvent -> adapter.onClick?.let {
                    it(adapterPosition, adapter.eventList[adapterPosition])
                }
            }
        }

        fun setEvent(event: Event) {
            binding.title.text = event.event
            binding.description.text = event.description
            event.images?.get(0)?.let {
                Glide.with(binding.root.context).load(it.image.getExtraLargeImage())
                    .placeholder(R.drawable.book_placeholder)
                    .into(binding.image)
            }

        }


    }
}