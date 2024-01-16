package com.treinetic.whiteshark.fragments.profilescategory

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.treinetic.whiteshark.constance.Screens
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.Books
import com.treinetic.whiteshark.services.BookService
import com.treinetic.whiteshark.services.Service

class ProfileCategoryViewModel : ViewModel() {


    private val TAG = "ProfileCategoryVM"
    private var books: MutableLiveData<Books> = MutableLiveData()
    var netException: MutableLiveData<NetException> = MutableLiveData()
    private var id: String? = null
    private var type = ""
    var isfetched = false


    fun setType(type: String) {
        this.type = type
    }


    fun getBooks(): MutableLiveData<Books> {
        return books
    }

    fun getException(): MutableLiveData<NetException> {
        return netException
    }

    fun getProfilesBooks(id: String) {
        this.id.let {
            if (it == id && books.value != null) {
                return
            }
        }

        this.id = id
        if (type == Screens.AUTHORS) {

            BookService.getInstance().getAuthorBooks(id, {
                Log.d(TAG, "Success authors books")
                books.postValue(it)
            }, {
                it.printStackTrace()
                netException.postValue(it)
            })


        } else {

            BookService.getInstance().getPublishersBooks(id, {
                Log.d(TAG, "Success publisher books")
                books.postValue(it)
            }, {
                it.printStackTrace()
                netException.postValue(it)
            })

        }
    }


    fun getNextPage() {
        books.value?.next_page_url?.let {
            BookService.getInstance().getNextPage(it, {
                var booksPage = books.value
                booksPage?.data?.addAll(it.data)
                booksPage?.next_page_url = it.next_page_url
                this.books.postValue(booksPage)
            }, {
                Log.e(TAG, "${it.message}")
            })
        }

    }
}