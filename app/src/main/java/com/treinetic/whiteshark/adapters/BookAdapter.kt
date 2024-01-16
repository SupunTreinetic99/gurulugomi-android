package com.treinetic.whiteshark.adapters


import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.constance.AdapterType
import com.treinetic.whiteshark.databinding.CustomBookCardviewBinding
import com.treinetic.whiteshark.models.Book
import com.treinetic.whiteshark.util.extentions.toCurrency


class BookAdapter(val type: String, var bookList: List<Book>) :
    RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    var onClick: ((position: Int, book: Book, itemview: View) -> Unit)? = null
    var onPopup: ((position: Int, book: Book, v: View) -> Unit)? = null
    var loadMore: ((bookList: List<Book>, position: Int) -> Unit)? = null
    private val loadingLimit = 5


    class BookViewHolder(val binding: CustomBookCardviewBinding, var adapter: BookAdapter) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {

        var promotion = binding.promotion
        var discount = binding.discount
        var totalPayable = binding.totalPayable
        var bookImage = binding.pbookImage
        var bookName = binding.bookName
        var bookAuthor = binding.bookAuthor
        var iconMore = binding.iconMore
        var purchaseBookCard = binding.purchasedBookCard
        var checkIconBkg = binding.checkIconBkg
        lateinit var model: Book

        init {
            binding.purchasedBookCard.setOnClickListener(this)
            binding.iconMore.setOnClickListener(this)
        }

        override
        fun onClick(view: View?) {

            when (view) {
                binding.purchasedBookCard -> {
                    adapter.onClick?.let {
                        it(adapterPosition, model, binding.purchasedBookCard)
                    }
                }
                binding.iconMore -> {
                    adapter.onPopup?.let {
                        it(adapterPosition, model, binding.iconMore)

                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): BookViewHolder {
        return BookViewHolder(
            CustomBookCardviewBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            ),
            this
        )
    }

    override fun getItemCount() = bookList.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: BookViewHolder, position: Int) {
        val book = bookList[position]
        val bookViewHolder = viewHolder as BookViewHolder

        bookViewHolder.apply {
            book.priceDetails.let {
                //                subTotal.text = it.originalPrice.toCurrency(it.currency)
                discount.text = it.originalPrice.toCurrency(it.currency)
                discount.paintFlags = viewHolder.discount.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

                if (!it.isOffer) {
                    viewHolder.discount.visibility = View.GONE
                }
                totalPayable.text = it.visiblePrice.toCurrency(it.currency)
            }

            if (book.promotions.isNotEmpty()) {
                promotion.text = book.promotions[0].promotion
            } else {
                binding.promotionValue.visibility = View.GONE
            }

            model = book
            bookName.text = book.title
            bookAuthor.text = book.bookAuthors[0]?.user?.name
            iconMore.visibility = View.GONE
        }

        book.bookImages[0].let {
            Glide.with(viewHolder.itemView)
                .load(it.image.medium).placeholder(R.drawable.book_placeholder)
                .into(bookViewHolder.bookImage)
        }

        if (type == AdapterType.WISHLIST) {
            bookViewHolder.iconMore.visibility = View.VISIBLE
        }

        if (!book.isClick) {
            viewHolder.purchaseBookCard.setBackgroundColor(Color.parseColor("#ffffff"))
            viewHolder.checkIconBkg.visibility = View.GONE

        } else {
            viewHolder.purchaseBookCard.setBackgroundColor(Color.parseColor("#FDDACE"))
            viewHolder.checkIconBkg.visibility = View.VISIBLE
        }

        loadMore?.let {
            if (position == bookList.size - loadingLimit) it(bookList, position)
        }
    }
}

