package com.treinetic.whiteshark.fragments.publisherbooks

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.Books
import com.treinetic.whiteshark.services.BookService
import com.treinetic.whiteshark.services.Service

class PublisherBookViewModel : ViewModel() {

    private val TAG = "PublisherBookVM"
    private var books: MutableLiveData<Books> = MutableLiveData()
    private var netException: MutableLiveData<NetException> = MutableLiveData()
    private var publisherId: String? = null


    fun getBooks(): MutableLiveData<Books> {
        return books
    }

    fun getException(): MutableLiveData<NetException> {
        return netException
    }

    fun getPublisherBooks(publisherId: String) {
        this.publisherId.let {
            if (it == publisherId && books.value != null) {
                return
            }
        }

        this.publisherId = publisherId
        BookService.getInstance().getPublishersBooks(publisherId, Service.Success {
            Log.d(TAG, "Success publisher books")
            books.postValue(it)
        }, Service.Error {
            it.printStackTrace()
            netException.postValue(it)
        })

    }


    fun getNextPage() {

        books.value?.next_page_url?.let {
            BookService.getInstance().getNextPage(it, Service.Success {
                var booksPage = books.value
                booksPage?.data?.addAll(it.data)
                booksPage?.next_page_url = it.next_page_url
                this.books.postValue(booksPage)
            }, Service.Error {

            })
        }


    }
}