package com.treinetic.whiteshark.services

import com.google.gson.Gson
import com.treinetic.whiteshark.models.BookCategory
import com.treinetic.whiteshark.models.Books
import com.treinetic.whiteshark.models.Category
import com.treinetic.whiteshark.models.HomeData
import com.treinetic.whiteshark.network.Net

class HomeService {

    val TAG: String = "HomeService"


    var categoryList = BookCategory()

    companion object {
        private val newInstance = HomeService()
        fun getInstance(): HomeService {
            return newInstance
        }
    }

    fun fetchHomePageData(
        success: Service.Success<BookCategory>,
        error: Service.Error,
        refresh: Boolean = false
    ) {
        if (!categoryList.data.isEmpty() && !refresh) {
            success.success(categoryList)
            return
        }

        val net = Net(
            Net.URL.HOME,
            Net.NetMethod.GET,
            null,
            null,
            null,
            null
        )
        net.perform({ response ->
            try {
                val initData = Gson().fromJson(response, HomeData::class.java)
                categoryList = BookCategory()
                categoryList.next_page_url = initData.categories.next_page_url
                if (categoryList.data.isEmpty()) {
                    categoryList.data = createList(initData).data
                }
                success.success(categoryList)
            } catch (e: Exception) {
                e.printStackTrace()
            }


        },
            { exception ->
                error.error(exception)
            })

    }

    fun fillCategories() {

    }

    fun getNextPageHome(url: String, success: Service.Success<BookCategory>, error: Service.Error) {

        val net = Net(
            url, Net.NetMethod.GET, null, null, null, null
        )
        net.perform({ response ->

            val bookCategory = Gson().fromJson(response, BookCategory::class.java)
            categoryList.next_page_url = bookCategory.next_page_url
            categoryList.data.addAll(bookCategory.data)
            success.success(categoryList)
        },
            { exception ->
                error.error(exception)

            })

    }

    fun getNextBookList(
        success: Service.Success<Category>,
        error: Service.Error,
        url: String,
        categoryId: String
    ) {

        val net = Net(
            url, Net.NetMethod.GET, null, null, null, null
        )
        net.perform({ response ->
            run {
                val updateBooks = Gson().fromJson(response, Books::class.java)
                for (category in categoryList.data) {
                    if (category.id == categoryId) {

                        category.books?.next_page_url = updateBooks.next_page_url
                        updateBooks?.data?.let {
                            category.books?.data?.addAll(it)
                        }
                        success.success(category)
                    }
                }
                success.success(filterCategoryById(categoryId))
            }
        },
            { exception ->
                error.error(exception)
            })

    }

    private fun createList(homeData: HomeData): BookCategory {

        val free = Category()
        free.apply {
            category = homeData.free.category
            books = homeData.free.books
            id = "free"
        }
        val offers = Category()
        offers.apply {
            category = homeData.offers.category
            books = homeData.offers.books
            id = "offers"
            showBackground = true
        }

        val newArrivals = Category()
        newArrivals.apply {
            category = homeData.newArrivals.category
            books = homeData.newArrivals.books
            id = "newArrivals"
        }

        val bestSeller = Category()
        bestSeller.apply {
            category = homeData.bestSeller.category
            books = homeData.bestSeller.books
            id = "bestSeller"
        }

        val unpublishedEpubs = Category()
        homeData.unpublishedEpubs.let {
            unpublishedEpubs.apply {
                category = homeData.unpublishedEpubs?.category
                books = homeData.unpublishedEpubs?.books
                id = "unpublishedEpubs"
            }
        }

        val categories = homeData.categories.data

        val bookCategoryArray = ArrayList<Category>()
        bookCategoryArray.apply {

            if (offers.books?.data?.size != 0) {
                add(offers)
            }
            if (unpublishedEpubs.books?.data != null) {
                if (unpublishedEpubs.books?.data?.size != 0) {
                    add(unpublishedEpubs)
                }
            }
            if (newArrivals.books?.data?.size != 0) {
                add(newArrivals)
            }
            if (bestSeller.books?.data?.size != 0) {
                add(bestSeller)
            }
            if (free.books?.data?.size != 0) {
                add(free)
            }
            for (cat in categories) {
                add(cat)
            }
        }

        val bookCategory = homeData.categories
        bookCategory.data = bookCategoryArray

        return bookCategory
    }

    fun filterCategoryById(categoryId: String): Category {

        var findCategory = Category()

        for (category in categoryList.data) {
            if (categoryId == category.id) {
                findCategory = category
                break
            }
        }

        return findCategory
    }

    fun getNextPageUrl(): String? {
        return categoryList.next_page_url
    }

    fun getCategoryById(id: String): Category? {
        return categoryList.data.find {
            it.id == id
        }
    }

    fun clear() {
        categoryList = BookCategory()
    }

}