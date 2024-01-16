package com.treinetic.whiteshark.fragments.category

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.treinetic.whiteshark.models.Category
import com.treinetic.whiteshark.services.CategoryService
import com.treinetic.whiteshark.services.Service

/**
 * Created by Nuwan on 2/8/19.
 */
class CategoryViewModel : ViewModel() {


    private var categoryList: MutableLiveData<List<Category>> = MutableLiveData()
    private var isFetched = false


    fun getCategoryList(): MutableLiveData<List<Category>> {
        return categoryList
    }

    fun fetchCategoryList() {
        getCategoryList().value?.let {

            if (it.isNotEmpty()) {
                return
            }
        }

        CategoryService.getInstance().fetchCategoryList(
            Service.Success { result ->
                categoryList.postValue(result)
                isFetched = true

            }, Service.Error { exception ->

            })

    }


}