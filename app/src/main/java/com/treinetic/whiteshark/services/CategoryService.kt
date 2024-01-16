package com.treinetic.whiteshark.services

import android.util.Log
import com.google.gson.Gson
import com.treinetic.whiteshark.constance.ErrorCodes
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.Books
import com.treinetic.whiteshark.models.Category
import com.treinetic.whiteshark.network.Net

/**
 * Created by Nuwan on 2/8/19.
 */
class CategoryService {
    var gson = Gson()
    private val TAG = "CategoryService"

    companion object {
        var categoryList: List<Category> = listOf()

        private var instance = CategoryService()

        fun getInstance(): CategoryService {
            return instance
        }
    }


    fun fetchCategoryList(success: Service.Success<List<Category>>, error: Service.Error) {

        if (categoryList != null && categoryList.size > 0) {
            success.success(categoryList)
            return
        }


        var net = Net(
            Net.URL.CATEGORY_LIST,
            Net.NetMethod.GET,
            null,
            null,
            null,
            null
        )

        net.perform({ response ->
            try {
                categoryList = gson.fromJson(response, Array<Category>::class.java).toList()
                fillCategories()
                success.success(categoryList)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, { exception ->
            exception.printStackTrace()
            error.error(exception)
        })


    }


    fun fillCategories() {
        HomeService.getInstance()?.categoryList?.data?.let {
            it?.forEach { category: Category ->
                val c = getCategoryById(category.id!!)
                c?.let {
                    if (it.isCategoryFilled()) return@let
                    it.fill(category)
                }
            }
        }
    }

    fun fetchCategory(categoryId: String, success: Service.Success<Books>, error: Service.Error) {

        var param: MutableMap<String, Any> = mutableMapOf()
        param["categoryId"] = categoryId

        var net = Net(
            Net.URL.EPUB,
            Net.NetMethod.GET,
            null,
            param,
            null,
            null
        )

        net.perform({ response ->
            try {
                val category: Books = gson.fromJson(response, Books::class.java)
                success.success(category)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, { exception ->
            error.error(exception)
        })

    }


    fun getCategoryBooksNextPage(category: Category, success: Service.Success<Books>, error: Service.Error) {

        category?.books?.next_page_url?.let { nextPageUrl ->
            Log.e(TAG, "CategoryBooksNextPag URL : $nextPageUrl")
            var net = Net(
                nextPageUrl,
                Net.NetMethod.GET,
                null,
                null,
                null,
                null
            )

            net.perform({ response ->
                try {
                    val categoryBooks: Books = gson.fromJson(response, Books::class.java)
                    category.books?.next_page_url = categoryBooks.next_page_url
                    category.books?.data?.addAll(categoryBooks.data)
                    success.success(categoryBooks)
                } catch (e: Exception) {
                    e.printStackTrace()
                    error(NetException("Something went wrong", "JSON_ERROR", ErrorCodes.JSON_ERROR))
                }
            }, { exception ->
                Log.e(TAG, "Error in getCategoryBooksNextPage")
                error.error(exception)
            })
        }


    }


    fun getCategoryById(id: String): Category? {
        return categoryList.find {
            it.id == id
        }
    }


}