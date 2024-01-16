package com.treinetic.whiteshark.fragments.bookcategory

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.Category
import com.treinetic.whiteshark.services.BookService
import com.treinetic.whiteshark.services.CategoryService
import com.treinetic.whiteshark.services.HomeService
import com.treinetic.whiteshark.services.Service

class BookCategoryViewModel : ViewModel() {

    private val TAG: String = "BookCategory"
    private var isLoad: Boolean = false
    private var loadedCategory: Category? = null
    var category: MutableLiveData<Category> = MutableLiveData()
    var error: MutableLiveData<String> = MutableLiveData()
    var netException: NetException? = null


    fun getCategoryName(): String {
        loadedCategory?.category?.let {
            return it
        }
        return ""
    }

    fun loadCategory(categoryId: String) {
        when (categoryId) {
            "offers" -> {
                loadedCategory = HomeService.getInstance().getCategoryById(categoryId)
            }
            "unpublishedEpubs" -> {
                loadedCategory = HomeService.getInstance().getCategoryById(categoryId)
            }
            "free" -> {
                loadedCategory = HomeService.getInstance().getCategoryById(categoryId)
            }
            "newArrivals" -> {
                loadedCategory = HomeService.getInstance().getCategoryById(categoryId)
            }
            "bestSeller" -> {
                loadedCategory = HomeService.getInstance().getCategoryById(categoryId)
            }
            else -> {
                loadedCategory = CategoryService.getInstance().getCategoryById(categoryId)
            }
        }
//        loadedCategory = CategoryService.getInstance().getCategoryById(categoryId)

        loadedCategory?.setBooksUrl()
        category.value = loadedCategory
    }

    fun getIsLoad() = isLoad

    fun setIsLoad(isLoad: Boolean) {
        this.isLoad = isLoad
    }

    fun fetchCategory(categoryId: String) {
        val cat = CategoryService.getInstance().getCategoryById(categoryId)
        cat?.let {
            if (it.isCategoryFilled()) {
                setIsLoad(true)
                return
            }
        }
        CategoryService.getInstance().fetchCategory(categoryId, Service.Success { books ->
            Log.d(TAG, " fetchCategory success")
            val cat = CategoryService.getInstance().getCategoryById(categoryId)
            cat?.let {
                it.books = books
                setIsLoad(true)
                category.postValue(cat)
                return@Success
            }
            error.postValue("Something went wrong")

        }, Service.Error {
            it.printStackTrace()
            Log.d(TAG, "Error in fetchCategory")

            error.postValue("Something went wrong")
        })
    }


//    fun loadNextPageData(nextUrl: String, categoryId: String) {
//        HomeService.getInstance().getNextBookList(Service.Success { result ->
//            Log.i(TAG, "Success loadNextPageData")
//            getBookCategory()
//        }, Service.Error { exception ->
//            Service.Error {
//                Log.e(TAG, "error loadNextPageData")
//            }
//        }, nextUrl, categoryId)
//
//    }


    fun getNextPage() {

        loadedCategory?.let {
            CategoryService.getInstance().getCategoryBooksNextPage(it, Service.Success {
                category.postValue(loadedCategory)
            }, Service.Error {
                Log.e(TAG, "Next page not loaded")
            })
        }
    }

    fun getAuthorsBooks(authorId: String) {
        BookService.getInstance().getAuthorBooks(authorId, Service.Success {
            Log.d(TAG, "Success author books")

        }, Service.Error {
            it.printStackTrace()
            Log.d(TAG, "Error in author books")

            error.postValue("Something went wrong")
        })

    }

}