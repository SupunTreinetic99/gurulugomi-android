package com.treinetic.whiteshark.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.databinding.CustomPublisherAuthorCardviewBinding
import com.treinetic.whiteshark.models.Author
import de.hdodenhof.circleimageview.CircleImageView

class AuthorsAdapter(private val authorList: MutableList<Author>) : RecyclerView.Adapter<AuthorsAdapter.AuthorViewHolder>() {

    var onClick: ((position: Int, author: Author) -> Unit)? = null
    var loadMore: ((author: Author) -> Unit)? = null


    class AuthorViewHolder(private val binding: CustomPublisherAuthorCardviewBinding) :
        RecyclerView.ViewHolder(binding.root){
        val itemCard = binding.btnCardItem
        val cardName = binding.cardName
        var image = binding.cardImg
        val context = binding.root

        fun setAuthor(author: Author) {
                author.user.image?.let {
                    Glide.with(binding.root.context).load(it.medium)
                        .placeholder(R.drawable.placeholder_user)
                        .into(binding.cardImg)
                }
            }

    }

    fun sub(){
//        View.OnClickListener {
//            init {
//                binding.btnCardItem.setOnClickListener(this)
//            }
//
//            override fun onClick(view: View?) {
//                when (view) {
//                    binding.btnCardItem -> adapter.onClick?.let {
//                        it(adapterPosition, adapter.authorList[adapterPosition])
//                    }
//                }
//            }
//
//            fun setAuthor(author: Author) {
//                binding.cardName.text = author.user.name
//                author.user.image?.let {
//                    Glide.with(binding.root.context).load(it.medium)
//                        .placeholder(R.drawable.placeholder_user)
//                        .into(binding.cardImg)
//                }
//            }
//        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): AuthorViewHolder {
        return AuthorViewHolder(
            CustomPublisherAuthorCardviewBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            ),
        )
    }

    override fun onBindViewHolder(viewHolder: AuthorViewHolder, position: Int) {
        val a = authorList[position]
        viewHolder.cardName.text = a.user.name
        viewHolder.setAuthor(a)
        viewHolder.itemCard.setOnClickListener{
            this.onClick.let {
                if (it != null) {
                    it(position, a)
                }
            }
        }

        if (position == authorList.size - 2) {
            loadMore?.let {
                it(a)
            }
        }
    }

    override fun getItemCount() = authorList.size
}

