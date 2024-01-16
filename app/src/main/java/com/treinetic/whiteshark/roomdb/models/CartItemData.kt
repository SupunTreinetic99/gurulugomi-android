package com.treinetic.whiteshark.roomdb.models

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity

@Keep
@Entity(tableName = "CartItems", primaryKeys = ["bookId"])
class CartItemData(
    @ColumnInfo(name = "bookId")
    var bookId: String,

    @ColumnInfo(name = "book")
    var book: String
) {




}