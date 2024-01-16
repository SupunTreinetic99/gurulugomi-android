package com.treinetic.whiteshark.adapters

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.databinding.CategoryBookCardBinding
import com.treinetic.whiteshark.glide.GlideApp
import com.treinetic.whiteshark.models.Book
import com.treinetic.whiteshark.models.Category
import com.treinetic.whiteshark.services.UserService
import com.treinetic.whiteshark.util.extentions.toCurrency


class CategoryBookAdapter(
    val type: CategoryTypes,
    val category: Category,
    var showMenu: Boolean = false
) :
    RecyclerView.Adapter<CategoryBookAdapter.CategoryBookViewHolder>() {

    val TAG = "CategoryBookAdapter"

    var onClick: ((index: Int, book: Book, view: View) -> Unit)? = null
    var onLongPress: ((index: Int, book: Book) -> Unit)? = null
    var bookList = category.books?.data
    var endPosition: ((nextPageUrl: String, categoryId: String) -> Unit)? = null
    var onPopupMenuClick: ((index: Int, book: Book, view: View, item: MenuItem) -> Unit)? = null
    val COLOUR_BLACK = "#b3000000"
    val COLOUR_WHITE = "#FFFFFF"
    var enableDownloadIndicator = false

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): CategoryBookViewHolder {
        return CategoryBookViewHolder(
            CategoryBookCardBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            ),
            this
        )
    }

    override fun getItemCount() = if (bookList == null) 0 else bookList!!.size

    override fun onBindViewHolder(viewHolder: CategoryBookViewHolder, position: Int) {

        val book = bookList!![position]
        viewHolder.bookObj = book

        GlideApp.with(viewHolder.itemView)
            .load(book.getBookImage())
            .placeholder(R.drawable.book_placeholder)
            .transition(DrawableTransitionOptions.withCrossFade(300))
            .into(viewHolder.bookImage)

        viewHolder.apply {
            bookName.text = book.title
            when {
                book.isFree -> {
                    pricelable.setBackgroundResource(R.drawable.bkg_free)
                    bookDiscount.visibility = View.GONE
                    bookPrice.text = "FREE"
                    bookPrice.setTextColor(Color.parseColor(COLOUR_WHITE))
                }
                book.isOffer || book.priceDetails.printedPrice > book.priceDetails.visiblePrice -> {
                    pricelable.setBackgroundResource(R.drawable.bkg_offer)
                    bookDiscount.text =
                        book.priceDetails.originalPrice.toCurrency(book.priceDetails.currency)
                    bookDiscount.paintFlags =
                        viewHolder.bookDiscount.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    bookPrice.text =
                        book.priceDetails.visiblePrice.toCurrency(book.priceDetails.currency)
                    bookPrice.setTextColor(Color.parseColor(COLOUR_WHITE))
                }
                else -> {
                    bookPrice.text =
                        book.priceDetails.visiblePrice.toCurrency(book.priceDetails.currency)
                    bookPrice.setTextColor(Color.parseColor(COLOUR_BLACK))
                    pricelable.setBackgroundResource(R.drawable.bkg_normal)

                }
            }
            if (type == CategoryTypes.IMAGE_ONLY) {
                pricelable.visibility = View.GONE
                bookDiscount.visibility = View.GONE
                bookPrice.visibility = View.GONE
                bookName.visibility = View.GONE
            }

            if (!showMenu) {
                menu.visibility = View.GONE
                gradientLayer.visibility = View.GONE
                offerView.book = book
            }

            if (enableDownloadIndicator) {
                GlideApp.with(viewHolder.itemView)
                    .load(R.drawable.warning_outlined)
                    .into(downloadedIcon)
                if (book.localPath != null)
                    GlideApp.with(viewHolder.itemView)
                        .load(R.drawable.checked)
                        .into(downloadedIcon)
            }

        }

        bookList?.let {
            if (position == it.size - 1)
                category.books?.next_page_url?.let {
                    if (category.id != null) endPosition?.invoke(it, category.id!!) else endPosition?.invoke(it, "")
                }
        }


    }

    enum class CategoryTypes {
        IMAGE_ONLY, DETAILS
    }

    class CategoryBookViewHolder(val binding: CategoryBookCardBinding, val adapter: CategoryBookAdapter) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener, PopupMenu.OnMenuItemClickListener {


        lateinit var bookObj: Book

        var bookImage = binding.bookImage
        var bookPrice = binding.bookPrice
        var bookDiscount = binding.bookDiscount
        var pricelable = binding.labelBkg
        var bookName = binding.catBookName
        var menu = binding.menu
        var gradientLayer = binding.gradientLayer
        var offerView = binding.offerView
        var downloadedIcon = binding.downloadedIcon


        init {
            binding.idBook.setOnClickListener(this)
//            viewItem.id_book.setOnLongClickListener(this)
            binding.menu.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            when (view) {
                binding.idBook -> adapter.onClick?.let {
                    it(adapterPosition, bookObj, itemView)
                }
                binding.menu -> {
                    showPopup(binding.menu)
                }
            }
        }

//        override fun onLongClick(view: View?): Boolean {
//            when (view) {
//                itemView.id_book -> adapter.onLongPress?.let {
//                    it(adapterPosition, bookObj)
//                }
//            }
//            return true
//        }


        private fun showPopup(v: View) {
            val popup = PopupMenu(v.context, v).apply {
                setOnMenuItemClickListener(this@CategoryBookViewHolder)
                inflate(R.menu.my_library_book_menu)
//                show()
            }
            if (UserService.isOfflineMode()) {
                popup.menu.findItem(R.id.action_profile).setVisible(false)
                popup.menu.findItem(R.id.action_review).setVisible(false)
            }
            popup.show()

        }

        override fun onMenuItemClick(item: MenuItem): Boolean {
            adapter.onPopupMenuClick?.let {
                it(adapterPosition, bookObj, menu, item)
                return true
            }
            return true
        }


    }


}


