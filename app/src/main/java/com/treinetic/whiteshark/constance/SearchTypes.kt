package com.treinetic.whiteshark.constance

/**
 * Created by Nuwan on 2/20/19.
 */
class SearchTypes {

    companion object {

        const val SEARCH_BOOKS: String = "Book"
        const val SEARCH_AUTHORS: String = "Author"
        const val SEARCH_PROMOSTION: String = "Promotion"
        const val SEARCH_AUTHOR_BOOKS: String = "Author Books"
        const val SEARCH_PUBLISHERS: String = "Publisher"
        const val SEARCH_PUBLISHER_BOOKS: String = "Publisher Books"
        val typeArray = arrayOf(
            SEARCH_BOOKS,
            SEARCH_PROMOSTION,
            SEARCH_AUTHORS,
            SEARCH_PUBLISHERS
        )
    }

}