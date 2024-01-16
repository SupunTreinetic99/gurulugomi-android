package com.treinetic.whiteshark.fragments.bookprofile

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import com.treinetic.whiteshark.FragmentNavigation
import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.databinding.FragmentBookProfileBinding

fun BookProfileFragment.observeLibraryData(binding: FragmentBookProfileBinding) {
    model.addToLibrary.observe(this, Observer {
        it?.let {
            enableClick(binding.btnPurchase)
            enableClick(binding.button)
            showLoading(false)
            model.fillBook(it)
            changeButton()
            model.addToLibrary.value == null
            model.addToLibraryError.value == null
            showMessageSnackBar(
                mainView,
                "${model.currentBook?.title ?: "Book"} successfully added to library"
            )
            Handler(Looper.getMainLooper()).post {
                FragmentNavigation.getInstance()
                    .startMyLibrary(requireFragmentManager(), R.id.fragment_view)
            }
            model.addToLibrary.value = null
        }
    })
    model.addToLibraryError.observe(this, Observer {
        it?.let {
            enableClick(binding.btnPurchase)
            enableClick(binding.button)
            showLoading(false)
            model.addToLibrary.value == null
            model.addToLibraryError.value == null
            showMessageSnackBar(
                mainView,
                "Failed to add ${model.currentBook?.title ?: "Book"} to library"
            )
            model.addToLibraryError.value = null
        }
    })
}