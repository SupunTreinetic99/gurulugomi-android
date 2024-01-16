package com.treinetic.whiteshark.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.constance.SearchTypes
import com.treinetic.whiteshark.databinding.SearchAuthorResultBinding
import com.treinetic.whiteshark.databinding.SearchResultCardBinding
import com.treinetic.whiteshark.glide.GlideApp
import com.treinetic.whiteshark.models.Author
import com.treinetic.whiteshark.models.Book
import com.treinetic.whiteshark.models.Publisher
import com.treinetic.whiteshark.util.extentions.drawStrikeThrough
import com.treinetic.whiteshark.util.extentions.setTextColorRes
import com.treinetic.whiteshark.util.extentions.toCurrency

/**
 * Created by Nuwan on 2/20/19.
 */
class SearchAdapter(var list: List<Any>, val type: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var TAG = "SearchAdapter"

    var onClickBook: (
        (position: Int, book: Book, type: String) -> Unit)? = null
    var onClickAuthor: (
        (position: Int, author: Author, type: String) -> Unit)? = null
    var onClickPublisher: (
        (position: Int, publisher: Publisher, type: String) -> Unit)? = null
    var fetchNextPage: (
        (position: Int, item: Any, type: String) -> Unit)? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): RecyclerView.ViewHolder {

        when (type) {
            SearchTypes.SEARCH_AUTHORS -> {
                // AUTHORS
                return SearchAuthorViewHolder(
                    SearchAuthorResultBinding.inflate(
                        LayoutInflater.from(viewGroup.context),
                        viewGroup,
                        false
                    ),
                    this
                )
            }
            SearchTypes.SEARCH_PUBLISHERS -> {
                // PUBLISHERS
                return SearchPublisherViewHolder(
                    SearchAuthorResultBinding.inflate(
                        LayoutInflater.from(viewGroup.context),
                        viewGroup,
                        false
                    ),
                    this
                )
            }
            else -> {
                // BOOKS
                return SearchBookViewHolder(
                    SearchResultCardBinding.inflate(
                        LayoutInflater.from(viewGroup.context),
                        viewGroup,
                        false
                    ),
                    this
                )
            }
        }

    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {

        if (list.size - 3 == position) {
            Log.d(TAG, "callling fetchNextPage")
            fetchNextPage?.let { it(position, list.get(position), type) }
        }
        if (list[position] is Author) {
            (viewHolder as SearchAuthorViewHolder).setAuthor(list[position] as Author)
            return
        }

        if (list[position] is Publisher) {
            (viewHolder as SearchPublisherViewHolder).setPublisher(list[position] as Publisher)
            return
        }
        if (list[position] is Book) {
            (viewHolder as SearchBookViewHolder).setBook(list[position] as Book)
            return
        }
    }

    class SearchBookViewHolder(val binding: SearchResultCardBinding, val adapter: SearchAdapter) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {

        override fun onClick(view: View?) {
            when (view) {
                binding.searchBookCard -> adapter.onClickBook?.let {
                    it(adapterPosition, adapter.list[adapterPosition] as Book, adapter.type)
                }
            }
        }


        fun setBook(book: Book) {

            binding.sBookName.text = book.title
            binding.sBookAuthor.text = book.getAuthorDisplayName()
//            itemView.sBook_price.text =
//                book.priceDetails.visiblePrice.toCurrency(book.priceDetails.currency)
            setPrice(book)
            setOffer(book)
            binding.searchBookCard.setOnClickListener(this)
            if (book.bookImages.isNullOrEmpty()) {
                return
            }

            var imageurl = ""
            book.getBookImage().let {
                imageurl = it
            }

            GlideApp.with(binding.root.context)
                .load(imageurl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.placeholder)
                .into(binding.sBookImage)
        }

        @SuppressLint("SetTextI18n")
        fun setPrice(book: Book) {

            if (book.priceDetails.isFree) {
                binding.sBookPrice.text = "Free"
                binding.sBookPrice.setTextColorRes(R.color.colorPrimary)
                return
            }
            binding.sBookPrice.setTextColorRes(R.color.colorReviewerName)
            binding.sBookPrice.text =
                book.priceDetails.visiblePrice.toCurrency(book.priceDetails.currency)

        }

        fun setOffer(book: Book) {
            if (!book.priceDetails.isOffer || book.promotions.isNullOrEmpty()) {
                binding.promotionLayer.visibility = View.GONE
                return
            }
            binding.promotionLayer.visibility = View.VISIBLE
            binding.promotionText.text = book.promotions[0].promotion
            binding.promotionPrice.text =
                book.priceDetails.originalPrice.toCurrency("LKR")
            binding.promotionPrice.drawStrikeThrough()
        }

    }


    class SearchAuthorViewHolder(val binding: SearchAuthorResultBinding, val adapter: SearchAdapter) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {

        override fun onClick(view: View?) {
            when (view) {
                binding.searchAuthorCard -> adapter.onClickAuthor?.let {
                    it(adapterPosition, adapter.list[adapterPosition] as Author, adapter.type)
                }

            }

        }

        fun setAuthor(author: Author) {
            binding.sBookAuthor.text = author.user.name
            binding.searchAuthorCard.setOnClickListener(this)
            var imageurl = ""
            author.user.image?.getImage()?.let {
                imageurl = it
            }

            GlideApp.with(binding.root.context)
                .load(imageurl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.placeholder_user)
                .into(binding.profileImage)


        }
    }

    class SearchPublisherViewHolder(val binding: SearchAuthorResultBinding, val adapter: SearchAdapter) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {


        override fun onClick(view: View?) {
            when (view) {
                binding.searchAuthorCard -> adapter.onClickPublisher?.let {
                    it(adapterPosition, adapter.list[adapterPosition] as Publisher, adapter.type)
                }
            }
        }

        fun setPublisher(publisher: Publisher) {
            binding.sBookAuthor.text = publisher.name
            binding.searchAuthorCard.setOnClickListener(this)
            var imageurl = ""
            publisher.image?.getImage()?.let {
                imageurl = it
            }

            GlideApp.with(binding.root.context)
                .load(imageurl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.placeholder_user)
                .into(binding.profileImage)


        }

    }


}