package com.treinetic.whiteshark.fragments.addreview

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.Book
import com.treinetic.whiteshark.models.Books
import com.treinetic.whiteshark.models.Rating
import com.treinetic.whiteshark.services.BookService
import com.treinetic.whiteshark.services.Service

/**
 * Created by Nuwan on 4/8/19.
 */
class AddReviewViewModel : ViewModel() {

    private val TAG = "AddReviewVM"

    private var rating: MutableLiveData<Rating> = MutableLiveData()
    private var netException: MutableLiveData<NetException> = MutableLiveData()
    var currentBook: Book? = null


    fun getRating(): MutableLiveData<Rating> {
        return rating
    }

    fun getNetException(): MutableLiveData<NetException> {
        return netException
    }

    fun saveReview(id: String, rating: Double, review: String) {

        BookService.getInstance().saveReview(id, rating, review, Service.Success { result ->
            this.rating.postValue(result)
            Books.updateFilledBooksRaings(result, true)
        }, Service.Error { exception ->
            netException.postValue(exception)
        })
    }


    fun editReview() {

    }

    fun fetchReview() {

    }


    fun isValidReview(rating: Double, review: String): Pair<Boolean, String> {
        if (rating == 0.00) {
            return Pair(false, "Rating is required")
        }
        if (review.isBlank()) {
            return Pair(false, "Review is required")
        }
        return Pair(true, "")
    }


}