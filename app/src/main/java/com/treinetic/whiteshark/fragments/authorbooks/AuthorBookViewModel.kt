package com.treinetic.whiteshark.fragments.authorbooks

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.Books
import com.treinetic.whiteshark.services.BookService
import com.treinetic.whiteshark.services.Service

/**
 * Created by Nuwan on 2/22/19.
 */
class AuthorBookViewModel : ViewModel() {

    private val TAG = "AuthorBookVM"
    private var books: MutableLiveData<Books> = MutableLiveData()
    private var netException: MutableLiveData<NetException> = MutableLiveData()
    private var authorId: String? = null


    fun getBooks(): MutableLiveData<Books> {
        return books
    }

    fun getException(): MutableLiveData<NetException> {
        return netException
    }

    fun getAuthorBooks(authorId: String) {
        this.authorId.let {
            if (it == authorId && books.value != null) {
                return
            }
        }

        this.authorId = authorId
        BookService.getInstance().getAuthorBooks(authorId, Service.Success {
            Log.d(TAG, "Success aauther books")
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