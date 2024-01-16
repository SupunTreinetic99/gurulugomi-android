package com.treinetic.whiteshark.fragments.searchresult

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.treinetic.whiteshark.constance.SearchTypes
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.Authors
import com.treinetic.whiteshark.models.Books
import com.treinetic.whiteshark.models.Publishers
import com.treinetic.whiteshark.services.*

/**
 * Created by Nuwan on 2/20/19.
 */
class SearchResultViewModel : ViewModel() {

    private val TAG = "SearchResultVM"
    private var books: MutableLiveData<Books> = MutableLiveData()
    var authors: MutableLiveData<Authors> = MutableLiveData()
    var publishers: MutableLiveData<Publishers> = MutableLiveData()
    private var netException: MutableLiveData<NetException> = MutableLiveData()

    var authorId: String? = null
    var publisherId: String? = null
    var type: String? = null

    fun clearError() {
        netException.value = null
    }

    fun getBooks(): MutableLiveData<Books> {
        return books
    }

    fun getException(): MutableLiveData<NetException> {
        return netException
    }

    fun getNextPageBooks() {


    }

    fun getNextPageAuthors() {


    }

    fun loadSearchData() {

        if (type == SearchTypes.SEARCH_AUTHORS) {
            authors.value = SearchService.getInstance().getSearchSection(type!!) as Authors
        } else if (type == SearchTypes.SEARCH_PUBLISHERS) {
            publishers.value = SearchService.getInstance().getSearchSection(type!!) as Publishers
        } else {
            books.value = SearchService.getInstance().getSearchSection(type!!) as Books
        }

    }


    fun getAuthorBooks(authorId: String) {

        BookService.getInstance().getAuthorBooks(authorId, Service.Success {
            Log.d(TAG, "Success aauther books")
            books.postValue(it)
        }, Service.Error {
            it.printStackTrace()
            netException.postValue(it)
        })

    }

    fun getPublisherBooks(publisherId: String) {

        BookService.getInstance().getPublishersBooks(publisherId, Service.Success {
            Log.d(TAG, "Success publishers books")
            books.postValue(it)
        }, Service.Error {
            it.printStackTrace()
            netException.postValue(it)
        })

    }

    fun getNextPage(url: String?) {
        if (url == null) {
            Log.d(TAG, "getNextPage url null")
            return
        }
        BookService.getInstance().getNextPage(url,
            Service.Success { books ->
                Log.d(TAG, "getNextPage success")

                this.books.value?.let {
                    it.data.addAll(books.data)
                    books.data = it.data
                    this.books.postValue(books)
                    Log.d(TAG, "getNextPage value posted")
                    return@Success
                }
                this.books.value = books

            }, Service.Error {
                Log.d(TAG, "Error in next page")
                it.printStackTrace()
            })

    }


    fun getAuthorNextPage(url: String?) {
        if (url == null) {
            Log.d(TAG, "getAuthorNextPage url null")
            return
        }
        AuthorService.getInstance().getAuthorPage(url,
            Service.Success { authorPage ->

                authors.value?.let {
                    it.data?.addAll(authorPage.data)
                    authorPage.data = it.data
                    authors.postValue(authorPage)
                    return@Success
                }
                authors.postValue(authorPage)
            },
            Service.Error {
                netException.postValue(it)
            })
    }

    fun getPublisherNextPage(url: String?) {
        if (url == null) {
            Log.d(TAG, "getPublisherNextPage url null")
            return
        }
        PublisherService.getInstance().getPublisherPage(url,
            Service.Success { publisherPage ->

                publishers.value?.let {
                    it.data?.addAll(publisherPage.data)
                    publisherPage.data = it.data
                    publishers.postValue(publisherPage)
                    return@Success
                }
                publishers.postValue(publisherPage)
            },
            Service.Error {
                netException.postValue(it)
            })
    }


    fun getFirstPage(types: String) {

        if (type == SearchTypes.SEARCH_AUTHORS) {
            if (authors.value != null) return
            val authorsData = SearchService.getInstance().getSearchSection(type!!) as Authors
            getAuthorNextPage(authorsData?.first_page_url!!)
        } else if (type == SearchTypes.SEARCH_PUBLISHERS) {
            if (publishers.value != null) return
            val publisherData = SearchService.getInstance().getSearchSection(type!!) as Publishers
            getPublisherNextPage(publisherData?.first_page_url!!)
        } else {
            if (books.value != null) return
            SearchService.getInstance().getSearchSection(type ?: "")?.let {
                val books = it as Books
                getNextPage(books?.first_page_url ?: "")
            }

        }

    }


}