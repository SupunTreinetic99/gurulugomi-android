package com.treinetic.whiteshark.roomdb

import androidx.annotation.Keep
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.treinetic.whiteshark.roomdb.models.CartItemData

@Keep
@Dao
interface CartItemDao {

    @Insert
    fun insertCartItem(vararg book: CartItemData)

    @Query("SELECT * FROM CartItems WHERE bookId=(:bookId)")
    fun getCartItem(bookId: String): List<CartItemData>

    @Query("SELECT * FROM CartItems")
    fun getAllCartItems(): List<CartItemData>

    @Query("DELETE FROM CartItems")
    fun deleteAllCartItems()

    @Delete
    fun deleteCartItem(vararg book: CartItemData)

}


