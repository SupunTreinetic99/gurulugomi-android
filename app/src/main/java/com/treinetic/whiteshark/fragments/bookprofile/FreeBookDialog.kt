package com.treinetic.whiteshark.fragments.bookprofile

import com.treinetic.whiteshark.R
import com.treinetic.whiteshark.models.Book


fun BookProfileFragment.showFreeBookDialog(book: Book) {

    if (!book.isFree) return
    if (book.isPurchased) return

    bottomDialogs.getMessageDialog(
        context = requireContext(),
        titleText = "Free Book",
        messageText = getString(R.string.free_book_messsage),
        positiveText = "Ok"
    ).show()
}
