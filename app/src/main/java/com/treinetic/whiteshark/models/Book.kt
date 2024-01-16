package com.treinetic.whiteshark.models

import android.util.Log
import com.google.gson.annotations.SerializedName
import com.treinetic.whiteshark.roomdb.models.BookData
import com.treinetic.whiteshark.services.LocalBookService
import com.treinetic.whiteshark.services.UserService
import java.io.File
import java.lang.Exception

data class Book(
    @SerializedName("id") var id: String,
    @SerializedName("title") var title: String,
    @SerializedName("product_code") var productCode: String? = null,
    @SerializedName("isbn") var isbn: String? = null,
    @SerializedName("price") var price: Float? = null,
    @SerializedName("book_url") var book_url: String? = null,
    @SerializedName("book_preview_url") var book_preview_url: String? = null,
    @SerializedName("purchase_link") var purchaseLink: String? = null,
    @SerializedName("description") var description: String? = null,
    @SerializedName("pages") var pages: Int? = null,
    @SerializedName("file_type") var fileType: String? = null,
    @SerializedName("epub_version") var epubVersion: String? = null,
    @SerializedName("web_link") var web_link: String? = null,
    @SerializedName("edition") var edition: String? = null,
    @SerializedName("rating_value") var ratingValue: Float,
    @SerializedName("ratings_count") var ratingsCount: Int = 0,
    @SerializedName("ratingOfUser") var ratingOfUser: Rating? = null,
    @SerializedName("isPurchased") var isPurchased: Boolean = false,
    @SerializedName("price_details") var priceDetails: PriceDetails,
    @SerializedName("book_images") var bookImages: MutableList<BookImages>,
    @SerializedName("isAtWishlist") var isAtWishlist: Boolean = false,
    @SerializedName("categories") var categories: List<Category>? = null,
    @SerializedName("languages") var languages: List<Language>,
    @SerializedName("book_authors")
    var bookAuthors: List<Author> = mutableListOf(),
    @SerializedName("book_system_fonts")
    var bookFont: MutableList<Font> = mutableListOf(),
    @SerializedName("ratings")
    var ratings: MutableList<Rating> = mutableListOf(),
    @SerializedName("promotions") var promotions: MutableList<Promotion> = mutableListOf(),
    @SerializedName("translators") var translators: MutableList<Translator> = mutableListOf(),
    @SerializedName("share_url") var shareUrl: String?,

    var status: Boolean = false,
    var isClick: Boolean = false,
    var isFill: Boolean = false
) {
    var byteLength: Double = -1.00
    var downloadedEpubVersion: String? = null
    var validationKey: String = ""
    var isFetchedBeforeLogin = false

    var localPath: String? = null
    var action: Book.ActionType = ActionType.ADD_TO_CART
        get() {
            if (!UserService.getInstance().isUserLogged()) return ActionType.ADD_TO_CART
            if (isPurchased) {
                return ActionType.READ_NOW
            }
            return ActionType.ADD_TO_CART

        }


    var bottomBtnAction: Book.ActionType = ActionType.REVIEW
        get() {
            if (isPurchased && ratingOfUser == null) {
                return ActionType.REVIEW
            } else if (isPurchased) {
                return ActionType.SHOP_MORE
            } else {
                return ActionType.READ_NOW
            }
        }

    fun setIsMyReview() {

        if(ratings.isNullOrEmpty()){
            Log.e("XXX","ratings is null")
            return
        }

        ratings.forEach { rating: Rating ->
            Log.d("XXX","${rating.userCustomerUserId}")
            if (ratingOfUser?.userCustomer?.userId == rating.userCustomerUserId) {
                rating.isMyReview = true
            }
        }

    }

    val isFree: Boolean
        get() {
            return if (priceDetails == null) false else priceDetails.isFree
        }

    val isOffer: Boolean
        get() {
            return if (priceDetails == null) false else priceDetails.isOffer
        }


    fun fill(book: Book): Book {

        isFill = true

        book.promotions.let {
            promotions = it
        }


        book.bookImages.let {
            bookImages = it
        }

        book.bookFont.let {
            bookFont = it
        }

        book.ratingOfUser.let {
            ratingOfUser = it
        }

        book.description.let {
            description = it
        }

        book.ratings.let {
            ratings = it
        }

        book.ratings.let {
            ratings = it
        }


        book.book_preview_url?.let {
            book_preview_url = it
        }

        book.pages?.let {
            pages = it
        }


        book.fileType?.let {
            fileType = it
        }

        book.epubVersion?.let {
            epubVersion = it
        }


        book.web_link?.let {
            web_link = it
        }
        book.edition?.let {
            edition = it
        }

        book.ratingValue.let {
            ratingValue = it
        }

        book.ratingsCount?.let {
            ratingsCount = it
        }

        book.isAtWishlist.let {
            isAtWishlist = it
        }

        book.priceDetails.let {
            priceDetails = it
        }

        book.priceDetails.let {
            priceDetails = it
        }


        book.categories?.let {
            categories = it
        }


        book.languages?.let {
            languages = it
        }


        book.bookAuthors.let {
            bookAuthors = it
        }

        book.ratings.let {
            ratings = it
        }

        book.book_url.let {
            book_url = it
        }

        book.isPurchased?.let {
            isPurchased = it
        }
        isFetchedBeforeLogin = book.isFetchedBeforeLogin
        return this
    }


    fun getRatingUSerImages(): List<String> {

        if (ratings == null) return listOf()

        return ratings.let {
            it.filter { it.userCustomer?.user?.image?.getSmallImage() != null }
                .map { it.userCustomer?.user?.image?.getSmallImage()!! }
        }
    }


    fun getAuthorDisplayName(): String {

        if (bookAuthors.isNullOrEmpty()) {
            return ""
        }
        return bookAuthors.map { it.user.name }.joinToString(separator = ",")

    }


    fun getBookImage(): String {
        bookImages.let {
            if (it.isEmpty()) {
                return ""
            }

            it.first().image.getImage()?.let {
                return it
            }
            return ""
        }

        return ""
    }


    fun getLocallyAvailable(onFinish: (isAvailable: Boolean, bookData: BookData?) -> Unit) {

        val userId = UserService.getInstance().getUser()?.user_id
        if (userId != null) {
            LocalBookService.getInstance().getBook(id, userId) { data: BookData? ->
                data?.let { bookData ->
                    bookData.getBookObj()?.localPath?.let {
                        val file = File(it)
                        var isFileSizeCorrect = isFullFileAvailable(bookData, file)
                        var isFileAvailable: Boolean = file.exists() && isFileSizeCorrect
                        onFinish(isFileAvailable, data)
                        return@getBook
                    }
                }
                onFinish(false, null)
            }
        }
    }

    private fun isFullFileAvailable(data: BookData, file: File): Boolean {
        var savedBytesLength = data?.getBookObj()!!.byteLength
        var fileBytesLength = file.length().toDouble()
        var hasByteLength = data?.getBookObj()!!.byteLength > -1.00

        if (!hasByteLength) return true
        if (fileBytesLength <= 0) return false
        return fileBytesLength == savedBytesLength
    }

    enum class ActionType {
        REVIEW, SHOP_MORE, READ_NOW, ADD_TO_CART
    }

    fun getCategoriesToDisplay(seperator: String = ","): String {
        var names = ""

        categories?.let {
            if (it.isNotEmpty()) {
                names = it[0].category!!
                return@let
            }
            return names
        }

        categories?.forEachIndexed { index, category ->
            if (index > 0) {
                names += seperator
                names += category.category
            }
        }

        return names
    }

    fun isBookUpdated(): Boolean {
        if (epubVersion == null || downloadedEpubVersion == null) return false
        return epubVersion == downloadedEpubVersion
    }

    fun isBookUpdated(version: String): Boolean {
        Log.d("Book ", " isBookUpdated version :$version  downloaded:$downloadedEpubVersion")
        return version == downloadedEpubVersion
    }

    fun getTimelyDiscountValue(): Double? {
        var promotion: Promotion? = promotions.find {
            it.timely_discount != null
        }

        return promotion?.timely_discount?.discount

    }

    fun hasTimelyDiscount(): Boolean {
        var promotion: Promotion? = promotions.find {
            it.timely_discount != null
        }

        return promotion != null
    }


    fun isAvailablePromotions(): Boolean {
        if (promotions == null) return false
        return promotions.size > 0
    }

    fun deleteBook() {
        try {
            localPath?.let {
                var file = File(it)
                if (file.exists()) {
                    file.delete()
                    localPath = null
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isValidDownloadedCopy(key: String = LocalBookService.VALIDATION_KEY) = validationKey == key

}

